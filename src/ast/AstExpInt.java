package ast;

import types.*;
import symboltable.SymbolTable;

public class AstExpInt extends AstExp
{
	public int value;
	public int lineNumber;
	public boolean isGlobal = false;
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpInt(int value,int lineNumber)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== exp -> INT( %d )\n", value);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.value = value;
		this.lineNumber = lineNumber;
	}

	/************************************************/
	/* The printing message for an int exp AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST INT EXP */
		/*******************************/
		System.out.format("AST NODE INT( %d )\n",value);

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("INT(%d)",value));
	}
	public Type semantMe() {
		SymbolTable s = SymbolTable.getInstance();
		if (s.isGlobalScope()) {
            this.isGlobal = true;
        }
		return TypeInt.getInstance();
	}
	
	public temp.Temp irMe() {
		temp.Temp t = temp.TempFactory.getInstance().getFreshTemp();
		ir.IRcommandConstInt cmd = new ir.IRcommandConstInt(t, value, this.isGlobal);
		ir.Ir.getInstance().AddIrCommand(cmd);
		return t;
	}
}
