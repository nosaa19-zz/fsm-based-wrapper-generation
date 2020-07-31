package Domain;

import java.io.Serializable;

public class FSMDetail implements Serializable {

    private Integer segmentId;

    private Integer colIdFrom;

    private char colSymbolFrom;

    private String codeNameOfSetFrom;

    private Integer colIdTo;

    private char colSymbolTo;

    private String codeNameOfSetTo;

    private Double probability;


    public FSMDetail(Integer segmentId,
                     Integer colIdFrom,
                     char colSymbolFrom,
                     String codeNameOfSetFrom,
                     Integer colIdTo,
                     char colSymbolTo,
                     String codeNameOfSetTo,
                     Double probability)
    {
        this.segmentId = segmentId;
        this.colIdFrom = colIdFrom;
        this.colSymbolFrom = colSymbolFrom;
        this.codeNameOfSetFrom = codeNameOfSetFrom;
        this.colIdTo = colIdTo;
        this.colSymbolTo = colSymbolTo;
        this.codeNameOfSetTo = codeNameOfSetTo;
        this.probability = probability;
    }

    public Integer getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(Integer segmentId) {
        this.segmentId = segmentId;
    }

    public Integer getColIdFrom() {
        return colIdFrom;
    }

    public void setColIdFrom(Integer colIdFrom) {
        this.colIdFrom = colIdFrom;
    }

    public char getColSymbolFrom() {
        return colSymbolFrom;
    }

    public void setColSymbolFrom(char colSymbolFrom) {
        this.colSymbolFrom = colSymbolFrom;
    }

    public String getCodeNameOfSetFrom() { return codeNameOfSetFrom; }

    public void setCodeNameOfSetFrom(String codeNameOfSetFrom) { this.codeNameOfSetFrom = codeNameOfSetFrom; }

    public Integer getColIdTo() {
        return colIdTo;
    }

    public void setColIdTo(Integer colIdTo) {
        this.colIdTo = colIdTo;
    }

    public char getColSymbolTo() {
        return colSymbolTo;
    }

    public void setColSymbolTo(char colSymbolTo) {
        this.colSymbolTo = colSymbolTo;
    }

    public String getCodeNameOfSetTo() { return codeNameOfSetTo; }

    public void setCodeNameOfSetTo(String codeNameOfSetTo) { this.codeNameOfSetTo = codeNameOfSetTo; }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }
}
