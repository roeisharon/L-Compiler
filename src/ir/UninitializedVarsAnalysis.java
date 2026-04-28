package ir;

import java.util.*;
import temp.Temp;

/**
 * Dataflow analysis to detect uninitialized variable usage.
 * * Logic: Must-Available (Intersection).
 * - A variable is initialized at node N only if it is initialized on ALL paths
 * reaching N.
 * - Initialization: All nodes start with ALL variables (Top) to prevent killing
 * definitions at loop headers.
 */
public class UninitializedVarsAnalysis {
    private final Cfg cfg;

    // Dataflow analysis state
    private final Map<CfgNode, Set<String>> in = new HashMap<>();
    private final Map<CfgNode, Set<String>> out = new HashMap<>();

    // Map from temp to the node that defines it
    private final Map<Temp, CfgNode> tempDefinitions = new HashMap<>();

    // Collect uninitialized variable accesses (errors)
    private final Set<String> uninitializedAccesses = new TreeSet<>();

    // Universal set of all variables in the program (for initialization)
    private final Set<String> allVariables = new HashSet<>();

    // Variables that are initialized globally (treated as initialized at entry)
    

    // Seed set to be used as the initial IN for non-global entry nodes
    private final Set<String> nonGlobalInitialIn = new HashSet<>();

    private UninitializedVarsAnalysis(Cfg cfg) {
        this.cfg = cfg;
        buildTempDefinitions();
        collectAllVariables();
        initializeDataflowSets();
    }

    /**
     * Run the dataflow analysis and return sorted set of uninitialized variable
     * names.
     */
    public static Set<String> analyze(Cfg cfg) {
        UninitializedVarsAnalysis analysis = new UninitializedVarsAnalysis(cfg);

        // Phase 1: analyze globals only
        analysis.chaoticIterationOver(cfg.globalNodes);

        // Build seed for non-global entries from global analysis results
        Set<String> seeded = new HashSet<>();
       
        
            // Find global exit nodes (globals with no global successors)
            List<CfgNode> exits = new ArrayList<>();
            for (CfgNode g : cfg.globalNodes) {
                boolean hasGlobalSucc = false;
                for (CfgNode s : g.succs) {
                    if (s.cmd.isGlobal) { hasGlobalSucc = true; break; }
                }
                if (!hasGlobalSucc) exits.add(g);
            }
            if (exits.isEmpty()) exits = cfg.globalNodes;
            System.out.println("Global exit nodes: " + exits);

            // Intersect OUT sets of exit nodes to find vars initialized on all global-exit paths
            seeded.addAll(analysis.allVariables);
            for (CfgNode e : exits) {
                Set<String> outSet = analysis.out.get(e);
                seeded.retainAll(outSet);
            }
            if (exits.isEmpty()) seeded.clear();;
        

        analysis.nonGlobalInitialIn.clear();
        analysis.nonGlobalInitialIn.addAll(seeded);
        System.out.println("Seeding non-global analysis with initialized vars: " + seeded);

        // Phase 2: analyze non-globals using the seed
        analysis.chaoticIterationOver(cfg.nonGlobalNodes);

        // Convert unique IDs back to variable names for output
        Set<String> result = new TreeSet<>();
        for (String uniqueId : analysis.uninitializedAccesses) {
            result.add(analysis.getVarNameFromUniqueId(uniqueId));
        }
        return result;
    }

    /**
     * 1. Collect all variable IDs to form the Universal Set.
     */
    private void collectAllVariables() {
        for (CfgNode node : cfg.nodes) {
            IrCommand cmd = node.cmd;
            if (cmd instanceof IrCommandStore) {
                IrCommandStore store = (IrCommandStore) cmd;
                allVariables.add(store.uniqueVarId);
                
            } else if (cmd instanceof IrCommandLoad) {
                allVariables.add(((IrCommandLoad) cmd).uniqueVarId);
                System.out.println("Variable found: " + ((IrCommandLoad) cmd).uniqueVarId);
            }
        }
    }

    /**
     * 2. Initialize sets for Must Analysis.
     * OUT sets must start with ALL variables (Optimistic).
     * Entry IN set is Empty.
     */
    private void initializeDataflowSets() {
        for (CfgNode node : cfg.nodes) {
            in.put(node, new HashSet<>()); // Will be computed
            // KEY FIX: Initialize OUT to Universal Set (All Variables)
            // This ensures loops don't kill variables on the first pass
            out.put(node, new HashSet<>(allVariables));
        }
    }

