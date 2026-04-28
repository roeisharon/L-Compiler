package ir;

import temp.Temp;

public class IrCommandNewClass extends IrCommand {
    public Temp dst;
    public int fieldCount;
    public int classId;

    public IrCommandNewClass(Temp dst, int fieldCount, int classId) {
        this.dst = dst;
        this.fieldCount = fieldCount;
        this.classId = classId;
    }
}
