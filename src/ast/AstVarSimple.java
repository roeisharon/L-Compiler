package ast;
import types.*;
import symboltable.SymbolTable;
public class AstVarSimple extends AstVar
{
	/************************/
	/* simple variable name */
	/************************/
	public String name;
	public int lineNumber;
	private String uniqueVarId;  // Store unique identifier (set during lookup)
	public boolean isClassField = false;
	public String ownerClassName = null;
	public int fieldOffsetWords = -1;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSimple(String name, int lineNumber)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
	
		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== var -> ID( %s )\n",name);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.name = name;
		this.lineNumber = lineNumber;
	}

	/**************************************************/
	/* The printing message for a simple var AST node */
	/**************************************************/
	public void printMe()
	{
		/**********************************/
		/* AST NODE TYPE = AST SIMPLE VAR */
		/**********************************/
		System.out.format("AST NODE SIMPLE VAR( %s )\n",name);

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("SIMPLE\nVAR\n(%s)",name));
	}
	public Type semantMe(){
		SymbolTable s = SymbolTable.getInstance();
		Type t = s.find(name);
		if (t == null) {
			return new TypeError(lineNumber, String.format("Undefined variable '%s'", name));
		}
		types.TypeClassVarDec classField = null;
		if (s.currClass != null) {
			classField = s.currClass.findInClass(name);
		}
		
		// Store unique identifier for later use in irMe()
		symboltable.SymbolTableEntry entry = s.findEntry(name);
		if (entry != null) {
			if (classField != null && entry.getScopeDepth() == s.getClassScopeDepth()) {
				this.uniqueVarId = name;
				this.isClassField = true;
				this.ownerClassName = s.currClass.name;
				this.fieldOffsetWords = s.currClass.getFieldOffset(name);
			} else {
				this.uniqueVarId = name + "#" + entry.uniqueindex;
				this.isClassField = false;
			}
		} else {
			this.uniqueVarId = name;  // Fallback
			if (classField != null) {
				this.isClassField = true;
				this.ownerClassName = s.currClass.name;
				this.fieldOffsetWords = s.currClass.getFieldOffset(name);
			}
		}
		
		return t;
	}
	
	/***********************************************/
	/* Helper method to get variable name for IR   */
	/***********************************************/
	public String getVarName() {
		return name;
	}
	
	/***********************************************/
	/* Get unique identifier for variable (name#index) */
	/***********************************************/
	public String getUniqueVarId() {
		// Use stored unique ID from semantMe() if available
		if (uniqueVarId != null) {
			return uniqueVarId;
		}
		
		// Fallback: try to look up in symbol table
		SymbolTable s = SymbolTable.getInstance();
		symboltable.SymbolTableEntry entry = s.findEntry(name);
		if (entry != null) {
			return name + "#" + entry.uniqueindex;
		}
		// Last resort: use name
		return name;
	}
}
