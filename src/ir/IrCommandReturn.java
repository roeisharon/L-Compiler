package ir;

import temp.Temp;

public class IrCommandReturn extends IrCommand {
    public Temp value;

    public IrCommandReturn(Temp value) {
        this.value = value;
    }
}
