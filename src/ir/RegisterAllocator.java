package ir;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegisterAllocator {
    public static final int K = 10; // $t0-$t9

    public static Map<Integer, Integer> allocate(Cfg cfg) {
        LivenessAnalysis liveness = new LivenessAnalysis(cfg);
        liveness.run();

        Map<Integer, Set<Integer>> graph = buildInterferenceGraph(cfg, liveness);
        return colorBySimplification(graph, K);
    }

    private static Map<Integer, Set<Integer>> buildInterferenceGraph(Cfg cfg, LivenessAnalysis liveness) {
        Map<Integer, Set<Integer>> g = new HashMap<>();

        for (Integer t : liveness.getAllTemps()) {
            g.put(t, new HashSet<>());
        }

        for (CfgNode n : cfg.nodes) {
            Set<Integer> defs = liveness.getDef(n);
            Set<Integer> uses = liveness.getUse(n);
            Set<Integer> liveOut = liveness.getOut(n);

            // Temps that are read by the same IR command must be simultaneously
            // available at that program point, so they must not alias.
            List<Integer> useList = new ArrayList<>(uses);
            for (int i = 0; i < useList.size(); i++) {
                for (int j = i + 1; j < useList.size(); j++) {
                    addUndirectedEdge(g, useList.get(i), useList.get(j));
                }
            }

            // Defs interfere with everything live-out
            for (Integer d : defs) {
                ensureVertex(g, d);
                for (Integer t : liveOut) {
                    if (!d.equals(t)) {
                        addUndirectedEdge(g, d, t);
                    }
                }

                // Def/use interference is usually unnecessary in 3-address IR.
                // Add it only for commands that expand to long instruction
                // sequences where dst aliasing sources is unsafe.
                if (n.cmd instanceof IrCommandStringConcat) {
                    for (Integer u : uses) {
                        if (!d.equals(u)) {
                            addUndirectedEdge(g, d, u);
                        }
                    }
                }
            }
        }

        return g;
    }

    private static void ensureVertex(Map<Integer, Set<Integer>> g, Integer v) {
        if (!g.containsKey(v)) {
            g.put(v, new HashSet<>());
        }
    }

    private static void addUndirectedEdge(Map<Integer, Set<Integer>> g, Integer a, Integer b) {
        if (a.equals(b)) return;
        ensureVertex(g, a);
        ensureVertex(g, b);
        g.get(a).add(b);
        g.get(b).add(a);
    }

    private static Map<Integer, Integer> colorBySimplification(Map<Integer, Set<Integer>> graph, int k) {
        Map<Integer, Set<Integer>> work = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> e : graph.entrySet()) {
            work.put(e.getKey(), new HashSet<>(e.getValue()));
        }

        Deque<Integer> stack = new ArrayDeque<>();

        while (!work.isEmpty()) {
            Integer chosen = null;
            List<Integer> nodes = new ArrayList<>(work.keySet());
            Collections.sort(nodes);

            for (Integer v : nodes) {
                if (work.get(v).size() < k) {
                    chosen = v;
                    break;
                }
            }

            if (chosen == null) {
                // No simplification candidate -> would require spill (not supported)
                return null;
            }

            for (Integer n : work.get(chosen)) {
                work.get(n).remove(chosen);
            }
            work.remove(chosen);
            stack.push(chosen);
        }

        Map<Integer, Integer> color = new HashMap<>();
        while (!stack.isEmpty()) {
            Integer v = stack.pop();
            boolean[] used = new boolean[k];
            Set<Integer> neigh = graph.get(v);
            if (neigh != null) {
                for (Integer n : neigh) {
                    Integer c = color.get(n);
                    if (c != null && c >= 0 && c < k) {
                        used[c] = true;
                    }
                }
            }

            int chosen = -1;
            for (int c = 0; c < k; c++) {
                if (!used[c]) {
                    chosen = c;
                    break;
                }
            }

            if (chosen < 0) {
                return null;
            }
            color.put(v, chosen);
        }

        return color;
    }
}