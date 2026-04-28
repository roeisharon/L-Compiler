package ast;
import types.*;

public class AstExpSingle extends AstExp {
    public AstExp exp;
    public int lineNumber;

    public AstExpSingle(AstExp exp,int lineNumber) {
        this.exp = exp;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("exp -> (exp)\n");
    }
    public void printMe() {
        System.out.print("AST_EXP_SINGLE\n");

        if (exp != null) exp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "single_Expresion");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (exp != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, exp.serialNumber);
        }
    }
    public Type semantMe() {
        if (exp == null) return null;
        return exp.semantMe();
    }
    
    public temp.Temp irMe() {
        if (exp == null) return null;
        // Just pass through to the inner expression
        return exp.irMe();
    }

}
