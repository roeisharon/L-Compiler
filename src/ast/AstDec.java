package ast;

public abstract class AstDec extends AstStmt {
    public void printMe() {
        System.out.print("UNKNOWN AST DECELERATION NODE");
    }
    /**
     * Optional pre-semant pass used to register top-level declarations
     * (e.g., global variable names) before performing full semantic checks.
     * Default is no-op.
     */
    public void preSemant() {
        // no-op by default
    }
    
}
