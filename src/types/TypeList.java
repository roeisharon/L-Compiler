package types;

import symboltable.SymbolTable;

public class TypeList extends Type
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public Type head;
	public TypeList tail;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public TypeList(Type head, TypeList tail)
	{
		this.head = head;
		this.tail = tail;
	}
	public void insert(Type elem)
	{
		if(head == null)
		{
			
			head = elem;
			return;
		}

		if(tail != null)
			tail.insert(elem);
		else
		{
		
			this.tail = new TypeList(elem, null);
		}
	}
	public boolean compare(TypeList t)
	{
		if(t.head == null || this.head == null) return t.head == this.head;
		if(t.tail == null || this.tail == null) return SymbolTable.getInstance().canAssign(t.head, this.head);
		if(SymbolTable.getInstance().canAssign(t.head, this.head))	return this.tail.compare(t.tail);
		return false;
	}
	public boolean isList() {
		return true;
	}	

}
