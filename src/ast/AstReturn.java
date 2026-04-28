package ast;
import symboltable.SymbolTable;
import types.*;

public class AstReturn extends AstStmt {
    public AstExp returnExp;
    public int lineNumber;

    public AstReturn(AstExp returnExp, int lineNumber) {
        this.returnExp = returnExp;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        if (returnExp != null) {
            System.out.format("stmt -> RETURN exp SEMICOLON\n");
        } else {
            System.out.format("stmt -> RETURN SEMICOLON\n");
        }
    }

    public void printMe() {
        System.out.print("AST_STMT_RETURN\n");

        if (returnExp != null) returnExp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "STMT_RETURN");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (returnExp != null)
            AstGraphviz.getInstance().logEdge(serialNumber, returnExp.serialNumber);
    }
    public Type semantMe() {

        SymbolTable s = SymbolTable.getInstance();

        // no return expression → treat as void
        if (returnExp == null) {
            if (!s.canReturnType(TypeVoid.getInstance())) {
                return new TypeError(lineNumber,
                        "Function must return a non-void value");
            }
            return TypeVoid.getInstance();
        }

        // check expression
        Type expType = returnExp.semantMe();
        if (expType == null || expType.isError()) return expType;
        if (expType.isVoid()) {
            
                return new TypeError(lineNumber,
                        "Function cannot return a function of void ");
           
        }

        if (!s.canReturnType(expType)) {
            return new TypeError(lineNumber,
                    String.format("Invalid return type '%s' for this function", expType.name));
        }

        return TypeVoid.getInstance();
    }

    public temp.Temp irMe() {
        temp.Temp retVal = null;
        if (returnExp != null) {
            retVal = returnExp.irMe();
        }
        ir.Ir.getInstance().AddIrCommand(new ir.IrCommandReturn(retVal));
        return null;
    }


}
