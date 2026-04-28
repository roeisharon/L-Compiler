package ast;
import types.*;
import temp.Temp;

public abstract class AstNode
{
	/*******************************************/
	/* The serial number is for debug purposes */
	/* In particular, it can help in creating  */
	/* a graphviz dot format of the AST ...    */
	/*******************************************/
	public int serialNumber;
	
	/***********************************************/
	/* The default message for an unknown AST node */
	/***********************************************/
	public void printMe()
	{
		System.out.print("AST NODE UNKNOWN\n");
	}
	public Type semantMe(){return null;}
	
	/***********************************************/
	/* IR generation method - returns Temp for    */
	/* expressions, null for statements           */
	/***********************************************/
	public Temp irMe(){return null;}
}
