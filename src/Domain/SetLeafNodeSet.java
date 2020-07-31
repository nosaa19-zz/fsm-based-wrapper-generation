package Domain;

import java.util.ArrayList;
import java.util.List;

public class SetLeafNodeSet {

    ArrayList<LeafNodeSet> leafNodeSets;

    ArrayList<String> codes;

    public ArrayList<LeafNodeSet> getLeafNodeSets() {
        return leafNodeSets;
    }

    public void setLeafNodeSets(ArrayList<LeafNodeSet> leafNodeSets) {
        this.leafNodeSets = leafNodeSets;
    }

    public void addLeafNodeSets(LeafNodeSet leafNode){
        this.leafNodeSets.add(leafNode);
    }

    public void removeLeafNodeSets(LeafNodeSet leafNode){
        this.leafNodeSets.remove(leafNode);
    }

    public ArrayList<String> getCodes() {
        return codes;
    }

    public void setCodes(ArrayList<String> codes) {
        this.codes = codes;
    }

    public void addCodes(String code){
        this.codes.add(code);
    }

    public void removeCodes(String code){
        this.codes.remove(code);
    }
}
