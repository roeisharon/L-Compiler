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

public class IrCommandStore extends IrCommand {
	public String varName; // Original name (for debugging)
	public String uniqueVarId; // Unique identifier (name#index) to handle shadowing
	public Temp src;
	

	public IrCommandStore(String varName, Temp src) {
		this.src = src;
		this.varName = varName;
		this.uniqueVarId = varName; // Default: use name if unique ID not provided
		this.isGlobal = false;
	}

	public IrCommandStore(String varName, String uniqueVarId, Temp src) {
		this.src = src;
		this.varName = varName;
		this.uniqueVarId = uniqueVarId;
		this.isGlobal = false;
	}

	public IrCommandStore(String varName, String uniqueVarId, Temp src, boolean isGlobal) {
		this.src = src;
		this.varName = varName;
		this.uniqueVarId = uniqueVarId;
		this.isGlobal = isGlobal;
	}
}
