package ast;
import symboltable.SymbolTable;
import types.*;

public class AstExtCl extends AstNode {
    public String id;
    public int lineNumber;
    public AstExtCl(String id,int lineNumber) {
        this.id = id;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("extend Clause -> ID(%s)\n", id);
    }

    public void printMe() {
        System.out.print("AST_EXT_CLause\n");

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("EXT_CL(%s)", id));
    }
    public Type semantMe() {

        SymbolTable s = SymbolTable.getInstance();

        Type fatherType = s.find(id);

        if (fatherType == null) {
            return new TypeError(lineNumber,
                    "Class '" + id + "' does not exist");
        }

        if (!fatherType.isClass()) {
            return new TypeError(lineNumber,
                    "'" + id + "' is not a class and cannot be used in extends");
        }

        return fatherType;
    }


}