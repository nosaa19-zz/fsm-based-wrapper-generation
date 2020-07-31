package Domain;

import java.util.ArrayList;

public class Schema {

    private ArrayList<LeafNodeSet> tableA;

    private ArrayList<SetLeafNodeSet> tableSetList;

    public ArrayList<LeafNodeSet> getTableA() {
        return tableA;
    }

    public void setTableA(ArrayList<LeafNodeSet> tableA) {
        this.tableA = tableA;
    }

    public ArrayList<SetLeafNodeSet> getTableSetList() {
        return tableSetList;
    }

    public void setTableSetList(ArrayList<SetLeafNodeSet> tableSetList) {
        this.tableSetList = tableSetList;
    }
}
