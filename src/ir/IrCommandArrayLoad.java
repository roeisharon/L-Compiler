package ir;

import temp.Temp;

public class IrCommandArrayLoad extends IrCommand {
    public Temp dst;
    public Temp array;
    public Temp index;

    public IrCommandArrayLoad(Temp dst, Temp array, Temp index) {
        this.dst = dst;
        this.array = array;
        this.index = index;
    }
}
