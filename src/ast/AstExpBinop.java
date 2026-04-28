package ast;
import symboltable.SymbolTable;
import types.*;

public class AstExpBinop extends AstExp
{
	int op;
	public AstExp left;
	public AstExp right;
	public int lineNumber;
	public boolean isGlobal = false;
	private boolean isStringConcat = false;
	private boolean isStringEquality = false;
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpBinop(AstExp left, AstExp right, int op,int lineNumber)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> exp BINOP exp\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.left = left;
		this.right = right;
		this.op = op;
		this.lineNumber = lineNumber;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe()
	{
		String sop="";
		
		/*********************************/
		/* CONVERT op to a printable sop */
		/*********************************/
		if (op == 0) {sop = "+";}
		if (op == 1) {sop = "-";}
		if (op == 2) {sop = "*";}
		if (op == 3) {sop = "/";}
		if (op == 4) {sop = "<";}
		if (op == 5) {sop = ">";}
		if (op == 6) {sop = "=";}
		
		
		/*************************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*************************************/
		System.out.print("AST NODE BINOP EXP\n");

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (left != null) left.printMe();
		if (right != null) right.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("BINOP(%s)",sop));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (left  != null) AstGraphviz.getInstance().logEdge(serialNumber,left.serialNumber);
		if (right != null) AstGraphviz.getInstance().logEdge(serialNumber,right.serialNumber);
	}

	public Type semantMe() {
		SymbolTable s = SymbolTable.getInstance();
		isGlobal = s.isGlobalScope();
		if (left == null || right == null) {
			return new TypeError(lineNumber, "Missing operand(s) in binary operation");
		}
		Type l = left.semantMe();
		Type r = right.semantMe();

		if (l == null || l.isError()) return l;
		if (r == null || r.isError()) return r;



		if((l==TypeInt.getInstance()) && (r==TypeInt.getInstance())) {
			if(op==3 &&right instanceof AstExpInt ri && ri.value==0) {
				return new TypeError(lineNumber, "Division by zero");
			}
			return TypeInt.getInstance();
		} 
		if((l==TypeString.getInstance()) && (r==TypeString.getInstance())){
			if(op==0) {
				isStringConcat = true;
				return TypeString.getInstance();
			}
			if(op==6) {
				isStringEquality = true;
				return TypeInt.getInstance();
			}
		}
		if(op==6){
			if(s.canAssign(l, r) || s.canAssign(r, l)|| (l==TypeVoid.getInstance() && r==TypeNil.getInstance()) ){
				return TypeInt.getInstance();
			}
		}
		return new TypeError(lineNumber, "Type mismatch in binary operation");

	}
	
	public temp.Temp irMe() {
		if (left == null || right == null) return null;
		
		temp.Temp leftTemp = left.irMe();
		temp.Temp rightTemp = right.irMe();
		if (leftTemp == null || rightTemp == null) return null;
		temp.Temp dst = temp.TempFactory.getInstance().getFreshTemp();
		
		ir.IrCommand cmd = null;
		switch (op) {
			case 0: // +
				if (isStringConcat) {
					cmd = new ir.IrCommandStringConcat(dst, leftTemp, rightTemp, this.isGlobal);
				} else {
					cmd = new ir.IrCommandBinopAddIntegers(dst, leftTemp, rightTemp, this.isGlobal);
				}
				break;
			case 2: // *
				cmd = new ir.IrCommandBinopMulIntegers(dst, leftTemp, rightTemp, this.isGlobal);
				break;
			case 4: // <
				cmd = new ir.IrCommandBinopLtIntegers(dst, leftTemp, rightTemp, this.isGlobal);
				break;
			case 6: // =
				if (isStringEquality) {
					cmd = new ir.IrCommandBinopEqStrings(dst, leftTemp, rightTemp, this.isGlobal);
				} else {
					cmd = new ir.IrCommandBinopEqIntegers(dst, leftTemp, rightTemp, this.isGlobal);
				}
				break;
			case 1: // - (subtraction)
				cmd = new ir.IrCommandBinopSubIntegers(dst, leftTemp, rightTemp, this.isGlobal);
				break;
			case 3: // / (division)
				cmd = new ir.IrCommandBinopDivIntegers(dst, leftTemp, rightTemp, this.isGlobal);
				break;
			case 5: // > (greater than)
				// Can be handled by swapping operands of <
				cmd = new ir.IrCommandBinopLtIntegers(dst, rightTemp, leftTemp, this.isGlobal);
				break;
			default:
				return null;
		}
		
		if (cmd != null) {
			ir.Ir.getInstance().AddIrCommand(cmd);
		}
		return dst;
	}
}
