package ast;

public abstract class AstVar extends AstNode
{
    public void printMe() {
        System.out.print("UNKNOWN AST DECLARATION NODE");
    }
    
    /***********************************************/
    /* Helper method to get variable name for IR   */
    /***********************************************/
    public String getVarName() {
        if (this instanceof AstVarSimple) {
            return ((AstVarSimple) this).name;
        }
        // For field and subscript access, we'd need more complex handling
        // but for this exercise subset, only simple variables are used
        return null;
    }
    
    /***********************************************/
    /* Get unique identifier for variable (name#index) */
    /***********************************************/
    public String getUniqueVarId() {
        if (this instanceof AstVarSimple) {
            return ((AstVarSimple) this).getUniqueVarId();
        }
        // For field and subscript access, we'd need more complex handling
        return null;
    }
}
