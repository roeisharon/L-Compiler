package ast;

import symboltable.SymbolTable;
import types.*;

public class AstArrTypeDef extends AstDec {
    public AstType type;
    public String name;
    public int lineNumber;

    public AstArrTypeDef(String name, AstType type,int lineNumber) {
        this.type = type;
        this.name = name;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();  
        System.out.format("arrayTypeDef ->  ARRAY NAME( %s ) = type[]\n", name);

   
    }
    public void printMe() {
        System.out.format("AST_ARRAY_TYPEDEF ID( %s ) = TYPE[]", name);
        if (type!=null) type.printMe();
        /*********************************/
        /* Print to AST GRAPHIZ DOT file */
        /*********************************/
        AstGraphviz.getInstance().logNode(serialNumber, String.format("Array Declaration NAME(%s)", name));
        AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
    }
    public Type semantMe(){
        SymbolTable s = SymbolTable.getInstance();
        /* check that the array name is available.
         * Note that we should only check the local scope,
         * since arrays can only be defined in the global scope. */
        if (!s.isGlobalScope()) return new TypeError(lineNumber, "Array Types can be defined at global scope only");
        if (s.existsInScope(name)) return new TypeError(type.lineNumber, String.format("%s type exists already", name));
        if (type == null) return null; 
        Type t = type.semantMe();
        if (t == null || t.isError()) return t;
        if (t.isVoid()) return new TypeError(type.lineNumber, "Array type cannot be void");
        // semantic success
        TypeArray arrType = new TypeArray(name, t);
        s.enter(name, arrType, true);
        return arrType;
    }
}
