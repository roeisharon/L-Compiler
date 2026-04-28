package ir;

import temp.Temp;

public class IrCommandArrayStore extends IrCommand {
    public Temp array;
    public Temp index;
    public Temp value;

    public IrCommandArrayStore(Temp array, Temp index, Temp value) {
        this.array = array;
        this.index = index;
        this.value = value;
    }
}
