package types;

public abstract class Type
{
	/******************************/
	/*  Every type has a name ... */
	/******************************/
	public String name;

	/*************/
	/* isClass() */
	/*************/
	public boolean isClass(){ return false;}

	/*************/
	/* isArray() */
	/*************/
	public boolean isArray(){ return false;}
	public boolean isString(){return false;}

	/*************/
	/* isNil() */
	/*************/
	public boolean isNil(){ return false;}

	/*************/
	/* isError() */
	/*************/
	public boolean isError(){ return false;}

	/*************/
	/* isFunc() */
	/*************/
	public boolean isFunc(){ return false;}

	/*************/
	/* isInt() */
	/*************/
	public boolean isInt(){ return false;}

	/*************/
	/* isVoid() */
	/*************/
	public boolean isVoid(){ return false;}

	/*************/
	/* isList() */
	/*************/
	public boolean isList(){ return false;}
}
