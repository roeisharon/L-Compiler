/***********/
/* PACKAGE */
/***********/
package symboltable;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;

/****************/
/* SYMBOL TABLE */
/****************/
public class SymbolTable
{
	private int hashArraySize = 13;
	
	/**********************************************/
	/* The actual symbol table data structure ... */
	/**********************************************/
	private static int uniqueIndexCounter = 0;
	private SymbolTableEntry[] table = new SymbolTableEntry[hashArraySize];
	private SymbolTableEntry top;
	private int topIndex = 0;
	public int curr_scope_depth = 0;
	private int classScopeDepth = -1;
	private Type returnType = null;
	public TypeClass currClass = null;
	
	/**************************************************************/
	/* A very primitive hash function for exposition purposes ... */
	/**************************************************************/
	private int hash(String s)
	{
		if (s.charAt(0) == 'l') {return 1;}
		if (s.charAt(0) == 'm') {return 1;}
		if (s.charAt(0) == 'r') {return 3;}
		if (s.charAt(0) == 'i') {return 6;}
		if (s.charAt(0) == 'd') {return 6;}
		if (s.charAt(0) == 'k') {return 6;}
		if (s.charAt(0) == 'f') {return 6;}
		if (s.charAt(0) == 'S') {return 6;}
		return 12;
	}

	/****************************************************************************/
	/* Enter a variable, function, class type or array type to the symbol table */
	/****************************************************************************/
	public void enter(String name, Type t)
	{
		/*************************************************/
		/* [1] Compute the hash value for this new entry */
		/*************************************************/
		int hashValue = hash(name);
		

		/******************************************************************************/
		/* [2] Extract what will eventually be the next entry in the hashed position  */
		/*     NOTE: this entry can very well be null, but the behaviour is identical */
		/******************************************************************************/
		SymbolTableEntry next = table[hashValue];
	
		/**************************************************************************/
		/* [3] Prepare a new symbol table entry with name, type, next and prevtop */
		/**************************************************************************/
		SymbolTableEntry e = new SymbolTableEntry(name,t,hashValue,next,curr_scope_depth,top, topIndex++);
		uniqueIndexCounter++;
		e.uniqueindex = uniqueIndexCounter;

		/**********************************************/
		/* [4] Update the top of the symbol table ... */
		/**********************************************/
		top = e;
		
		/****************************************/
		/* [5] Enter the new entry to the table */
		/****************************************/
		table[hashValue] = e;
		
		/**************************/
		/* [6] Print Symbol Table */
		/**************************/
		printMe();
	}
	public void enter(String name, Type t, boolean newable){
		/*************************************************/
		/* [1] Compute the hash value for this new entry */
		/*************************************************/
		int hashValue = hash(name);

		/******************************************************************************/
		/* [2] Extract what will eventually be the next entry in the hashed position  */
		/*     NOTE: this entry can very well be null, but the behaviour is identical */
		/******************************************************************************/
		SymbolTableEntry next = table[hashValue];
	
		/**************************************************************************/
		/* [3] Prepare a new symbol table entry with name, type, next and prevtop */
		/**************************************************************************/
		SymbolTableEntry e = new SymbolTableEntry(name,newable,t,hashValue,next,curr_scope_depth,top, topIndex++);

		/**********************************************/
		/* [4] Update the top of the symbol table ... */
		/**********************************************/
		top = e;
		
		/****************************************/
		/* [5] Enter the new entry to the table */
		/****************************************/
		table[hashValue] = e;
		
		/**************************/
		/* [6] Print Symbol Table */
		/**************************/
		printMe();
	}

	/***********************************************/
	/* Find the inner-most scope element with name */
	/***********************************************/
	public Type find(String name)
	{
		SymbolTableEntry e;
		TypeClassVarDec res;
				
		for (e = table[hash(name)]; e != null; e = e.next)
		{
			if (name.equals(e.name))
			{
				if((currClass!=null)&&e.scope_depth==0){
					break;
				}
				return e.type;
			}
		}
		if(currClass!=null){
			res=currClass.findInClass(name);
			if(res!=null){
				return res.t;
			}
			else if(e!=null){
				return e.type;
			}
		}
		return null;
	}
	
