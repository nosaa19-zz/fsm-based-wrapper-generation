package Domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FSM implements Serializable{

    private Integer FSMId;

    private ArrayList<FSMDetail> details;

    private Boolean isContainSet;

    private Integer numberOfSet;

    public FSM (Integer fsmId, ArrayList<FSMDetail> det, Integer numberOfSet){
        this.FSMId = fsmId;
        this.details = det;
        this.numberOfSet = numberOfSet;
    }

    public Integer getFSMId() {
        return FSMId;
    }

    public void setFSMId(Integer FSMId) {
        this.FSMId = FSMId;
    }

    public ArrayList<FSMDetail> getDetails() {
        return details;
    }

    public void setDetails(ArrayList<FSMDetail> details) {
        this.details = details;
    }

    public void addDetails(FSMDetail detail){
        this.details.add(detail);
    }

    public void removeDetails(FSMDetail detail){
        this.details.remove(detail);
    }

    public Boolean getContainSet() { return isContainSet; }

    public void setContainSet(Boolean containSet) { isContainSet = containSet; }

    public Integer getNumberOfSet() { return numberOfSet; }

    public void setNumberOfSet(Integer numberOfSet) { this.numberOfSet = numberOfSet; }
}
