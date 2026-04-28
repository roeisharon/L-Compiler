package ast;
import types.*;
public class AstVarSubscript extends AstVar
{
	public AstVar var;
	public AstExp subscript;
	public int lineNumber;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSubscript(AstVar var, AstExp subscript, int lineNumber)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== var -> var [ exp ]\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.subscript = subscript;
		this.lineNumber = lineNumber;
	}

	/*****************************************************/
	/* The printing message for a subscript var AST node */
	/*****************************************************/
	public void printMe()
	{
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE SUBSCRIPT VAR\n");

		/****************************************/
		/* RECURSIVELY PRINT VAR + SUBSCRIPT ... */
		/****************************************/
		if (var != null) var.printMe();
		if (subscript != null) subscript.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"SUBSCRIPT\nVAR\n...[...]");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var       != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		if (subscript != null) AstGraphviz.getInstance().logEdge(serialNumber,subscript.serialNumber);
	}
	public Type semantMe(){
		Type t = var.semantMe();
		if (t == null || t.isError()) return t;

		if (!t.isArray()) {
			return new TypeError(lineNumber,
				"Attempting array access on non-array type");
		}

		TypeArray arr = (TypeArray) t;

		Type indexType = subscript.semantMe();
		if (indexType == null || indexType.isError()) return indexType;
		if (indexType != TypeInt.getInstance()) {
			return new TypeError(lineNumber,
				"Array index must be of type int");
		}
		if(subscript instanceof AstExpInt && ((AstExpInt)subscript).value < 0) {
            return new TypeError(lineNumber, "subscript cannot be negative");
        }

		return arr.arrayType;
	}
}
