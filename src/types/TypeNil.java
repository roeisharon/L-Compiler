package types;

public class TypeNil extends Type
{
    private static TypeNil instance = null;
    protected TypeNil()
    {
    }
    public static TypeNil getInstance()
    {
        if (instance == null)
        {
            instance = new TypeNil();
            instance.name = "nil";
        }
        return instance;
    }
    public boolean isNil(){
        return true;
    }
}
