package ast;
import symboltable.SymbolTable;
import types.*;

public class AstStmtIf extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;
	public int lineNumber;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtIf(AstExp cond, AstStmtList body, int lineNumber)
	{
		this.cond = cond;
		this.body = body;
		this.lineNumber = lineNumber;
		serialNumber = AstNodeSerialNumber.getFresh();
		System.out.format("stmtIf -> IF EXP STMTLIST\n");
	}
	public void printMe()
	{
		System.out.print("AST_STMT_IF\n");

		if (cond != null) cond.printMe();
		if (body != null) body.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				"IF_STMT");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (cond != null) {
			AstGraphviz.getInstance().logEdge(serialNumber, cond.serialNumber);
		}
		if (body != null) {
			AstGraphviz.getInstance().logEdge(serialNumber, body.serialNumber);
		}
	}

	public Type semantMe(){
		SymbolTable s = SymbolTable.getInstance();
		if(cond==null){
			return new TypeError(lineNumber, "Internal error: if-condition is null");
		}
		if(body==null){
			throw new RuntimeException(
		"NULL returned from semantMe in AstStmtIf bodyisnull: " +
		body.getClass().getSimpleName());
		}
		if (cond == null || body == null) return null;
		Type condType = cond.semantMe();
		if(condType == null){
			throw new RuntimeException(
		"NULL returned from semantMe in AstStmtIf cond: " +
		cond.getClass().getSimpleName());
		}
		if (condType == null || condType.isError()) return condType;
		if(!condType.isInt()) return new TypeError(lineNumber);
		s.beginScope();
		Type body_type = body.semantMe();
		if(body_type == null){
			throw new RuntimeException(
		"NULL returned from semantMe in AstStmtIf body: " +
		body.getClass().getSimpleName());
		}
		if(body_type == null || body_type.isError()) return body_type;
		s.endScope();
		return TypeVoid.getInstance();

	}
	
	public temp.Temp irMe() {
		if (cond == null || body == null) return null;
		
		
		
		String labelEnd = ir.IrCommand.getFreshLabel("if_end");
		
		// Evaluate condition
		temp.Temp condTemp = cond.irMe();
		if (condTemp == null) return null;
		
		// Jump to end if condition is false (0)
		ir.IrCommandJumpIfEqToZero jumpCmd = new ir.IrCommandJumpIfEqToZero(condTemp, labelEnd);
		ir.Ir.getInstance().AddIrCommand(jumpCmd);
		
		
		
		
		// Generate body
		body.irMe();
		
		
		
		
		// End label
		ir.IrCommandLabel labelCmd = new ir.IrCommandLabel(labelEnd);
		ir.Ir.getInstance().AddIrCommand(labelCmd);
		
		return null;
	}
}