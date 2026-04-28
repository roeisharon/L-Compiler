package ast;
import symboltable.SymbolTable;
import types.*;

public class AstNewExp extends AstExp {
    public AstType type;
    public AstExp exp;
    public int lineNumber;
    private static int hiddenObjSlotCounter = 0;

    public AstNewExp(AstType type, AstExp exp,int lineNumber) {
        this.type = type;
        this.exp = exp;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        if (exp != null) {
            System.out.format("exp -> NEW type [exp]\n");
        } else {
            System.out.format("exp -> NEW type \n");

        }
        
    }

    public void printMe() {
        System.out.print("AST_NEW_EXP\n");

        if (type != null) type.printMe();
        if (exp != null) exp.printMe();
        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "NEW_EXP");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (type != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
        }
        if (exp != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, exp.serialNumber);
        }
    }

    public Type semantMe(){
        SymbolTable s = SymbolTable.getInstance();

        Type t = type.semantMe();
        if (t == null || t.isError()) return t;

        // case: new Class
        if (exp == null) {
            if (!t.isClass()) {
                return new TypeError(lineNumber, "new on non-class type");
            }
            
            return t;
        }

        // case: new Type[size]
        Type sizeType = exp.semantMe();
        if (!sizeType.isInt()) {
            return new TypeError(lineNumber, "Array size expression must be int");
        }
        if (t.isVoid()) {
            return new TypeError(lineNumber, "Cannot create array of void");
        }
        if(exp instanceof AstExpInt && ((AstExpInt)exp).value <= 0) {
            return new TypeError(lineNumber, "Array size must be non-negative");
        }

        return new TypeArray(null, t);
    }

    public temp.Temp irMe() {
        if (exp == null) {
            Type t = type.semantMe();
            if (t == null || !t.isClass()) return null;
            types.TypeClass cls = (types.TypeClass) t;
            temp.Temp dst = temp.TempFactory.getInstance().getFreshTemp();
            ir.Ir.getInstance().AddIrCommand(new ir.IrCommandNewClass(dst, cls.totalFieldCount(), cls.classId));

            String hiddenObjSlot = "__newobj_slot_" + (hiddenObjSlotCounter++);
            ir.Ir.getInstance().registerGlobal(hiddenObjSlot);
            ir.Ir.getInstance().AddIrCommand(new ir.IrCommandAllocate(hiddenObjSlot));
            ir.Ir.getInstance().AddIrCommand(new ir.IrCommandStore(hiddenObjSlot, hiddenObjSlot, dst, true));

			for (java.util.Map.Entry<String, Integer> e : cls.getAllIntFieldInitializers().entrySet()) {
				int off = cls.getFieldOffset(e.getKey());
				if (off >= 0) {
                    temp.Temp objTmp = temp.TempFactory.getInstance().getFreshTemp();
                    ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLoad(objTmp, hiddenObjSlot, hiddenObjSlot, true));
                    ir.Ir.getInstance().AddIrCommand(new ir.IrCommandFieldStoreImm(objTmp, e.getValue(), off));
				}
			}

            for (java.util.Map.Entry<String, String> e : cls.getAllStringFieldInitializers().entrySet()) {
                int off = cls.getFieldOffset(e.getKey());
                if (off >= 0) {
                    temp.Temp objTmp = temp.TempFactory.getInstance().getFreshTemp();
                    temp.Temp strTmp = temp.TempFactory.getInstance().getFreshTemp();
                    ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLoad(objTmp, hiddenObjSlot, hiddenObjSlot, true));
                    ir.Ir.getInstance().AddIrCommand(new ir.IrCommandConstString(strTmp, e.getValue(), true));
                    ir.Ir.getInstance().AddIrCommand(new ir.IrCommandFieldStore(objTmp, strTmp, off));
                }
            }

            for (java.util.Map.Entry<String, Boolean> e : cls.getAllNilFieldInitializers().entrySet()) {
                int off = cls.getFieldOffset(e.getKey());
                if (off >= 0) {
                    temp.Temp objTmp = temp.TempFactory.getInstance().getFreshTemp();
                    ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLoad(objTmp, hiddenObjSlot, hiddenObjSlot, true));
                    ir.Ir.getInstance().AddIrCommand(new ir.IrCommandFieldStoreImm(objTmp, 0, off));
                }
            }
            return dst;
        }
        temp.Temp sizeTemp = exp.irMe();
        if (sizeTemp == null) return null;

        temp.Temp dst = temp.TempFactory.getInstance().getFreshTemp();
        ir.Ir.getInstance().AddIrCommand(new ir.IrCommandNewArray(dst, sizeTemp));
        return dst;
    }
}