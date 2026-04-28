package types;

public class TypeError extends Type {
    public int line;
    public String msg;
    public TypeError(int line) {
       
        this.line = line;
    }
    public TypeError(int line, String msg){
        this.line = line;
        this.msg = msg;
    }

    public boolean isError() {
        return true;
    }
}
