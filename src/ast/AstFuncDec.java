package ast;
import symboltable.SymbolTable;
import types.*;

public class AstFuncDec extends AstDec {
    public String name;
    public AstParamList params;
    public AstType Type;
    public AstStmtList body;
    public int lineNumber;

    public AstFuncDec(AstType returnType,String name,AstParamList params, AstStmtList body,int lineNumber) {
        this.name = name;
        this.params = params;
        this.Type = returnType;
        this.body = body;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("funcDec ->  TYPE ID PARAMLIST STMTLIST\n", name);
    }


    public void printMe() {
        System.out.format("AST_FUNC_DEC ID( %s )\n", name);
        if (Type!=null) Type.printMe();
        if (params!=null) params.printMe();
        if (body!=null) body.printMe();
        /*********************************/
        /* Print to AST GRAPHIZ DOT file */
        /*********************************/
        AstGraphviz.getInstance().logNode(serialNumber, String.format("Function Declaration NAME(%s)", name));
        if (Type!=null) AstGraphviz.getInstance().logEdge(serialNumber, Type.serialNumber);
        if (params!=null) AstGraphviz.getInstance().logEdge(serialNumber, params.serialNumber);
        if (body!=null) AstGraphviz.getInstance().logEdge(serialNumber, body.serialNumber);

    }
    public Type semantMe() {
        SymbolTable s = SymbolTable.getInstance();
        // Check if function name already exists in the current scope
        if (s.existsInScope(name)) {
        return new TypeError(lineNumber,
                String.format("Function '%s' already defined in this scope", name));
        }
        // Semant return type
        Type returnType = Type.semantMe();
        if (returnType.isError() || returnType == null) {
            return returnType;
        }

        // open scope and semant params
        s.beginFunctionScope(returnType);
        TypeList paramTypes = null;
        if (params != null) {
            Type t = params.semantMe();
            if (t.isError() || t == null) {
                return t;
            }
            paramTypes = (TypeList) t;
        }

        // check father class
        TypeFunction duplicate = null;
        if (s.currClass != null) {
            // find function in father class
            TypeClassVarDec found = s.currClass.findInClass(name);
            if (found != null) {
                if (!found.t.isFunc()){
                    return new TypeError(lineNumber,
                        String.format("'%s' in class '%s' is not a function", name, s.currClass.name));
                }
                duplicate = (TypeFunction) found.t;
                // check return type
                if (duplicate.t != returnType) {
                    return new TypeError(lineNumber,
                        String.format("Function '%s' return type does not match the one in the father class", name));
                }
                // check param types
                if (!duplicate.argsSameType(paramTypes, true)) {
                    return new TypeError(lineNumber,
                        String.format("Function '%s' parameters do not match the ones in the father class", name));
                }
            }     
        } 


        // create function type and add to symbol table
        TypeFunction funcType = new TypeFunction(returnType,name,paramTypes);
        // make the method visible in the current class during body checking
        // so that it can be called recursively
        if (s.currClass != null) {
            s.currClass.dataMembers.insert(new TypeClassVarDec(funcType, name));
        }
        s.enter(name, funcType, false);

        if (body != null) {
            Type t = body.semantMe();
            if (t==null){
                throw new RuntimeException(
        "NULL returned from semantMe in AstFuncDec: " +
        body.getClass().getSimpleName());
            }
            if (t == null || t.isError()) {
                return t;
            }
        }
        s.endFunctionScope();
        s.enter(name, funcType, false);

        return funcType;         
    }
    
    public temp.Temp irMe() {
        System.out.println("IR FUNC_DEC");
		SymbolTable s = SymbolTable.getInstance();
		String funcLabel = (s.currClass != null) ? (s.currClass.name + "_" + name) : name;

		ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLabel(funcLabel, true));

		int paramIdx = 0;
        if (s.currClass != null) {
            ir.Ir.getInstance().registerParam(funcLabel, "__self", 0);
            paramIdx = 1;
        }
        java.util.ArrayList<String> paramNames = new java.util.ArrayList<>();
        AstParamList it = params;
        while (it != null && it.head != null) {
            paramNames.add(it.head.name);
            it = it.tail;
        }
        for (int i = paramNames.size() - 1; i >= 0; i--) {
			ir.Ir.getInstance().registerParam(funcLabel, paramNames.get(i), paramIdx++);
		}

		if (body != null) {
			body.irMe();
		}

        if (Type != null && "void".equals(Type.type_name)) {
			ir.Ir.getInstance().AddIrCommand(new ir.IrCommandReturn(null));
		}
        return null;
    }   
}