	/***********************************************/
	/* Find the inner-most scope entry with name  */
	/* Returns the SymbolTableEntry (for unique ID) */
	/***********************************************/
	public SymbolTableEntry findEntry(String name)
	{
		SymbolTableEntry e;
		TypeClassVarDec res;
				
		for (e = table[hash(name)]; e != null; e = e.next)
		{
			if (name.equals(e.name))
			{
				if((currClass!=null)&&e.scope_depth==0){
					break;
				}
				return e;
			}
		}
		if(currClass!=null){
			res=currClass.findInClass(name);
			if(res!=null){
				// For class members, we don't have a direct entry
				// Return null or handle differently
				return null;
			}
			else if(e!=null){
				return e;
			}
		}
		return null;
	}

	public Type findClass(String className){
		SymbolTableEntry e;
		
				
		for (e = table[hash(className)]; e != null; e = e.next)
		{
			if (className.equals(e.name))
			{
				if(!e.newable)
				{
					
					return null;
				}
				
				return e.type;
			}
		}
				
		return null;
	}

	public Type findNonClass(String name){
		SymbolTableEntry e;
		TypeClassVarDec res;
				
		for (e = table[hash(name)]; e != null; e = e.next)
		{
			if (name.equals(e.name))
			{
				if(e.newable){
					continue;
				}
				if ((currClass != null) && (e.scope_depth == 0)) {
					break;
				}
				return e.type;
			}
		}
		if(currClass!=null){
			res=currClass.findInClass(name);
			if(res!=null){
				return res.t;
			}
			else if(e!=null){
				return e.type;
			}
		}
		return null;
	}

	public boolean shadowingVariable(String name, Type t){
		if(currClass != null && curr_scope_depth == 1) {
			
			TypeClassVarDec d = currClass.findInClass(name);
			if(d != null) {
				return true;
			}
		}
		return false;}

	public boolean existsInScope(String name){
		SymbolTableEntry  e;
		for (e = table[hash(name)]; e != null; e = e.next)
		{
			if (name.equals(e.name))
			{
				return e.scope_depth == curr_scope_depth;
			}
		}
		return false;}

	public boolean canAssign(Type variable, Type value){
		if(variable == value) return true;
		if(value.isNil()){
			return variable.isClass()||variable.isArray()||variable.isNil();
		}
		if(value.isFunc()){
			value = ((TypeFunction)value).t;
			System.out.println("The func type is " + value.name);
			System.out.println("The var type is " + variable.name);
			return value.name.equals(variable.name);
		}
		if(value.isArray()&&variable.isArray()){
			TypeArray vararr = (TypeArray)variable;
			TypeArray valarr = (TypeArray)value;
			if(valarr.name !=null){
				return valarr.name.equals(vararr.name);
			}
			return valarr.arrayType == vararr.arrayType
        || (valarr.arrayType.name != null
            && valarr.arrayType.name.equals(vararr.arrayType.name));
			
		}
		if(value.isClass()&&variable.isClass()){
			return ((TypeClass)variable).fatherOf((TypeClass)value);
		}
		return variable.name.equals(value.name);
	}

	public boolean canReturnType(Type type){
		if(this.returnType == null) return false;
		return canAssign(this.returnType, type);	
		}

	/***************************************************************************/
	/* begine scope = Enter the <SCOPE-BOUNDARY> element to the data structure */
	/***************************************************************************/
	public void beginScope()
	{
		/************************************************************************/
		/* Though <SCOPE-BOUNDARY> entries are present inside the symbol table, */
		/* they are not really types. In order to be able to debug print them,  */
		/* a special TYPE_FOR_SCOPE_BOUNDARIES was developed for them. This     */
		/* class only contain their type name which is the bottom sign: _|_     */
		/************************************************************************/
		enter(
			"SCOPE-BOUNDARY",
			new TypeForScopeBoundaries("NONE"));
		curr_scope_depth = curr_scope_depth + 1;

		/*********************************************/
		/* Print the symbol table after every change */
		/*********************************************/
		printMe();
	}

	public void beginClassScope(TypeClass className){
		beginScope();
		this.currClass = className;
		this.classScopeDepth = curr_scope_depth;
	}
	public void endClassScope(){
		endScope();
		//entered the typeclass before the boundary, and every member stored in the Typeclass
		this.currClass = null;
		this.classScopeDepth = -1;
	}

	public int getClassScopeDepth() {
		return classScopeDepth;
	}

