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

public class IrCommandLabel extends IrCommand
{
	public String labelName;
	public boolean isFunctionEntry;
	
	public IrCommandLabel(String labelName)
	{
		this.labelName = labelName;
		this.isFunctionEntry = false;
	}

	public IrCommandLabel(String labelName, boolean isFunctionEntry)
	{
		this.labelName = labelName;
		this.isFunctionEntry = isFunctionEntry;
	}
}
