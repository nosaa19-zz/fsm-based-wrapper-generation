package Domain;

public class State {

    private Integer colId;

    private char colSymbolId;

    String codeNameOfSet;

    public State(){}

    public State(Integer colId, char colSymbolId, String codeNameOfSet){
        this.colId = colId;
        this.colSymbolId = colSymbolId;
        this.codeNameOfSet = codeNameOfSet;

    }

    public Integer getColId() {
        return colId;
    }

    public void setColId(Integer colId) {
        this.colId = colId;
    }

    public char getColSymbolId() {
        return colSymbolId;
    }

    public void setColSymbolId(char colSymbolId) {
        this.colSymbolId = colSymbolId;
    }

    public String getCodeNameOfSet() { return codeNameOfSet; }

    public void setCodeNameOfSet(String codeNameOfSet) { this.codeNameOfSet = codeNameOfSet; }
}
