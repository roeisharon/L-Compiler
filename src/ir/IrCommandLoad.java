/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;

public class IrCommandLoad extends IrCommand {
	Temp dst;
	String varName; // Original name (for debugging)
	String uniqueVarId; // Unique identifier (name#index) to handle shadowing
	

	public IrCommandLoad(Temp dst, String varName) {
		this.dst = dst;
		this.varName = varName;
		this.uniqueVarId = varName; // Default: use name if unique ID not provided
	}

	public IrCommandLoad(Temp dst, String varName, String uniqueVarId) {
		this.dst = dst;
		this.varName = varName;
		this.uniqueVarId = uniqueVarId;
	}
	

	public IrCommandLoad(Temp dst, String varName, String uniqueVarId, Boolean isGlobal) {
		this.dst = dst;
		this.varName = varName;
		this.uniqueVarId = uniqueVarId;
		this.isGlobal = isGlobal;
	}
}