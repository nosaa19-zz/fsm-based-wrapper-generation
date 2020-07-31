package Domain;

import java.io.Serializable;

public class SegmentVerification implements Serializable{

    Integer colIdTrain;

    Integer colIdTest;

    public SegmentVerification(Integer colIdTrain, Integer colIdTest){
        this.colIdTrain = colIdTrain;
        this.colIdTest = colIdTest;
    }

    public Integer getColIdTrain() {
        return colIdTrain;
    }

    public void setColIdTrain(Integer colIdTrain) {
        this.colIdTrain = colIdTrain;
    }

    public Integer getColIdTest() {
        return colIdTest;
    }

    public void setColIdTest(Integer colIdTest) {
        this.colIdTest = colIdTest;
    }
}
