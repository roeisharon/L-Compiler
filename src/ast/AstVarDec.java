package ast;

import symboltable.SymbolTable;
import types.*;

public class AstVarDec extends AstDec {
    public AstType type;
    public String name;
    int lineNumber;
    AstExp assignExp;
    private String uniqueVarId; // Store unique identifier from semantMe
    private boolean isGlobal = false;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstVarDec(AstType type, String name, AstExp assignExp, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.format("====================== varDec -> type ID( %s )\n", name);

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.type = type;
        this.name = name;
        this.assignExp = assignExp;
        this.lineNumber = lineNumber;
    }

    /*************************************************/
    /* The printing message for a var dec AST node */
    /*************************************************/
    public void printMe() {
        /*********************************/
        /* AST NODE TYPE = AST VAR DEC */
        /*********************************/
        System.out.print("AST NODE VAR DEC\n");

        /**********************************************/
        /* PRINT TYPE, then NAME ... */
        /**********************************************/
        if (type != null)
            System.out.format("TYPE( %s )\n", type.type_name);
        System.out.format("VAR NAME( %s )\n", name);

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("VAR\nDEC\n%s", name));

    }

    public Type semantMe() {
        SymbolTable s = SymbolTable.getInstance();
        System.out.println("current scope_depth: " + s.curr_scope_depth );
        if (type == null) {
            return new TypeError(lineNumber, "Variable declaration must have a type");
        }

        // 1. semant type
        Type t = type.semantMe();
        if (t == null || t.isError())
            return new TypeError(lineNumber, "Unknown type for variable " + name);

        // 2. void type check
        if (t.isVoid()) {
            return new TypeError(lineNumber,
                    String.format("Variable '%s' cannot be of type void", name));
        }
        // 23. check redefinition in same scope
        // Allow redefinition if we're in the global scope (pre-declaration pass)
        if (s.existsInScope(name) && !s.isGlobalScope()) {
            System.out.println("Error: Variable " + name + " already defined in this scope");
            return new TypeError(lineNumber, "Variable " + name + " already defined in this scope");
        }

        // 4. class shadowing rule
        if (s.shadowingVariable(name, t))
            return new TypeError(lineNumber, "Illegal shadowing of inherited field " + name);

        // 5. assignment check
        if (assignExp != null) {
            
            Type val = assignExp.semantMe();
            if (val == null || val.isError())
                return val;

            if (!s.canAssign(t, val))
                return new TypeError(lineNumber,
                        "Cannot assign value of type " + val.name + " to variable of type " + t.name);
        }

        // 6. Check if global scope
        if (s.isGlobalScope()) {
            this.isGlobal = true;
            System.out.println("Variable " + name + " is global=true");
        }
       
        System.out.println("current scope_depth: " + s.curr_scope_depth);

        // 7. insert to table if not already present (pre-declared)
        if (!s.existsInScope(name)) {
            s.enter(name, t);
        }

        // Store unique identifier for later use in irMe()
        symboltable.SymbolTableEntry entry = s.findEntry(name);
        System.out.println("ENTERED VAR " + name + " WITH INDEX " + (entry != null ? entry.uniqueindex : "null"));
        if (entry != null) {
            this.uniqueVarId = name + "#" + entry.uniqueindex;
        } else {
            this.uniqueVarId = name; // Fallback
        }

        return t;
    }

    @Override
    public void preSemant() {
        // Register global variables in a first pass so they are visible to
        // function bodies that appear earlier in the source file.
        System.out.println("Pre-semanting var dec " + name);
        symboltable.SymbolTable s = symboltable.SymbolTable.getInstance();
        if (s.isGlobalScope()) {
            Type t = type.semantMe();
            if (t != null && !t.isError()) {
                if (!s.existsInScope(name)) {
                    s.enter(name, t);
                    symboltable.SymbolTableEntry entry = s.findEntry(name);
                    if (entry != null) this.uniqueVarId = name + "#" + entry.uniqueindex;
                    this.isGlobal = true;
                }
               
            }
        }
    }

    public temp.Temp irMe() {
        System.out.println("IR VAR_DEC");

        if (isGlobal) {
            String uniqueVarIdToUse = (uniqueVarId != null) ? uniqueVarId : name;
            ir.Ir.getInstance().registerGlobal(uniqueVarIdToUse);
            ir.Ir.getInstance().AddIrCommand(new ir.IrCommandAllocate(uniqueVarIdToUse));
        }

        // If variable has initialization, generate store command
        if (assignExp != null) {
            temp.Temp expTemp = assignExp.irMe();
            if (expTemp != null) {
                // Use the unique identifier stored during semantMe()
                System.out.println("adding store cmd with global " + isGlobal + " " + uniqueVarId + " FOR VAR " + name);
                String uniqueVarIdToUse = (uniqueVarId != null) ? uniqueVarId : name;
                ir.IrCommandStore storeCmd = new ir.IrCommandStore(name, uniqueVarIdToUse, expTemp, isGlobal);
                ir.Ir.getInstance().AddIrCommand(storeCmd);
            }
        }
        // If no initialization, variable remains uninitialized (no IR needed)
        return null;
    }

}
