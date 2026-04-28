package ast;

import symboltable.SymbolTable;
import types.*;

public class AstAssignCl extends AstNode {
    public String name;
    public AstExp assignExp;
    public int lineNumber;

    public AstAssignCl(String name, AstExp assignExp,int lineNumber) {
        this.name = name;
        this.assignExp = assignExp;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("assignClause -> ASSIGN exp\n");
    }

    public void printMe() {
        System.out.print("AST_ASSIGN_CLAUSE\n");

        if (assignExp != null) assignExp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "ASSIGN_CLAUSE");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        AstGraphviz.getInstance().logEdge(serialNumber, assignExp.serialNumber);
    }
    public Type semantMe() {

        SymbolTable s = SymbolTable.getInstance();

        Type varType = s.find(name);
        if (varType == null) {
            return new TypeError(lineNumber,
                    "Variable '" + name + "' was not declared");
        }

        if (varType.isError())
            return varType;

        Type expType = assignExp.semantMe();
        if (expType == null || expType.isError())
            return expType;

        if (!s.canAssign(varType, expType)) {
            return new TypeError(lineNumber,
                    String.format(
                            "Cannot assign value of type '%s' to variable '%s' (type '%s')",
                            expType.name, name, varType.name));
        }

        return null;
    }
}