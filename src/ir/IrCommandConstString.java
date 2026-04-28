package ir;

import temp.Temp;

public class IrCommandConstString extends IrCommand {
    public Temp dst;
    public String value;

    public IrCommandConstString(Temp dst, String value, boolean isGlobal) {
        this.dst = dst;
        this.value = value;
        this.isGlobal = isGlobal;
    }
}
