package ast;

import symboltable.SymbolTable;
import types.*;

public class AstCallExp extends AstExp {
    public String id;
    public AstVar var; // null if simple call
    public AstExpList expList;
    public int lineNumber;
	public String methodOwnerClassName = null;

    public AstCallExp(String id, AstExpList expList,int lineNumber) {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.id = id;
        this.expList = expList;
        this.var = null;
        this.lineNumber = lineNumber;
    }

    public AstCallExp(AstVar var, String id, AstExpList expList,int lineNumber) {
        this.var = var;
        this.id = id;
        this.expList = expList;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
    }
     public void printMe() {
        System.out.format("AST_EXP_METHOD( %s )\n", id);

        if (var != null) var.printMe();
        if (expList != null) expList.printMe();
        /*********************************/
        /* Print to AST GRAPHIZ DOT file */
        /*********************************/
        AstGraphviz.getInstance().logNode(serialNumber, String.format("Call for method: NAME(%s)", id));
        if (var != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
        }
        if (expList != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, expList.serialNumber);
        }
    }

    public Type semantMe()
    {
        SymbolTable s = SymbolTable.getInstance();

        Type funcType = null;

        // f(args)
        if (var == null) {
            // In class methods, unqualified calls should resolve to methods first (implicit self).
            if (s.currClass != null) {
                TypeClassVarDec methodInClass = s.currClass.findInClass(id);
                if (methodInClass != null && methodInClass.t != null && methodInClass.t.isFunc()) {
                    funcType = methodInClass.t;

                    TypeClass it = s.currClass;
                    while (it != null) {
                        TypeClassVarDec m = (it.dataMembers == null) ? null : it.dataMembers.find(id);
                        if (m != null && m.t.isFunc()) {
                            methodOwnerClassName = it.name;
                            break;
                        }
                        it = it.father;
                    }
                }
            }

            if (funcType == null) {
                funcType = s.find(id);
                if (funcType == null)
                    return new TypeError(lineNumber,
                            String.format("Undefined function '%s'", id));
                if(funcType.isError()) return funcType;
                if (!funcType.isFunc())
                    return new TypeError(lineNumber,
                            String.format("'%s' is not a function it is %s", id, funcType.getClass().getSimpleName()));
            }
        }

        // x.f(args)
        else {
            Type t = var.semantMe();
            if (t == null || t.isError()) return t;

            if (!t.isClass())
                return new TypeError(lineNumber,
                        String.format("'%s' is not a class", t.name));

            TypeClass cls = (TypeClass) t;
            TypeClassVarDec f = cls.findInClass(id);

            

            if (f == null || !f.t.isFunc())
                return new TypeError(lineNumber,
                        String.format("Class '%s' has no method '%s' ",
                                    cls.name, id));

            TypeClass it = cls;
            while (it != null) {
                TypeClassVarDec m = (it.dataMembers == null) ? null : it.dataMembers.find(id);
                if (m != null && m.t.isFunc()) {
                    methodOwnerClassName = it.name;
                    break;
                }
                it = it.father;
            }

            funcType = f.t;
        }

        TypeFunction func = (TypeFunction) funcType;

        // check parameters
        Type argTypes = (expList == null ? null : expList.semantMe());
       
        if (argTypes!=null && argTypes.isError())
            return new TypeError(lineNumber,
                    String.format("Invalid arguments in call '%s'", id));
        

        if (!func.argsSameType((TypeList) argTypes, false))
            return new TypeError(lineNumber,
                    String.format("Argument mismatch in call '%s'", id));

		return func.t;
	}
	
	public temp.Temp irMe() {
    temp.Temp receiverTemp = null;
    if (var != null) {
        receiverTemp = new AstExpVar(var, lineNumber).irMe();
        if (receiverTemp == null) return null;
    }

        // Unqualified call resolved as class method: use implicit self as receiver.
        if (receiverTemp == null && methodOwnerClassName != null && SymbolTable.getInstance().currClass != null) {
            receiverTemp = temp.TempFactory.getInstance().getFreshTemp();
            ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLoad(receiverTemp, "__self"));
        }

        java.util.List<temp.Temp> args = new java.util.ArrayList<>();
        evalArgsLeftToRight(expList, args);

        if (receiverTemp != null) {
            args.add(0, receiverTemp);
        }

        if ("PrintInt".equals(id)) {
            if (!args.isEmpty() && args.get(0) != null) {
                ir.Ir.getInstance().AddIrCommand(new ir.IrCommandPrintInt(args.get(0)));
            }
            return null;
        }

        if ("PrintString".equals(id)) {
            if (!args.isEmpty() && args.get(0) != null) {
                ir.Ir.getInstance().AddIrCommand(new ir.IrCommandPrintString(args.get(0)));
            }
            return null;
        }

        ir.TempList argList = null;
        for (int i = args.size() - 1; i >= 0; i--) {
            argList = new ir.TempList(args.get(i), argList);
        }

        temp.Temp retTemp = temp.TempFactory.getInstance().getFreshTemp();
        String calleeName = (receiverTemp != null && methodOwnerClassName != null)
            ? ("__virtual__:" + methodOwnerClassName + ":" + id)
            : id;
        ir.IrCommandCall cmd = new ir.IrCommandCall(retTemp, calleeName, argList);
        ir.Ir.getInstance().AddIrCommand(cmd);
        return retTemp;
	}

    private void evalArgsLeftToRight(AstExpList list, java.util.List<temp.Temp> out) {
        if (list == null) return;
        // expList is built in reverse order by the parser:
        //   expList:l COMMA exp:e  => new AstExpList(e, l)
        // so evaluate tail first to preserve source left-to-right semantics.
        evalArgsLeftToRight(list.tail, out);
        if (list.head != null) {
            out.add(list.head.irMe());
        }
    }
}
