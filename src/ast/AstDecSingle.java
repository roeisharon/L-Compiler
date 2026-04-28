package ast;
import types.*;

public class AstDecSingle extends AstDec {
public AstDec dec;
public int lineNumber;

    public AstDecSingle(AstDec dec,int lineNumber) {
        this.dec = dec;
        this.lineNumber = lineNumber;
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.print("dec ->  varDec | funcDec | classDec | arrayTypeDef \n");
    }

    public void printMe() {
        System.out.format("AST_DEC_SINGLE\n");
        if (dec != null)
        dec.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("DEC_SINGLE"));
        
        if (dec != null)
        AstGraphviz.getInstance().logEdge(serialNumber, dec.serialNumber);
    }
    public Type semantMe() {
        if (dec != null)
            return dec.semantMe();
        if(dec == null) {
        	throw new RuntimeException(
        "NULL returned from semantMe in AstDecSingle: " +
        dec.getClass().getSimpleName());
        }
        return null;
    }
    public temp.Temp irMe() {
        System.out.println("IR DEC_SINGLE");
        if (dec != null)
            return dec.irMe();
        return null;
    }
    @Override
    public void preSemant() {
        // Forward preSemant to the contained declaration so top-level
        // pre-semant passes reach concrete declarations (e.g., AstVarDec).
        if (dec != null) dec.preSemant();
    }
}