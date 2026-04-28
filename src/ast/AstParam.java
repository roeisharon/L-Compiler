package ast;
import symboltable.SymbolTable;
import types.*;

public class AstParam extends AstNode {
    public String name;
    public AstType type;
    public int lineNumber;

    public AstParam(String name, AstType type, int lineNumber) {
        this.name = name;
        this.type = type;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("param -> TYPE ID\n", name);
    }

    public void printMe() {
        System.out.format("AST_PARAM ID( %s )\n", name);
        if (type != null) type.printMe();
        AstGraphviz.getInstance().logNode(serialNumber, String.format("PARAM NAME(%s)", name));
        if (type != null) AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
    }

    public Type semantMe(){
        
        if (type == null) {
            return new TypeError(lineNumber,
                    String.format("Parameter '%s' must have a type", name));
        }
        Type t = type.semantMe();
        if (t == null || t.isError()) return t;
        if (t.isVoid())
            return new TypeError(lineNumber, "Parameter type cannot be void");


        return t;
    }
}