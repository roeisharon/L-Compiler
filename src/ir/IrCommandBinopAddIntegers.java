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

public class IrCommandBinopAddIntegers extends IrCommand
{
	public Temp t1;
	public Temp t2;
	public Temp dst;
	
	public IrCommandBinopAddIntegers(Temp dst, Temp t1, Temp t2, Boolean isGlobal)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
		this.isGlobal = isGlobal;
	}
}

