package ast;
import symboltable.SymbolTable;
import types.*;

public class AstExpVar extends AstExp
{
	public AstVar var;
	public int lineNumber;
	public boolean isGlobal = false;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVar(AstVar var,int lineNumber)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> var\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.lineNumber = lineNumber;
	}
	
	/***********************************************/
	/* The default message for an exp var AST node */
	/***********************************************/
	public void printMe()
	{
		/************************************/
		/* AST NODE TYPE = EXP VAR AST NODE */
		/************************************/
		System.out.print("AST NODE EXP VAR\n");

		/*****************************/
		/* RECURSIVELY PRINT var ... */
		/*****************************/
		if (var != null) var.printMe();
		
		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"EXP\nVAR");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
			
	}
	public Type semantMe() {
		SymbolTable s = SymbolTable.getInstance();
		this.isGlobal = s.isGlobalScope();
		System.out.println("isGlobal in AstExpVar: " + this.isGlobal);
		if (var == null) return null;
		return var.semantMe();
	}
	
	public temp.Temp irMe() {
		if (var == null) return null;

		if (var instanceof AstVarSubscript) {
			AstVarSubscript sub = (AstVarSubscript) var;
			temp.Temp arrayTemp = (sub.var != null) ? new AstExpVar(sub.var, this.lineNumber).irMe() : null;
			temp.Temp indexTemp = (sub.subscript != null) ? sub.subscript.irMe() : null;
			if (arrayTemp == null || indexTemp == null) return null;

			temp.Temp dst = temp.TempFactory.getInstance().getFreshTemp();
			ir.Ir.getInstance().AddIrCommand(new ir.IrCommandArrayLoad(dst, arrayTemp, indexTemp));
			return dst;
		}

		if (var instanceof AstVarField) {
			AstVarField fld = (AstVarField) var;
			temp.Temp objTemp = (fld.var != null) ? new AstExpVar(fld.var, this.lineNumber).irMe() : null;
			if (objTemp == null) return null;

			types.TypeClass cls = (types.TypeClass) symboltable.SymbolTable.getInstance().findClass(fld.ownerClassName);
			if (cls == null) return null;
			int offset = cls.getFieldOffset(fld.fieldName);
			if (offset < 0) return null;

			temp.Temp dst = temp.TempFactory.getInstance().getFreshTemp();
			ir.Ir.getInstance().AddIrCommand(new ir.IrCommandFieldLoad(dst, objTemp, offset));
			return dst;
		}

		if (var instanceof AstVarSimple) {
			AstVarSimple sv = (AstVarSimple) var;
			if (sv.isClassField && sv.fieldOffsetWords >= 0) {
				temp.Temp selfTemp = temp.TempFactory.getInstance().getFreshTemp();
				ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLoad(selfTemp, "__self", "__self", false));
				temp.Temp dst = temp.TempFactory.getInstance().getFreshTemp();
				ir.Ir.getInstance().AddIrCommand(new ir.IrCommandFieldLoad(dst, selfTemp, sv.fieldOffsetWords));
				return dst;
			}
		}

		String varName = var.getVarName();
		if (varName == null) return null;
		String uniqueVarId = var.getUniqueVarId();
		if (uniqueVarId == null) uniqueVarId = varName;
		
		temp.Temp t = temp.TempFactory.getInstance().getFreshTemp();
		ir.IrCommandLoad cmd = new ir.IrCommandLoad(t, varName, uniqueVarId,this.isGlobal);
		ir.Ir.getInstance().AddIrCommand(cmd);
		return t;
	}
}