    private void chaoticIteration() {
        // Backwards-compatible full-CFG iteration (kept for compatibility)
        Queue<CfgNode> worklist = new LinkedList<>(cfg.nodes);

        while (!worklist.isEmpty()) {
            CfgNode n = worklist.poll();

            // 1. Compute IN[n]
            Set<String> newIn = computeIn(n);

            // Save old IN to check for changes if needed (optional optimization)
            in.put(n, newIn);

            // 2. Compute OUT[n] using transfer function
            Set<String> newOut = transferFunction(n, newIn);
            System.out.println("Node " + n.id + " IRCommand " + n.cmd + " IN: " + newIn + " OUT: " + newOut);

            // 3. Update OUT[n] and check for convergence
            Set<String> oldOut = out.get(n);
            if (!newOut.equals(oldOut)) {
                out.put(n, newOut);

                // Add successors to worklist
                for (CfgNode succ : n.succs) {
                    if (!worklist.contains(succ)) {
                        worklist.add(succ);
                    }
                }
            }
        }
    }

    /**
     * Run chaotic iteration but limited to a provided list of nodes (e.g., globals or non-globals).
     */
    private void chaoticIterationOver(List<CfgNode> nodesToProcess) {
        if (nodesToProcess == null || nodesToProcess.isEmpty()) return;

        Queue<CfgNode> worklist = new LinkedList<>(nodesToProcess);

        while (!worklist.isEmpty()) {
            CfgNode n = worklist.poll();

            // 1. Compute IN[n]
            Set<String> newIn = computeIn(n);

            // Save old IN
            in.put(n, newIn);

            // 2. Compute OUT[n]
            Set<String> newOut = transferFunction(n, newIn);
            System.out.println("Node " + n.id + " IRCommand " + n.cmd + " IN: " + newIn + " OUT: " + newOut);

            // 3. Update OUT and schedule successors that are inside nodesToProcess
            Set<String> oldOut = out.get(n);
            if (!newOut.equals(oldOut)) {
                out.put(n, newOut);
                for (CfgNode succ : n.succs) {
                    if (nodesToProcess.contains(succ) && !worklist.contains(succ)) {
                        worklist.add(succ);
                    }
                }
            }
        }
    }

    private Set<String> computeIn(CfgNode n) {
        
        if (n == cfg.nodes.get(0) && n.cmd.isGlobal) {
            return new HashSet<>();
        }
        if (!cfg.nonGlobalNodes.isEmpty() && n == cfg.nonGlobalNodes.get(0)) {
            return new HashSet<>(nonGlobalInitialIn);
        }
        if( n.preds.isEmpty()  ) {
            return new HashSet<>(nonGlobalInitialIn);
        }

        // INTERSECTION: Start with first pred's OUT
        Set<String> result = new HashSet<>(out.get(n.preds.get(0)));

        // Intersect with remaining preds
        for (int i = 1; i < n.preds.size(); i++) {
            result.retainAll(out.get(n.preds.get(i)));
        }

        return result;
    }

    private Set<String> transferFunction(CfgNode n, Set<String> inSet) {
        IrCommand cmd = n.cmd;
        Set<String> outSet = new HashSet<>(inSet);

        if (cmd instanceof IrCommandStore) {
            // x := exp
            IrCommandStore store = (IrCommandStore) cmd;

            // For stores (global or non-global) evaluate RHS initialization
            // and update OUT accordingly. Global stores should also be
            // processed so their effect propagates to the seeded IN for
            // non-global analysis.

            String uniqueId = store.uniqueVarId;
            Temp src = store.src;

            if (isTempInitialized(n, src)) {
                outSet.add(uniqueId); // Gen
            } else {
                outSet.remove(uniqueId); // Kill
            }

        } else if (cmd instanceof IrCommandLoad) {
            // temp := x
            IrCommandLoad load = (IrCommandLoad) cmd;
            String uniqueId = load.uniqueVarId;

            // Report error if x is not in IN set
            if (!inSet.contains(uniqueId)) {
                uninitializedAccesses.add(uniqueId);
            }

        } else if (cmd instanceof IrCommandPrintInt) {
            IrCommandPrintInt print = (IrCommandPrintInt) cmd;
            if (!isTempInitialized(n, print.t)) {
                findUninitializedVarsInTemp(print.t, new HashSet<>());
            }
        }

        return outSet;
    }

    private boolean isTempInitialized(CfgNode usageNode, Temp t) {
        return isTempInitializedRecursive(t, new HashSet<>());
    }

    private boolean isTempInitializedRecursive(Temp t, Set<Temp> visited) {
        if (visited.contains(t))
            return false;
        visited.add(t);

        CfgNode defNode = tempDefinitions.get(t);
        if (defNode == null)
            return false;

        IrCommand cmd = defNode.cmd;

        if (cmd instanceof IRcommandConstInt) {
            return true;
        } else if (cmd instanceof IrCommandLoad) {
            String uniqueId = ((IrCommandLoad) cmd).uniqueVarId;
            // Check IN set of the DEFINITION node
            return in.get(defNode).contains(uniqueId);
        } else if (isBinop(cmd)) {
            Temp t1 = getBinopT1(cmd);
            Temp t2 = getBinopT2(cmd);
            return isTempInitializedRecursive(t1, visited) &&
                    isTempInitializedRecursive(t2, visited);
        }
        return false;
    }

