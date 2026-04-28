/***********/
/* PACKAGE */
/***********/
package symboltable;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;

/**********************/
/* SYMBOL TABLE ENTRY */
/**********************/
public class SymbolTableEntry
{
	public int uniqueindex=0;
	/*********/
	/* index */
	/*********/
	public int index;
	
	/********/
	/* name */
	/********/
	public String name;

	/******************/
	/* TYPE value ... */
	/******************/
	public Type type;

	public boolean newable=false ;

	/*********************************************/
	/* prevtop and next symbol table entries ... */
	/*********************************************/
	public SymbolTableEntry prevtop;
	public SymbolTableEntry next;

	/****************************************************/
	/* The prevtopIndex is just for debug purposes ... */
	/****************************************************/
	public int prevtopIndex;
	int scope_depth;

	public int getScopeDepth()
	{
		return scope_depth;
	}
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public SymbolTableEntry(
		String name, boolean newable,
		Type type,
		int index,
		SymbolTableEntry next, int scope_depth,
		SymbolTableEntry prevtop,
		int prevtopIndex)
	{
		this.index = index;
		this.name = name;
		this.newable=newable ;
		this.scope_depth=scope_depth ;
		this.type = type;
		this.next = next;
		this.prevtop = prevtop;
		this.prevtopIndex = prevtopIndex;
	}

	public SymbolTableEntry(
		String name, 
		Type type,
		int index,
		SymbolTableEntry next, int scope_depth,
		SymbolTableEntry prevtop,
		int prevtopIndex)
	{
		this.index = index;
		this.name = name;
		
		this.scope_depth=scope_depth ;
		this.type = type;
		this.next = next;
		this.prevtop = prevtop;
		this.prevtopIndex = prevtopIndex;
	}
}
