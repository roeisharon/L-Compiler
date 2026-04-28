package ast;
import types.*;

public class AstExpList extends AstNode {
    public AstExp head;
    public AstExpList tail;
    public int lineNumber;

    public AstExpList(AstExp head, AstExpList tail,int lineNumber) {
        this.head = head;
        this.tail = tail;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        if (tail != null) System.out.print("expList -> exp expList\n");
        if (tail == null) System.out.print("expList -> exp      \n");

    }
    public void printMe() {
        System.out.print("AST_EXP_LIST\n");

        if (head != null) head.printMe();
        if (tail != null) tail.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "EXP_LIST");

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
        if (head == null){
            return new TypeList(null, null);
        }
        Type headType = null;
        if (head != null) {
            headType = head.semantMe();
            if (headType == null || headType.isError()) return headType;
        }

        TypeList tailTypes = null;
        if (tail != null) {
            Type t = tail.semantMe();
            if (t == null || t.isError()) return t;
            tailTypes = (TypeList)t;
        }
        else{
            return new TypeList(headType, null);
        }

        return new TypeList(headType, tailTypes);
    }

}
