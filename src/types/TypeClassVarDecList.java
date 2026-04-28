package types;

public class TypeClassVarDecList extends Type
{
	public TypeClassVarDec head;
	public TypeClassVarDecList tail;
	
	public TypeClassVarDecList(TypeClassVarDec head, TypeClassVarDecList tail)
	{
		this.head = head;
		this.tail = tail;
	}	
	public TypeClassVarDec find(String name) {
        if (head == null) {
            return null;
        }
        if (tail != null){
            if (head.name.equals(name)) {
                return head;
            } 
            TypeClassVarDec res = tail.find(name);
            return res;
        }
        if (head.name.equals(name)) {
            return head;
        }
        return null;
    }
	public void insert(TypeClassVarDec field) {
        if (head == null)
        {
            System.out.println(String.format("Inserting field %s of type %s in head", field.name, field.t.name));
			head = field;
			return;
        }
        if (tail != null) {
            tail.insert(field);
        } 
        else {
            this.tail = new TypeClassVarDecList(field, null);
        }
    }

}


