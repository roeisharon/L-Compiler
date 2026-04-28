package ast;
import symboltable.SymbolTable;
import types.*;

public class AstCField extends AstNode {
    public AstDec dec;
    public int lineNumber;

    public AstCField(AstDec dec,int lineNumber) {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.dec = dec;
        this.lineNumber = lineNumber;
    }
    public void printMe() {
        System.out.print("AST_CFIELD\n");

        if (dec != null) dec.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "CFIELD");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        AstGraphviz.getInstance().logEdge(serialNumber, dec.serialNumber);
    }
    public Type semantMe() {
        if (dec == null) return null;

        Type t = dec.semantMe();
        if (t == null || t.isError()) return t;

        SymbolTable s = SymbolTable.getInstance();
        String fieldName = null;
        if (dec instanceof AstVarDec) {
            fieldName = ((AstVarDec) dec).name;
        } else if (dec instanceof AstFuncDec) {
            fieldName = ((AstFuncDec) dec).name;
        }
        // Record the class member immediately so later fields/methods can see it 
        TypeClassVarDec field = new TypeClassVarDec(t, fieldName);
        SymbolTable.getInstance().currClass.dataMembers.insert(field);
        return field;

       
        
       
    }


}
