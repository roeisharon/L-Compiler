package ast;
import types.*;
import symboltable.SymbolTable;

public class AstExpNil extends AstExp {
    public int lineNumber;

    public AstExpNil(int lineNumber) {
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("exp -> NIL\n");
        this.lineNumber = lineNumber;
    }

    public void printMe() {
        System.out.print("AST_EXP_NIL\n");

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "EXP_NIL");
    }
    public Type semantMe(){
        return TypeNil.getInstance();
    }

    public temp.Temp irMe() {
        temp.Temp t = temp.TempFactory.getInstance().getFreshTemp();
        ir.Ir.getInstance().AddIrCommand(new ir.IRcommandConstInt(t, 0, false));
        return t;
    }
}