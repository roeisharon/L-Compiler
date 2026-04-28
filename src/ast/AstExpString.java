package ast;

import types.*;
import symboltable.SymbolTable;


public class AstExpString extends AstExp {
    public String value;
    public int lineNumber;

    public AstExpString(String value,int lineNumber) {
        this.value = value;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("exp -> STRING( %s )\n", value);
    }

    public void printMe() {
        System.out.print("AST_EXP_STRING\n");

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("STRING(%s)", value));
    }
    public Type semantMe() {
        return TypeString.getInstance();
    }

    public temp.Temp irMe() {
        temp.Temp t = temp.TempFactory.getInstance().getFreshTemp();
        ir.IrCommandConstString cmd = new ir.IrCommandConstString(t, value, false);
        ir.Ir.getInstance().AddIrCommand(cmd);
        return t;
    }
}