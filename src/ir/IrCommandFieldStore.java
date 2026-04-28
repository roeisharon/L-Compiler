package ir;

import temp.Temp;

public class IrCommandFieldStore extends IrCommand {
    public Temp object;
    public Temp value;
    public int fieldOffsetWords;

    public IrCommandFieldStore(Temp object, Temp value, int fieldOffsetWords) {
        this.object = object;
        this.value = value;
        this.fieldOffsetWords = fieldOffsetWords;
    }
}
