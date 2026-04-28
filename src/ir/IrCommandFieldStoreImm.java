package ir;

import temp.Temp;

public class IrCommandFieldStoreImm extends IrCommand {
    public Temp object;
    public int immValue;
    public int fieldOffsetWords;

    public IrCommandFieldStoreImm(Temp object, int immValue, int fieldOffsetWords) {
        this.object = object;
        this.immValue = immValue;
        this.fieldOffsetWords = fieldOffsetWords;
    }
}
