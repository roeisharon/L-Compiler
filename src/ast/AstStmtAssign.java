package ast;
import symboltable.SymbolTable;
import types.*;

public class AstStmtAssign extends AstStmt
{
	/***************/
	/*  var := exp */
	/***************/
	public AstVar var;
	public AstExp exp;
	public int lineNumber;
	public boolean isGlobal = false;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssign(AstVar var, AstExp exp, int lineNumber)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== stmt -> var ASSIGN exp SEMICOLON\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
		this.lineNumber = lineNumber;
	}

	/*********************************************************/
	/* The printing message for an assign statement AST node */
	/*********************************************************/
	public void printMe()
	{
		/********************************************/
		/* AST NODE TYPE = AST ASSIGNMENT STATEMENT */
		/********************************************/
		System.out.print("AST NODE ASSIGN STMT\n");

		/***********************************/
		/* RECURSIVELY PRINT VAR + EXP ... */
		/***********************************/
		if (var != null) var.printMe();
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"ASSIGN\nleft := right\n");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	public Type semantMe()
	{
		SymbolTable s = SymbolTable.getInstance();
		if (var == null || exp == null) return null;
		Type var_type = var.semantMe();
		if(var_type == null || var_type.isError()) return var_type;
		Type exp_type = exp.semantMe();
		if(exp_type == null || exp_type.isError()) return exp_type;
		System.out.println(var_type.toString() + "   " + exp_type.toString());

		if(!s.canAssign(var_type, exp_type))
		{
			
			return new TypeError(lineNumber, String.format("Can't assign"));
		}
		 if (s.isGlobalScope()) {
            this.isGlobal = true;
        }
		
		return TypeVoid.getInstance();
	}
	
	public temp.Temp irMe() {
		if (var == null || exp == null) return null;

		if (var instanceof AstVarSubscript) {
			AstVarSubscript sub = (AstVarSubscript) var;
			temp.Temp arrayTemp = (sub.var != null) ? new AstExpVar(sub.var, this.lineNumber).irMe() : null;
			temp.Temp indexTemp = (sub.subscript != null) ? sub.subscript.irMe() : null;
			if (arrayTemp == null || indexTemp == null) return null;
			temp.Temp expTemp = exp.irMe();
			if (expTemp == null) return null;
			ir.Ir.getInstance().AddIrCommand(new ir.IrCommandArrayStore(arrayTemp, indexTemp, expTemp));
			return null;
		}

		if (var instanceof AstVarField) {
			AstVarField fld = (AstVarField) var;
			types.TypeClass cls = (types.TypeClass) symboltable.SymbolTable.getInstance().findClass(fld.ownerClassName);
			if (cls == null) return null;
			int offset = cls.getFieldOffset(fld.fieldName);
			if (offset < 0) return null;

			temp.Temp objTemp = (fld.var != null) ? new AstExpVar(fld.var, this.lineNumber).irMe() : null;
			if (objTemp == null) return null;

			temp.Temp expTemp = exp.irMe();
			if (expTemp == null) return null;
			ir.Ir.getInstance().AddIrCommand(new ir.IrCommandFieldStore(objTemp, expTemp, offset));
			return null;
		}

		temp.Temp expTemp = exp.irMe();
		if (expTemp == null) return null;

		if (var instanceof AstVarSimple) {
			AstVarSimple sv = (AstVarSimple) var;
			if (sv.isClassField && sv.fieldOffsetWords >= 0) {
				temp.Temp selfTemp = temp.TempFactory.getInstance().getFreshTemp();
				ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLoad(selfTemp, "__self", "__self", false));
				ir.Ir.getInstance().AddIrCommand(new ir.IrCommandFieldStore(selfTemp, expTemp, sv.fieldOffsetWords));
				return null;
			}
		}
		
		String varName = var.getVarName();
		if (varName == null) return null;
		String uniqueVarId = var.getUniqueVarId();
		if (uniqueVarId == null) uniqueVarId = varName;
		
		ir.IrCommandStore cmd = new ir.IrCommandStore(varName, uniqueVarId, expTemp, this.isGlobal);
		ir.Ir.getInstance().AddIrCommand(cmd);
		return null;
	}
}
