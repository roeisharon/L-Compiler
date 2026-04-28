package ast;
import types.*;


public class AstDecList extends AstNode {
    public AstDec head;
	public AstDecList tail;
    public int lineNumber;

    public AstDecList(AstDec head, AstDecList tail,int lineNumber) {
        this.head = head;
        this.tail = tail;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
         if (tail != null) System.out.print("declist -> dec declist\n");
        else System.out.print("declist -> dec\n");
    }
    
     public void printMe() {
        System.out.print("AST_DEC_LIST\n");

        if (head != null) head.printMe();
        if (tail != null) tail.printMe();
        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "DEC_LIST");

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
        if (head != null) {
			Type t = head.semantMe();
			if(t==null){
				throw new RuntimeException(
		"NULL returned from semantMe in AstDecList head: " +
		head.getClass().getSimpleName());
			}
			if (t == null || t.isError()) {
				return t;
			}
		}
		if (tail != null) {
			Type t = tail.semantMe();
			if(t==null){
				throw new RuntimeException(
		"NULL returned from semantMe in AstStmtList tail: " +
		tail.getClass().getSimpleName());
			}
			if (t == null || t.isError()) {
				return t;
			}
		}
		return TypeVoid.getInstance();
    }
    
    public temp.Temp irMe() {
        System.out.println("IR DEC_LIST");
        if (head != null) head.irMe();
        if (tail != null) tail.irMe();
        return null;
    }
}
