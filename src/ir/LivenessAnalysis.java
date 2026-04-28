package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import temp.Temp;

public class LivenessAnalysis {
    private final Cfg cfg;
    private final Map<CfgNode, Set<Integer>> in = new HashMap<>();
    private final Map<CfgNode, Set<Integer>> out = new HashMap<>();
    private final Map<CfgNode, Set<Integer>> use = new HashMap<>();
    private final Map<CfgNode, Set<Integer>> def = new HashMap<>();

    public LivenessAnalysis(Cfg cfg) {
        this.cfg = cfg;
        for (CfgNode n : cfg.nodes) {
            use.put(n, computeUse(n.cmd));
            def.put(n, computeDef(n.cmd));
            in.put(n, new HashSet<>());
            out.put(n, new HashSet<>());
        }
    }

    public void run() {
        List<CfgNode> order = new ArrayList<>(cfg.nodes);
        boolean changed;
        do {
            changed = false;
            for (int i = order.size() - 1; i >= 0; --i) {
                CfgNode n = order.get(i);

                Set<Integer> newOut = new HashSet<>();
                for (CfgNode s : n.succs) {
                    newOut.addAll(in.get(s));
                }

                Set<Integer> newIn = new HashSet<>(use.get(n));
                Set<Integer> outMinusDef = new HashSet<>(newOut);
                outMinusDef.removeAll(def.get(n));
                newIn.addAll(outMinusDef);

                if (!newOut.equals(out.get(n)) || !newIn.equals(in.get(n))) {
                    out.put(n, newOut);
                    in.put(n, newIn);
                    changed = true;
                }
            }
        } while (changed);
    }

    public Set<Integer> getIn(CfgNode n) {
        return in.get(n);
    }

    public Set<Integer> getOut(CfgNode n) {
        return out.get(n);
    }

    public Set<Integer> getUse(CfgNode n) {
        return use.get(n);
    }

    public Set<Integer> getDef(CfgNode n) {
        return def.get(n);
    }

    public Set<Integer> getAllTemps() {
        Set<Integer> all = new HashSet<>();
        for (CfgNode n : cfg.nodes) {
            all.addAll(use.get(n));
            all.addAll(def.get(n));
        }
        return all;
    }

    private static void addTemp(Set<Integer> s, Temp t) {
        if (t != null) s.add(t.getSerialNumber());
    }

    private static void addTempList(Set<Integer> s, TempList list) {
        for (TempList it = list; it != null; it = it.tail) {
            addTemp(s, it.head);
        }
    }

