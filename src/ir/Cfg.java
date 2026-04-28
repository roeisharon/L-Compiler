package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cfg {
    // All nodes in program order (preserved for compatibility with existing analyses)
    public final List<CfgNode> nodes = new ArrayList<>();
    // Split graphs: global commands and non-global (the rest)
    public final List<CfgNode> globalNodes = new ArrayList<>();
    public final List<CfgNode> nonGlobalNodes = new ArrayList<>();

    // Label -> node map for quick lookup (covers all nodes)
    private final Map<String, CfgNode> labelToNode = new HashMap<>();

    private Cfg() {}

    public static Cfg buildFromIr() {
        Cfg cfg = new Cfg();

        Ir ir = Ir.getInstance();
        IrCommand head = ir.getHeadCommand();
        IrCommandList tail = ir.getTailList();

        // collect commands in order
        if (head == null && tail == null) return cfg;

        // first command is in head
        int id = 0;
        if (head != null) {
            CfgNode n = new CfgNode(id++, head);
            cfg.nodes.add(n);
            if (head.isGlobal) cfg.globalNodes.add(n); else cfg.nonGlobalNodes.add(n);
            if (head instanceof IrCommandLabel) {
                cfg.labelToNode.put(((IrCommandLabel) head).labelName, n);
            }
        }

        IrCommandList it = tail;
        while (it != null) {
            IrCommand cmd = it.head;
            CfgNode n = new CfgNode(id++, cmd);
            cfg.nodes.add(n);
            if (cmd.isGlobal) cfg.globalNodes.add(n); else cfg.nonGlobalNodes.add(n);
            if (cmd instanceof IrCommandLabel) {
                cfg.labelToNode.put(((IrCommandLabel) cmd).labelName, n);
            }
            it = it.tail;
        }

        // Step 1: Add sequential edges (fall-through edges)
        // Skip sequential edges for unconditional jumps (they redirect control flow)
        for (int i = 0; i + 1 < cfg.globalNodes.size(); ++i) {
            CfgNode a = cfg.globalNodes.get(i);
            CfgNode b = cfg.globalNodes.get(i+1);

            // Only add sequential edge if 'a' is not a control-flow terminator
            // and both nodes belong to the same graph (both global or both non-global)
            if (!(a.cmd instanceof IrCommandJumpLabel)
                    && !(a.cmd instanceof IrCommandReturn)
                    && (a.cmd.isGlobal == b.cmd.isGlobal)) {
                a.succs.add(b);
                b.preds.add(a);
            }
        }
         for (int i = 0; i + 1 < cfg.nonGlobalNodes.size(); ++i) {
            CfgNode a = cfg.nonGlobalNodes.get(i);
            CfgNode b = cfg.nonGlobalNodes.get(i+1);

            // Only add sequential edge if 'a' is not a control-flow terminator
            // and both nodes belong to the same graph (both global or both non-global)
            if (!(a.cmd instanceof IrCommandJumpLabel)
                    && !(a.cmd instanceof IrCommandReturn)
                    && (a.cmd.isGlobal == b.cmd.isGlobal)) {
                a.succs.add(b);
                b.preds.add(a);
            }
        }

        // Step 2: Add jump edges (explicit control flow)
        for (CfgNode n : cfg.nodes) {
            IrCommand c = n.cmd;
            
            if (c instanceof IrCommandJumpLabel) {
                // Unconditional jump: redirects control to target label
                String target = ((IrCommandJumpLabel) c).labelName;
                CfgNode targetNode = cfg.labelToNode.get(target);
                if (targetNode != null && n.cmd.isGlobal == targetNode.cmd.isGlobal) {
                    n.succs.add(targetNode);
                    targetNode.preds.add(n);
                }
            } else if (c instanceof IrCommandJumpIfEqToZero) {
                // Conditional jump: has two paths
                // 1. Jump edge (taken if condition is true/zero) - added here
                // 2. Fall-through edge (if condition is false/non-zero) - already added in Step 1
                String target = ((IrCommandJumpIfEqToZero) c).labelName;
                CfgNode targetNode = cfg.labelToNode.get(target);
                if (targetNode != null && n.cmd.isGlobal == targetNode.cmd.isGlobal) {
                    n.succs.add(targetNode);
                    targetNode.preds.add(n);
                }
            }
        }

        return cfg;
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();
                sb.append("CFG: ").append(nodes.size()).append(" nodes (globals: ")
                    .append(globalNodes.size()).append(", non-globals: ")
                    .append(nonGlobalNodes.size()).append(")\n");
        for (CfgNode n : nodes) {
            sb.append(n.id).append(": ").append(n.cmd.getClass().getSimpleName()).append(" -> [");
            for (int i = 0; i < n.succs.size(); ++i) {
                if (i>0) sb.append(", ");
                sb.append(n.succs.get(i).id);
            
            }
            sb.append(",");
            sb.append(n.cmd.isGlobal);
            
            sb.append("]\n");
        }
        return sb.toString();
    }
}
