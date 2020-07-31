package Domain;

import java.util.ArrayList;
import java.util.List;

public class FSMVerification extends Element {

    ArrayList<State> candidateList;

    State finalState;

    public FSMVerification(){}

    public FSMVerification(Element o){
        this.setLeafIndex(o.getLeafIndex());
        this.setPathId(o.getPathId());
        this.setSimSeqId(o.getSimSeqId());
        this.setPTypeSetId(o.getPTypeSetId());
        this.setTypeSetId(o.getTypeSetId());
        this.setContentId(o.getContentId());
        this.setContent(o.getContent());
        this.candidateList = new ArrayList<>();
    }

    public FSMVerification(ArrayList<State> list, State state){
        this.candidateList = list;
        this.finalState = state;
    }

    public ArrayList<State> getCandidateList() {
        return candidateList;
    }

    public void setCandidateList(ArrayList<State> candidateList) {
        this.candidateList = candidateList;
    }

    public State getFinalState() {
        return finalState;
    }

    public void setFinalState(State finalState) {
        this.finalState = finalState;
    }
}
