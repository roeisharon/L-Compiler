package ast;
import symboltable.SymbolTable;
import types.*;
public class AstProgram extends AstNode {
    public AstDecList decList;
    public int lineNumber;

    /******************/
    /* CONSTRUCTOR(S) */

    /******************/
    public AstProgram(AstDecList decList, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.format("Program -> [decList]+\n");

        /*******************************/
        /* COPY INPUT DATA NENBERS ... */
        /*******************************/
        this.decList = decList;
        this.lineNumber = lineNumber;
    }

    public void printMe() {

        System.out.format("AST_PROGRAM\n");


        decList.printMe();
        /*********************************/
        /* Print to AST GRAPHIZ DOT file */
        /*********************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("START OF PROGRAM"));

        AstGraphviz.getInstance().logEdge(serialNumber, decList.serialNumber);
    }
    public Type semantMe(){
        System.out.println("Entering AstProgram.semantMe, decList=" + (decList==null?"null":"not-null"));
        // First pass: pre-semant for top-level declarations so globals
        // declared after functions are visible during function/body checks.
        if (decList != null) {
            /* for (AstDecList it = decList; it != null; it = it.tail) {
                System.out.println("Pre-semant loop visiting dec node: " + (it.head==null?"null":it.head.getClass().getSimpleName()));
                if (it.head != null) it.head.preSemant();
            } */
            Type t = decList.semantMe();
            if (t == null || t.isError()) return t;
        }

        Type mainType = SymbolTable.getInstance().find("main");
        if (mainType == null || !mainType.isFunc()) {
            return new TypeError(lineNumber, "Program must define function: void main()");
        }
        TypeFunction mainFunc = (TypeFunction) mainType;
        if (mainFunc.t != TypeVoid.getInstance() || mainFunc.params != null) {
            return new TypeError(lineNumber, "Program entry point must be: void main()");
        }

        return TypeVoid.getInstance();
    }
    public temp.Temp irMe() {
        if (decList != null) {
            System.out.println("IR PROGRAM");
            decList.irMe();
        }
        return null;
    }
     
}