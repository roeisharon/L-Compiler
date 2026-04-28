package types;

import symboltable.SymbolTable;

public class TypeFunction extends TypeClassVarDec
{
	/***********************************/
	/* The return type of the function */
	/***********************************/
	

	/*************************/
	/* types of input params */
	/*************************/
	public TypeList params;
	
	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeFunction(Type returnType, String name, TypeList params)
	{
		super(returnType, name);
		this.params = params;
	}
	
	public boolean argsSameType(TypeList lst, boolean override)
	{
		System.out.println("Comparing passed args and function args types");
		TypeList params = this.params;
		while(params != null && lst != null)
		{
			if(override)
			{
				if(!(params.head == lst.head)) return false;
			}
			else
			{
				if(!(SymbolTable.getInstance().canAssign(params.head, lst.head))) return false;
			}
			params = params.tail;
			lst = lst.tail;
		}
		return (params == null && lst == null);
	}

	public boolean isFunc() {
		return true;
	}
}