    private void findUninitializedVarsInTemp(Temp t, Set<Temp> visited) {
        if (visited.contains(t))
            return;
        visited.add(t);

        CfgNode defNode = tempDefinitions.get(t);
        if (defNode == null)
            return;

        IrCommand cmd = defNode.cmd;
        if (cmd instanceof IrCommandLoad) {
            String uniqueId = ((IrCommandLoad) cmd).uniqueVarId;
            if (!in.get(defNode).contains(uniqueId)) {
                uninitializedAccesses.add(uniqueId);
            }
        } else if (isBinop(cmd)) {
            findUninitializedVarsInTemp(getBinopT1(cmd), visited);
            findUninitializedVarsInTemp(getBinopT2(cmd), visited);
        }
    }

    // --- Helpers ---

    private void buildTempDefinitions() {
        for (CfgNode node : cfg.nodes) {
            IrCommand cmd = node.cmd;
            Temp defined = getDefinedTemp(cmd);
            if (defined != null) {
                tempDefinitions.put(defined, node);
            }
        }
    }

    private Temp getDefinedTemp(IrCommand cmd) {
        if (cmd instanceof IRcommandConstInt)
            return ((IRcommandConstInt) cmd).t;
        if (cmd instanceof IrCommandLoad)
            return ((IrCommandLoad) cmd).dst;
        if (cmd instanceof IrCommandBinopAddIntegers)
            return ((IrCommandBinopAddIntegers) cmd).dst;
        if (cmd instanceof IrCommandBinopSubIntegers)
            return ((IrCommandBinopSubIntegers) cmd).dst;
        if (cmd instanceof IrCommandBinopMulIntegers)
            return ((IrCommandBinopMulIntegers) cmd).dst;
        if (cmd instanceof IrCommandBinopDivIntegers)
            return ((IrCommandBinopDivIntegers) cmd).dst;
        if (cmd instanceof IrCommandBinopLtIntegers)
            return ((IrCommandBinopLtIntegers) cmd).dst;
        if (cmd instanceof IrCommandBinopEqIntegers)
            return ((IrCommandBinopEqIntegers) cmd).dst;
        if (cmd instanceof IrCommandBinopEqStrings)
            return ((IrCommandBinopEqStrings) cmd).dst;
        return null;
    }

    private boolean isBinop(IrCommand cmd) {
        return cmd instanceof IrCommandBinopAddIntegers ||
                cmd instanceof IrCommandBinopSubIntegers ||
                cmd instanceof IrCommandBinopMulIntegers ||
                cmd instanceof IrCommandBinopDivIntegers ||
                cmd instanceof IrCommandBinopLtIntegers ||
                cmd instanceof IrCommandBinopEqIntegers ||
                cmd instanceof IrCommandBinopEqStrings;
    }

    private Temp getBinopT1(IrCommand cmd) {
        if (cmd instanceof IrCommandBinopAddIntegers)
            return ((IrCommandBinopAddIntegers) cmd).t1;
        if (cmd instanceof IrCommandBinopSubIntegers)
            return ((IrCommandBinopSubIntegers) cmd).t1;
        if (cmd instanceof IrCommandBinopMulIntegers)
            return ((IrCommandBinopMulIntegers) cmd).t1;
        if (cmd instanceof IrCommandBinopDivIntegers)
            return ((IrCommandBinopDivIntegers) cmd).t1;
        if (cmd instanceof IrCommandBinopLtIntegers)
            return ((IrCommandBinopLtIntegers) cmd).t1;
        if (cmd instanceof IrCommandBinopEqIntegers)
            return ((IrCommandBinopEqIntegers) cmd).t1;
        if (cmd instanceof IrCommandBinopEqStrings)
            return ((IrCommandBinopEqStrings) cmd).t1;
        return null;
    }

    private Temp getBinopT2(IrCommand cmd) {
        if (cmd instanceof IrCommandBinopAddIntegers)
            return ((IrCommandBinopAddIntegers) cmd).t2;
        if (cmd instanceof IrCommandBinopSubIntegers)
            return ((IrCommandBinopSubIntegers) cmd).t2;
        if (cmd instanceof IrCommandBinopMulIntegers)
            return ((IrCommandBinopMulIntegers) cmd).t2;
        if (cmd instanceof IrCommandBinopDivIntegers)
            return ((IrCommandBinopDivIntegers) cmd).t2;
        if (cmd instanceof IrCommandBinopLtIntegers)
            return ((IrCommandBinopLtIntegers) cmd).t2;
        if (cmd instanceof IrCommandBinopEqIntegers)
            return ((IrCommandBinopEqIntegers) cmd).t2;
        if (cmd instanceof IrCommandBinopEqStrings)
            return ((IrCommandBinopEqStrings) cmd).t2;
        return null;
    }

    private String getVarNameFromUniqueId(String uniqueId) {
        int hashIndex = uniqueId.indexOf('#');
        if (hashIndex >= 0) {
            return uniqueId.substring(0, hashIndex);
        }
        return uniqueId;
    }
}