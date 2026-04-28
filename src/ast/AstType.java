package ast;
import symboltable.SymbolTable;
import types.*;

public class AstType extends AstNode {
    String type_name;
    public int lineNumber;

    public AstType(String type_name, int lineNumber) {
        this.type_name = type_name;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("type -> %s\n", type_name);
    }

    public void printMe() {
        System.out.print("AST_TYPE\n");

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("TYPE(%s)", type_name));
    }
    public Type semantMe(){
        SymbolTable s = SymbolTable.getInstance();
        switch(type_name){
            case "int": return TypeInt.getInstance();
            case "string": return TypeString.getInstance();
            case "void": return TypeVoid.getInstance();
            default:
                Type class_type = s.findClass(type_name);
                //System.out.println("AST_TYPE: " + type_name + " " + class_type);
                // if(t != null && (t.isClass() || t.isArray()))
                //     return t;
                if(class_type != null) return class_type;
        }
        return new TypeError(lineNumber, String.format("Couldn't find type %s", type_name));
    }

}
