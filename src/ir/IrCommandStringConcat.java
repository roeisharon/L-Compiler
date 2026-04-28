package ir;

import temp.Temp;

public class IrCommandStringConcat extends IrCommand {
    public Temp dst;
    public Temp left;
    public Temp right;

    public IrCommandStringConcat(Temp dst, Temp left, Temp right, boolean isGlobal) {
        this.dst = dst;
        this.left = left;
        this.right = right;
        this.isGlobal = isGlobal;
    }
}
