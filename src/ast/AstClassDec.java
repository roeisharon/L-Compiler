package ast;
import symboltable.SymbolTable;
import types.*;

public class AstClassDec extends AstDec {
    public String name;
    public AstExtCl extCl; // can be null
    public AstCFieldList cFieldList; // can be null
    public int lineNumber;
    
    public AstClassDec(String name, AstExtCl extCl, AstCFieldList cFieldList,int lineNumber) {
        this.name = name;
        this.extCl = extCl;
        this.cFieldList = cFieldList;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        if (extCl != null) System.out.print("classDec -> CLASS ID EXTCL CFIELDLIST\n");
        if (extCl == null) System.out.print("classDec -> CLASS ID CFIELDLIST\n");
    }
    public void printMe() {
        

        if (extCl != null) extCl.printMe();
        if (cFieldList != null) cFieldList.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "CLASS_DEC");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (extCl != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, extCl.serialNumber);
        }
        if (cFieldList != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, cFieldList.serialNumber);
        }
    }

    public Type semantMe() {
        SymbolTable s = SymbolTable.getInstance();
        if(!s.isGlobalScope()) {
            return new TypeError(lineNumber, "Class declarations are only allowed in the global scope");
        }
        if (s.existsInScope(name)) {
            return new TypeError(lineNumber, "Class name already exists in the current scope");
        }
        if(name.equals("int")||name.equals("string")||name.equals("void")) {
            return new TypeError(lineNumber, "Invalid class name");
        }
        
        TypeClass father = null;
        if (extCl != null) {
            Type tFather = s.findClass(extCl.id);
            if (tFather == null || !tFather.isClass()) { // added isClass check that was missing
                return new TypeError(lineNumber, "Extended class not found");
            }
            father = (TypeClass) tFather;
        }
        
       TypeClass thisClass = new TypeClass(father, name, new TypeClassVarDecList(null, null));
        s.enter(name, thisClass, true);
        s.beginClassScope(thisClass);

        
        if (cFieldList != null) {
            Type tMembers = cFieldList.semantMe();
            if (tMembers == null || tMembers.isError()) {
                s.endClassScope();  
                return tMembers;
            }
            thisClass.dataMembers = (TypeClassVarDecList) tMembers;

            AstCFieldList it = cFieldList;
            while (it != null) {
                if (it.head != null && it.head.dec instanceof AstVarDec) {
                    AstVarDec vd = (AstVarDec) it.head.dec;
                    if (vd.assignExp instanceof AstExpInt) {
                        thisClass.setIntFieldInitializer(vd.name, ((AstExpInt) vd.assignExp).value);
                    } else if (vd.assignExp instanceof AstExpString) {
                        thisClass.setStringFieldInitializer(vd.name, ((AstExpString) vd.assignExp).value);
                    } else if (vd.assignExp instanceof AstExpNil) {
                        thisClass.setNilFieldInitializer(vd.name);
                    }
                }
                it = it.tail;
            }
        }
        

        s.endClassScope();
       
        return thisClass;
    }

    public temp.Temp irMe() {
        SymbolTable s = SymbolTable.getInstance();
        TypeClass prev = s.currClass;
        Type classType = s.findClass(this.name);
        if (classType instanceof TypeClass) {
            s.currClass = (TypeClass) classType;
        }

        if (cFieldList != null) {
            AstCFieldList it = cFieldList;
            while (it != null) {
                if (it.head != null && it.head.dec instanceof AstFuncDec) {
                    it.head.dec.irMe();
                }
                it = it.tail;
            }
        }

        s.currClass = prev;
        return null;
    }

}