    private static Set<Integer> computeUse(IrCommand cmd) {
        Set<Integer> u = new HashSet<>();

        if (cmd instanceof IrCommandBinopAddIntegers) {
            IrCommandBinopAddIntegers c = (IrCommandBinopAddIntegers) cmd;
            addTemp(u, c.t1);
            addTemp(u, c.t2);
        } else if (cmd instanceof IrCommandBinopSubIntegers) {
            IrCommandBinopSubIntegers c = (IrCommandBinopSubIntegers) cmd;
            addTemp(u, c.t1);
            addTemp(u, c.t2);
        } else if (cmd instanceof IrCommandBinopMulIntegers) {
            IrCommandBinopMulIntegers c = (IrCommandBinopMulIntegers) cmd;
            addTemp(u, c.t1);
            addTemp(u, c.t2);
        } else if (cmd instanceof IrCommandBinopDivIntegers) {
            IrCommandBinopDivIntegers c = (IrCommandBinopDivIntegers) cmd;
            addTemp(u, c.t1);
            addTemp(u, c.t2);
        } else if (cmd instanceof IrCommandBinopLtIntegers) {
            IrCommandBinopLtIntegers c = (IrCommandBinopLtIntegers) cmd;
            addTemp(u, c.t1);
            addTemp(u, c.t2);
        } else if (cmd instanceof IrCommandBinopEqIntegers) {
            IrCommandBinopEqIntegers c = (IrCommandBinopEqIntegers) cmd;
            addTemp(u, c.t1);
            addTemp(u, c.t2);
        } else if (cmd instanceof IrCommandBinopEqStrings) {
            IrCommandBinopEqStrings c = (IrCommandBinopEqStrings) cmd;
            addTemp(u, c.t1);
            addTemp(u, c.t2);
        } else if (cmd instanceof IrCommandStore) {
            IrCommandStore c = (IrCommandStore) cmd;
            addTemp(u, c.src);
        } else if (cmd instanceof IrCommandJumpIfEqToZero) {
            IrCommandJumpIfEqToZero c = (IrCommandJumpIfEqToZero) cmd;
            addTemp(u, c.t);
        } else if (cmd instanceof IrCommandPrintInt) {
            IrCommandPrintInt c = (IrCommandPrintInt) cmd;
            addTemp(u, c.t);
        } else if (cmd instanceof IrCommandPrintString) {
            IrCommandPrintString c = (IrCommandPrintString) cmd;
            addTemp(u, c.t);
        } else if (cmd instanceof IrCommandStringConcat) {
            IrCommandStringConcat c = (IrCommandStringConcat) cmd;
            addTemp(u, c.left);
            addTemp(u, c.right);
        } else if (cmd instanceof IrCommandCall) {
            IrCommandCall c = (IrCommandCall) cmd;
            addTempList(u, c.args);
        } else if (cmd instanceof IrCommandNewArray) {
            IrCommandNewArray c = (IrCommandNewArray) cmd;
            addTemp(u, c.size);
        } else if (cmd instanceof IrCommandArrayLoad) {
            IrCommandArrayLoad c = (IrCommandArrayLoad) cmd;
            addTemp(u, c.array);
            addTemp(u, c.index);
        } else if (cmd instanceof IrCommandArrayStore) {
            IrCommandArrayStore c = (IrCommandArrayStore) cmd;
            addTemp(u, c.array);
            addTemp(u, c.index);
            addTemp(u, c.value);
        } else if (cmd instanceof IrCommandFieldLoad) {
            IrCommandFieldLoad c = (IrCommandFieldLoad) cmd;
            addTemp(u, c.object);
        } else if (cmd instanceof IrCommandFieldStore) {
            IrCommandFieldStore c = (IrCommandFieldStore) cmd;
            addTemp(u, c.object);
            addTemp(u, c.value);
        } else if (cmd instanceof IrCommandFieldStoreImm) {
            IrCommandFieldStoreImm c = (IrCommandFieldStoreImm) cmd;
            addTemp(u, c.object);
        } else if (cmd instanceof IrCommandReturn) {
            IrCommandReturn c = (IrCommandReturn) cmd;
            addTemp(u, c.value);
        }

        return u;
    }

    private static Set<Integer> computeDef(IrCommand cmd) {
        Set<Integer> d = new HashSet<>();

        if (cmd instanceof IRcommandConstInt) {
            IRcommandConstInt c = (IRcommandConstInt) cmd;
            addTemp(d, c.t);
        } else if (cmd instanceof IrCommandConstString) {
            IrCommandConstString c = (IrCommandConstString) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandLoad) {
            IrCommandLoad c = (IrCommandLoad) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandBinopAddIntegers) {
            IrCommandBinopAddIntegers c = (IrCommandBinopAddIntegers) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandBinopSubIntegers) {
            IrCommandBinopSubIntegers c = (IrCommandBinopSubIntegers) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandBinopMulIntegers) {
            IrCommandBinopMulIntegers c = (IrCommandBinopMulIntegers) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandBinopDivIntegers) {
            IrCommandBinopDivIntegers c = (IrCommandBinopDivIntegers) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandBinopLtIntegers) {
            IrCommandBinopLtIntegers c = (IrCommandBinopLtIntegers) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandBinopEqIntegers) {
            IrCommandBinopEqIntegers c = (IrCommandBinopEqIntegers) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandBinopEqStrings) {
            IrCommandBinopEqStrings c = (IrCommandBinopEqStrings) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandStringConcat) {
            IrCommandStringConcat c = (IrCommandStringConcat) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandCall) {
            IrCommandCall c = (IrCommandCall) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandNewArray) {
            IrCommandNewArray c = (IrCommandNewArray) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandArrayLoad) {
            IrCommandArrayLoad c = (IrCommandArrayLoad) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandNewClass) {
            IrCommandNewClass c = (IrCommandNewClass) cmd;
            addTemp(d, c.dst);
        } else if (cmd instanceof IrCommandFieldLoad) {
            IrCommandFieldLoad c = (IrCommandFieldLoad) cmd;
            addTemp(d, c.dst);
        }

        return d;
    }
}