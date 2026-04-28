package ast;
import symboltable.SymbolTable;
import types.*;

public class AstCFieldList extends AstNode {
    public AstCField head;
    public AstCFieldList tail;
    public int lineNumber;

    public AstCFieldList(AstCField head, AstCFieldList tail,int lineNumber) {
        this.head = head;
        this.tail = tail;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        if (tail != null) System.out.print("cFieldList -> cfield cFieldList\n");
        if (tail == null) System.out.print("cFieldList -> cfield\n");
    }
     public void printMe() {
        System.out.print("AST_CFIELD_LIST\n");

        if (head != null) head.printMe();
        if (tail != null) tail.printMe();
        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "CFIELD_LIST");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (head != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, head.serialNumber);
        }
        if (tail != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, tail.serialNumber);
        }
    }
    public Type semantMe() {
        /* case: empty list */
        if (head == null) return null;

        Type headType = head.semantMe();
        if (headType == null || headType.isError()) return headType;
        if (!(headType instanceof TypeClassVarDec)) {
            return new TypeError(lineNumber, "Internal Error: Class Field did not evaluate to a ClassVarDec");
        }
       
        if (tail != null) {
            Type tailType = tail.semantMe();
            if (tailType == null || tailType.isError()) return tailType;
            if (!(tailType instanceof TypeClassVarDecList)) {
                return new TypeError(lineNumber, "Internal Error: Class Field List did not evaluate to a ClassVarDecList");
            }
            
        }
        return SymbolTable.getInstance().currClass.dataMembers;
    }

}