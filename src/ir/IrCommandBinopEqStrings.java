package ir;

import temp.Temp;

public class IrCommandBinopEqStrings extends IrCommand {
    public Temp t1;
    public Temp t2;
    public Temp dst;

    public IrCommandBinopEqStrings(Temp dst, Temp t1, Temp t2, Boolean isGlobal) {
        this.dst = dst;
        this.t1 = t1;
        this.t2 = t2;
        this.isGlobal = isGlobal;
    }
}