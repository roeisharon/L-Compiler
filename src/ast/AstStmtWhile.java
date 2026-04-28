package ast;
import symboltable.SymbolTable;
import types.*;

public class AstStmtWhile extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;
	public int lineNumber;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtWhile(AstExp cond, AstStmtList body, int lineNumber)
	{
		this.cond = cond;
		this.body = body;
		this.lineNumber = lineNumber;
		serialNumber = AstNodeSerialNumber.getFresh();
		System.out.format("stmtWhile -> WHILE EXP STMTLIST\n");
	}
	public void printMe()
	{
		System.out.print("AST_STMT_WHILE\n");

		if (cond != null) cond.printMe();
		if (body != null) body.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				"WHILE_STMT");

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
	public Type semantMe()
	{
		SymbolTable s = SymbolTable.getInstance();
		if (cond == null) return null;
		Type condType = cond.semantMe();
		if (condType == null || condType.isError()) return condType;
		if (!condType.isInt()) return new TypeError(lineNumber, "while condition is not an integer");
		s.beginScope();
		if (body == null) {
			return null;
		}
		Type bodyType = body.semantMe();
		s.endScope();
		if (bodyType == null || bodyType.isError()) return bodyType;
		return TypeVoid.getInstance();
	}
	
	public temp.Temp irMe() {
		if (cond == null || body == null) return null;
		
		
		
		String labelStart = ir.IrCommand.getFreshLabel("while_start");
		String labelEnd = ir.IrCommand.getFreshLabel("while_end");
		
		// Start label (loop entry point)
		ir.IrCommandLabel startLabel = new ir.IrCommandLabel(labelStart);
		ir.Ir.getInstance().AddIrCommand(startLabel);
		
		// Evaluate condition
		temp.Temp condTemp = cond.irMe();
		if (condTemp == null) return null;
		
		// Exit loop if condition is false
		ir.IrCommandJumpIfEqToZero jumpCmd = new ir.IrCommandJumpIfEqToZero(condTemp, labelEnd);
		ir.Ir.getInstance().AddIrCommand(jumpCmd);
		
		
		
		// Generate body
		body.irMe();
		
		
		
		// Jump back to start
		ir.IrCommandJumpLabel backJump = new ir.IrCommandJumpLabel(labelStart);
		ir.Ir.getInstance().AddIrCommand(backJump);
		
		// End label
		ir.IrCommandLabel endLabel = new ir.IrCommandLabel(labelEnd);
		ir.Ir.getInstance().AddIrCommand(endLabel);
		
		return null;
	}
}