package Domain;

import java.io.Serializable;

public class Segment implements Serializable {

    private Integer colId;
    private byte colClass;
    private String codeNameOfSet;
    private Integer counter;

    public Segment(){

    }

    public Segment(Integer colId, byte colClass, Integer counter){
        this.colId = colId;
        this.colClass = colClass;
        this.counter = counter;
    }

    public Segment(Integer colId, byte colClass, String codeNameOfSet, Integer counter){
        this.colId = colId;
        this.colClass = colClass;
        this.codeNameOfSet = codeNameOfSet;
        this.counter = counter;

    }

    public Integer getColId() {
        return colId;
    }

    public void setColId(Integer colId) {
        this.colId = colId;
    }

    public byte getColClass() {
        return colClass;
    }

    public void setColClass(byte colClass) {
        this.colClass = colClass;
    }

    public String getCodeNameOfSet() { return codeNameOfSet; }

    public void setCodeNameOfSet(String codeNameOfSet) { this.codeNameOfSet = codeNameOfSet; }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }
}
