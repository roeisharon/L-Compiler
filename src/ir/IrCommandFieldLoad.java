package ir;

import temp.Temp;

public class IrCommandFieldLoad extends IrCommand {
    public Temp dst;
    public Temp object;
    public int fieldOffsetWords;

    public IrCommandFieldLoad(Temp dst, Temp object, int fieldOffsetWords) {
        this.dst = dst;
        this.object = object;
        this.fieldOffsetWords = fieldOffsetWords;
    }
}
