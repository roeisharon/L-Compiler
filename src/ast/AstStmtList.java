package ast;
import types.*;

public class AstStmtList extends AstStmt
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstStmt head;
	public AstStmtList tail;
	public int lineNumber;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstStmtList(AstStmt head, AstStmtList tail, int lineNumber)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		if (tail != null) System.out.print("====================== stmts -> stmt stmts\n");
		if (tail == null) System.out.print("====================== stmts -> stmt      \n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.head = head;
		this.tail = tail;
		this.lineNumber = lineNumber;
	}

	/******************************************************/
	/* The printing message for a statement list AST node */
	/******************************************************/
	public void printMe()
	{
		/**************************************/
		/* AST NODE TYPE = AST STATEMENT LIST */
		/**************************************/
		System.out.print("AST NODE STMT LIST\n");

		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		if (head != null) head.printMe();
		if (tail != null) tail.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"STMT\nLIST\n");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (head != null) AstGraphviz.getInstance().logEdge(serialNumber,head.serialNumber);
		if (tail != null) AstGraphviz.getInstance().logEdge(serialNumber,tail.serialNumber);
	}
	public Type semantMe() {
		if (head != null) {
			Type t = head.semantMe();
			if(t==null){
				throw new RuntimeException(
		"NULL returned from semantMe in AstStmtList head: " +
		head.getClass().getSimpleName());
			}
			if ( t.isError()) {
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
			if (t.isError()) {
				return t;
			}
		}
		return TypeVoid.getInstance();
	}
	
	public temp.Temp irMe() {
		if (head != null) head.irMe();
		if (tail != null) tail.irMe();
		return null;
	}
}
