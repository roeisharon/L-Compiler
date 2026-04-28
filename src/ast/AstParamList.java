package ast;
import symboltable.SymbolTable;
import types.*;

public class AstParamList extends AstNode {
    public AstParam head;
    public AstParamList tail;
    public int lineNumber;

    public AstParamList(AstParam head, AstParamList tail, int lineNumber) {
        this.head = head;
        this.tail = tail;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        if (tail != null) System.out.print("paramList -> param paramList\n");
        if (tail == null) System.out.print("paramList -> param\n");
    }

    public void printMe() {
        System.out.format("AST_PARAM_LIST\n");
        if (head != null) head.printMe();
        if (tail != null) tail.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "PARAM_LIST");

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
    public Type semantMe(){
        if (head == null) {
            return new TypeList(null, null);
        }
        Type headType = head.semantMe();
        if (headType == null || headType.isError()) return headType;
        SymbolTable s = SymbolTable.getInstance();
        if(s.existsInScope(head.name)){
            return new TypeError(lineNumber, String.format("Duplicate parameter name '%s'", head.name));
        }
        s.enter(head.name, headType);
        Type tail_type=null;

        if (tail != null){
            tail_type = tail.semantMe();
            if(tail_type.isError()) return new TypeError(lineNumber, ((TypeError)tail_type).msg);
            if(tail_type == null || tail_type.isError()) return new TypeError(lineNumber, "Error in tail parameters");
        }

        else{
            return new TypeList(headType, null);
        }
        return new TypeList(headType, (TypeList)tail_type);
    }
    
    public temp.Temp irMe() {
        // Re-enter parameters into symbol table (mirroring semantMe)
        if (head != null) {
            head.irMe();
        }
        if (tail != null) {
            tail.irMe();
        }
        return null;
    }
}