	public void beginFunctionScope(Type returnType)
	{
		beginScope();
		
		this.returnType = returnType;
	}
	public void endFunctionScope(){
		endScope();
		this.returnType = null;
	}

	public boolean isGlobalScope(){return this.curr_scope_depth == 0;}


	/********************************************************************************/
	/* end scope = Keep popping elements out of the data structure,                 */
	/* from most recent element entered, until a <NEW-SCOPE> element is encountered */
	/********************************************************************************/
	public void endScope()
	{
		/**************************************************************************/
		/* Pop elements from the symbol table stack until a SCOPE-BOUNDARY is hit */		
		/**************************************************************************/
		while (top.name != "SCOPE-BOUNDARY")
		{
			table[top.index] = top.next;
			topIndex = topIndex -1;
			top = top.prevtop;
		}
		/**************************************/
		/* Pop the SCOPE-BOUNDARY sign itself */		
		/**************************************/
		table[top.index] = top.next;
		topIndex = topIndex -1;
		top = top.prevtop;
		curr_scope_depth = curr_scope_depth - 1;

		/*********************************************/
		/* Print the symbol table after every change */		
		/*********************************************/
		printMe();
	}
	
	public static int n=0;
	
	public void printMe()
	{
		int i=0;
		int j=0;
		String dirname="./output/";
		String filename=String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt",n++);

		try
		{
			/*******************************************/
			/* [1] Open Graphviz text file for writing */
			/*******************************************/
			PrintWriter fileWriter = new PrintWriter(dirname+filename);

			/*********************************/
			/* [2] Write Graphviz dot prolog */
			/*********************************/
			fileWriter.print("digraph structs {\n");
			fileWriter.print("rankdir = LR\n");
			fileWriter.print("node [shape=record];\n");

			/*******************************/
			/* [3] Write Hash Table Itself */
			/*******************************/
			fileWriter.print("hashTable [label=\"");
			for (i=0;i<hashArraySize-1;i++) { fileWriter.format("<f%d>\n%d\n|",i,i); }
			fileWriter.format("<f%d>\n%d\n\"];\n",hashArraySize-1,hashArraySize-1);
		
			/****************************************************************************/
			/* [4] Loop over hash table array and print all linked lists per array cell */
			/****************************************************************************/
			for (i=0;i<hashArraySize;i++)
			{
				if (table[i] != null)
				{
					/*****************************************************/
					/* [4a] Print hash table array[i] -> entry(i,0) edge */
					/*****************************************************/
					fileWriter.format("hashTable:f%d -> node_%d_0:f0;\n",i,i);
				}
				j=0;
				for (SymbolTableEntry it = table[i]; it!=null; it=it.next)
				{
					/*******************************/
					/* [4b] Print entry(i,it) node */
					/*******************************/
					fileWriter.format("node_%d_%d ",i,j);
					fileWriter.format("[label=\"<f0>%s|<f1>%s|<f2>prevtop=%d|<f3>next\"];\n",
						it.name,
						it.type.name,
						it.prevtopIndex);

					if (it.next != null)
					{
						/***************************************************/
						/* [4c] Print entry(i,it) -> entry(i,it.next) edge */
						/***************************************************/
						fileWriter.format(
							"node_%d_%d -> node_%d_%d [style=invis,weight=10];\n",
							i,j,i,j+1);
						fileWriter.format(
							"node_%d_%d:f3 -> node_%d_%d:f0;\n",
							i,j,i,j+1);
					}
					j++;
				}
			}
			fileWriter.print("}\n");
			fileWriter.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static SymbolTable instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected SymbolTable() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static SymbolTable getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new SymbolTable();

			/*****************************************/
			/* [1] Enter primitive types int, string */
			/*****************************************/
			instance.enter("int",   TypeInt.getInstance());
			instance.enter("string", TypeString.getInstance());

			/*************************************/
			/* [2] How should we handle void ??? */
			/*************************************/

			/***************************************/
			/* [3] Enter library function PrintInt */
			/***************************************/
			instance.enter(
				"PrintInt",
				new TypeFunction(
					TypeVoid.getInstance(),
					"PrintInt",
					new TypeList(
						TypeInt.getInstance(),
						null)));
			instance.enter(
                "PrintString",
                new TypeFunction(
                    TypeVoid.getInstance(),
                    "PrintString",
                    new TypeList(
                        TypeString.getInstance(),
                        null)));
			
		}
		return instance;
	}

	


}
