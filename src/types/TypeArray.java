package types;

public class TypeArray extends Type {
    public Type arrayType;
    
    public TypeArray(String name,Type arrayType) {
        this.arrayType = arrayType;
        this.name = name;
    }
    public boolean isArray() {
        return true;
    }   

}
