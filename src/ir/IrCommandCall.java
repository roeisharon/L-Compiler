package ir;

import temp.Temp;

public class IrCommandCall extends IrCommand {
    public Temp dst;
    public String funcName;
    public TempList args;

    public IrCommandCall(Temp dst, String funcName, TempList args) {
        this.dst = dst;
        this.funcName = funcName;
        this.args = args;
    }
}
