package ast;
import types.*;

public class AstVarField extends AstVar
{
	public AstVar var;
	public String fieldName;
	public int lineNumber;
	public String ownerClassName;
	
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarField(AstVar var, String fieldName, int lineNumber)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== var -> var DOT ID( %s )\n",fieldName);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.fieldName = fieldName;
		this.lineNumber = lineNumber;
	}

	/*************************************************/
	/* The printing message for a field var AST node */
	/*************************************************/
	public void printMe()
	{
		/*********************************/
		/* AST NODE TYPE = AST FIELD VAR */
		/*********************************/
		System.out.print("AST NODE FIELD VAR\n");

		/**********************************************/
		/* RECURSIVELY PRINT VAR, then FIELD NAME ... */
		/**********************************************/
		if (var != null) var.printMe();
		System.out.format("FIELD NAME( %s )\n",fieldName);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("FIELD\nVAR\n...->%s",fieldName));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
	}
	public Type semantMe(){
		Type t = var.semantMe();
		if (t == null || t.isError()) return t;

		if (!t.isClass()) {
			return new TypeError(lineNumber,
				String.format("Accessing field '%s' of non-class variable", fieldName));
		}

		TypeClass cls = (TypeClass) t;
		this.ownerClassName = cls.name;
		TypeClassVarDec dec = cls.findInClass(fieldName);

		if (dec == null) {
			return new TypeError(lineNumber,
				String.format("Class '%s' has no field named '%s'",
							cls.name, fieldName));
		}

		return dec.t;	
	}
}
