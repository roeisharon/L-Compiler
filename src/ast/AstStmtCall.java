package ast;
import types.*;

public class AstStmtCall extends AstStmt {
    public AstCallExp callExp;
    public int lineNumber;

    public AstStmtCall(AstCallExp callExp, int lineNumber) {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.callExp = callExp;
        this.lineNumber = lineNumber;
        System.out.format("stmt -> CALL exp SEMICOLON\n");
    }

    public void printMe() {
        System.out.print("AST_STMT_CALL\n");

        if (callExp != null) callExp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "STMT_CALL");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        AstGraphviz.getInstance().logEdge(serialNumber, callExp.serialNumber);
    }
    public Type semantMe() {
        if (callExp == null) {
            return null;
        }
        Type t = callExp.semantMe();
        if (t == null || t.isError()) {
            return t;
        }
        return TypeVoid.getInstance();
    }
    
    public temp.Temp irMe() {
        if (callExp == null) return null;
		callExp.irMe();
        return null;
    }

}
