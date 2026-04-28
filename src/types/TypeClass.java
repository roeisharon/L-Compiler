package types;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TypeClass extends Type
{
	/*********************************************************************/
	/* If this class does not extend a father class this should be null  */
	/*********************************************************************/
	public TypeClass father;

	/**************************************************/
	/* Gather up all data members in one place        */
	/* Note that data members coming from the AST are */
	/* packed together with the class methods         */
	/**************************************************/
	public TypeClassVarDecList dataMembers;
	private LinkedHashMap<String, Integer> intFieldInitializers = new LinkedHashMap<>();
	private LinkedHashMap<String, String> stringFieldInitializers = new LinkedHashMap<>();
	private LinkedHashMap<String, Boolean> nilFieldInitializers = new LinkedHashMap<>();
	private static final LinkedHashMap<String, TypeClass> classesByName = new LinkedHashMap<>();
	private static int nextClassId = 1;
	public final int classId;
	
	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeClass(TypeClass father, String name, TypeClassVarDecList dataMembers)
	{
		this.name = name;
		this.father = father;
		this.dataMembers = dataMembers;
		this.classId = nextClassId++;
		classesByName.put(name, this);
	}

	public static TypeClass getByName(String name)
	{
		return classesByName.get(name);
	}

	public static List<TypeClass> getAllClasses()
	{
		return new ArrayList<>(classesByName.values());
	}

	public boolean isDescendantOf(String ancestorName)
	{
		TypeClass c = this;
		while (c != null)
		{
			if (c.name.equals(ancestorName)) return true;
			c = c.father;
		}
		return false;
	}

	private Type findInThisClassOnly(String memberName)
	{
		for (TypeClassVarDecList it = dataMembers; it != null; it = it.tail)
		{
			if (it.head != null && memberName.equals(it.head.name))
			{
				return it.head.t;
			}
		}
		return null;
	}

	public String findMethodOwnerClassName(String methodName)
	{
		TypeClass c = this;
		while (c != null)
		{
			Type t = c.findInThisClassOnly(methodName);
			if (t != null && t.isFunc()) return c.name;
			c = c.father;
		}
		return null;
	}
	public Type findInClassScope(String name){
		for (TypeClassVarDecList e = this.dataMembers;e!=null && e.head != null;e=e.tail){
			if (e.head.name.equals(name)){
				return e.head.t;
			}
		}
		if (this.father != null)
			return this.father.findInClassScope(name);
		return null;
	}
	public TypeClassVarDec findInClass(String id)
	{
		TypeClass c = this;
		TypeClassVarDec tmp = c.dataMembers.find(id);
		while(tmp == null && c.father != null)
		{
			c = c.father;
			tmp = c.dataMembers.find(id);
		}
		return tmp;
	}
	public boolean fatherOf(TypeClass son)
	{
		while(son != null)
		{
			if(son.name.equals(this.name))
				return true;
			son = son.father;
		}
		return false;
	}
	
	public boolean isClass() {return true;}

	public void setIntFieldInitializer(String fieldName, int value)
	{
		intFieldInitializers.put(fieldName, value);
	}

	public Map<String, Integer> getAllIntFieldInitializers()
	{
		LinkedHashMap<String, Integer> res = new LinkedHashMap<>();
		if (father != null) {
			res.putAll(father.getAllIntFieldInitializers());
		}
		res.putAll(intFieldInitializers);
		return res;
	}

	public void setStringFieldInitializer(String fieldName, String value)
	{
		stringFieldInitializers.put(fieldName, value);
	}

	public Map<String, String> getAllStringFieldInitializers()
	{
		LinkedHashMap<String, String> res = new LinkedHashMap<>();
		if (father != null) {
			res.putAll(father.getAllStringFieldInitializers());
		}
		res.putAll(stringFieldInitializers);
		return res;
	}

	public void setNilFieldInitializer(String fieldName)
	{
		nilFieldInitializers.put(fieldName, Boolean.TRUE);
	}

	public Map<String, Boolean> getAllNilFieldInitializers()
	{
		LinkedHashMap<String, Boolean> res = new LinkedHashMap<>();
		if (father != null) {
			res.putAll(father.getAllNilFieldInitializers());
		}
		res.putAll(nilFieldInitializers);
		return res;
	}

	public int totalFieldCount()
	{
		int count = 0;
		if (father != null) {
			count += father.totalFieldCount();
		}
		for (TypeClassVarDecList it = dataMembers; it != null; it = it.tail) {
			if (it.head != null && !it.head.t.isFunc()) {
				count++;
			}
		}
		return count;
	}

	public int getFieldOffset(String fieldName)
	{
		int base = (father != null) ? father.totalFieldCount() : 0;
		int idx = 0;
		for (TypeClassVarDecList it = dataMembers; it != null; it = it.tail) {
			if (it.head != null && !it.head.t.isFunc()) {
				if (fieldName.equals(it.head.name)) {
					return base + idx;
				}
				idx++;
			}
		}
		if (father != null) {
			return father.getFieldOffset(fieldName);
		}
		return -1;
	}
	
}
