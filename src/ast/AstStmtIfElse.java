package ast;
import symboltable.SymbolTable;
import types.*;

public class AstStmtIfElse extends AstStmt {
    public AstExp condition;
    public AstStmt thenBranch;
    public AstStmt elseBranch;
    public int lineNumber;

    public AstStmtIfElse(AstExp condition, AstStmt thenBranch, AstStmt elseBranch, int lineNumber) {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
        this.lineNumber = lineNumber;
    }

    public void printMe() {
        System.out.print("AST_STMT_IF_ELSE\n");

        if (condition != null) condition.printMe();
        if (thenBranch != null) thenBranch.printMe();
        if (elseBranch != null) elseBranch.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "STMT_IF_ELSE");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        AstGraphviz.getInstance().logEdge(serialNumber, condition.serialNumber);
        AstGraphviz.getInstance().logEdge(serialNumber, thenBranch.serialNumber);
        AstGraphviz.getInstance().logEdge(serialNumber, elseBranch.serialNumber);
    }
    public Type semantMe() {

        SymbolTable s = SymbolTable.getInstance();

        Type condType = condition.semantMe();
        if(condType == null || condType.isError()) return condType;
        if (condType != TypeInt.getInstance()) {
            return new TypeError(lineNumber, "Condition in IF-ELSE must be int");
        }

        // THEN
        s.beginScope();
        Type t1 = thenBranch.semantMe();
        s.endScope();
        if (t1 == null || t1.isError()) return t1;

        // ELSE
        s.beginScope();
        Type t2 = elseBranch.semantMe();
        s.endScope();
        if (t2 == null || t2.isError()) return t2;

        return TypeVoid.getInstance();
    }
    
    public temp.Temp irMe() {
        if (condition == null) return null;
        
       
        
        String labelElse = ir.IrCommand.getFreshLabel("if_else");
        String labelEnd = ir.IrCommand.getFreshLabel("if_end");
        
        // Evaluate condition
        temp.Temp condTemp = condition.irMe();
        if (condTemp == null) return null;
        
        // Jump to else if condition is false
        ir.IrCommandJumpIfEqToZero jumpToElse = new ir.IrCommandJumpIfEqToZero(condTemp, labelElse);
        ir.Ir.getInstance().AddIrCommand(jumpToElse);
        
        // Open scope for then branch (mirroring semantMe)
       
        // Then branch
        if (thenBranch != null) thenBranch.irMe();
        // Close scope
       
        
        // Jump to end (skip else)
        ir.IrCommandJumpLabel jumpToEnd = new ir.IrCommandJumpLabel(labelEnd);
        ir.Ir.getInstance().AddIrCommand(jumpToEnd);
        
        // Else label
        ir.IrCommandLabel elseLabel = new ir.IrCommandLabel(labelElse);
        ir.Ir.getInstance().AddIrCommand(elseLabel);
        
        // Open scope for else branch (mirroring semantMe)
       
        // Else branch
        if (elseBranch != null) elseBranch.irMe();
        // Close scope
       
        
        // End label
        ir.IrCommandLabel endLabel = new ir.IrCommandLabel(labelEnd);
        ir.Ir.getInstance().AddIrCommand(endLabel);
        
        return null;
    }
}
