package ir;

import java.util.ArrayList;
import java.util.List;

public class CfgNode {
    public final int id;
    public final IrCommand cmd;
    public final List<CfgNode> preds = new ArrayList<>();
    public final List<CfgNode> succs = new ArrayList<>();

    public CfgNode(int id, IrCommand cmd) {
        this.id = id;
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node(").append(id).append(") : ");
        sb.append(cmd.getClass().getSimpleName());
        return sb.toString();
    }
}
