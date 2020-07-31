package Domain;/*
 * LeafNode class is used to save a leaf node type and leaf nodes from all document that are in the same cluster.
 * Leaf node type: 1=Mandatory Template (MT), 2=Optional Template (OT), 3=Mandatory Data (MD),
 *                 4=Optional Data (OD), 5=Mandatory Set (MS), 6=Optional Set (0S), 7=Mandatory Composite (MC), 8=Optional Composite (0C)
 * StartRec: 1: RT, 2: RD, 3: SR (Start Record), 4: ER (End Record), 5: SE: (Start and End Record), 6: has char similarity,
 *           7: ST, 11: SDR, 12: EDR, 13: ESDR, 14: ED, 15:STD, 16: ETD
 */

import java.util.ArrayList;

/**
 * @author Oviliani
 */
public class PrepNode {

    private ArrayList<String> PathId;
    private ArrayList<String> SimSeqId;
    private ArrayList<String> TypeSetId;
    private ArrayList<String> PTypeSetId;
    private ArrayList<String> SimContent; //We use this to make sure for moving the leaf node
    private ArrayList<String> ContentId;
    private String[] LNMember;
    private byte LNType;
    private byte Encoding;
    private int Counter;
    private byte StartRec;

    public PrepNode() {

    }

    public ArrayList<String> getPathId() {
        return PathId;
    }

    public void setPathId(ArrayList<String> pathId) {
        PathId = pathId;
    }

    public ArrayList<String> getSimSeqId() {
        return SimSeqId;
    }

    public void setSimSeqId(ArrayList<String> simSeqId) {
        SimSeqId = simSeqId;
    }

    public ArrayList<String> getTypeSetId() {
        return TypeSetId;
    }

    public void setTypeSetId(ArrayList<String> typeSetId) {
        TypeSetId = typeSetId;
    }

    public ArrayList<String> getPTypeSetId() {
        return PTypeSetId;
    }

    public void setPTypeSetId(ArrayList<String> PTypeSetId) {
        this.PTypeSetId = PTypeSetId;
    }

    public ArrayList<String> getSimContent() {
        return SimContent;
    }

    public void setSimContent(ArrayList<String> simContent) {
        SimContent = simContent;
    }

    public ArrayList<String> getContentId() {
        return ContentId;
    }

    public void setContentId(ArrayList<String> contentId) {
        ContentId = contentId;
    }

    public String[] getLNMember() {
        return LNMember;
    }

    public void setLNMember(String[] LNMember) {
        this.LNMember = LNMember;
    }

    public byte getLNType() {
        return LNType;
    }

    public void setLNType(byte LNType) {
        this.LNType = LNType;
    }

    public byte getEncoding() {
        return Encoding;
    }

    public void setEncoding(byte encoding) {
        Encoding = encoding;
    }

    public int getCounter() {
        return Counter;
    }

    public void setCounter(int counter) {
        Counter = counter;
    }

    public byte getStartRec() {
        return StartRec;
    }

    public void setStartRec(byte startRec) {
        StartRec = startRec;
    }

    public void resetCounter() {
        this.Counter = 0;
    }

    public void addLNMember(int Pos, String Content, String PathId, String ContentId) {//used in ReSegmenting
        this.LNMember[Pos] = Content;

        /*if ((Pos==1 && "92".equals(ContentId)) || (Pos==9 && "97".equals(ContentId))) {
                    System.out.println("ovi");
                }*/
        if (this.Encoding == 0 && !this.PathId.equals(PathId) && !this.ContentId.equals(ContentId)) {
            this.Encoding = 1;
        }

        incCounter();
    }

    public void addLNMember(int Pos, String Content) {
        this.LNMember[Pos] = Content;

        incCounter();
    }

    public void concatenateMember(int Pos, int LNIndex) {
        if (this.LNMember[Pos] == null) {
            this.LNMember[Pos] = Integer.toString(LNIndex);
            incCounter();
        } else {
            this.LNMember[Pos] = this.LNMember[Pos] + " " + Integer.toString(LNIndex);
        }
    }

    public void concatenateMember(int Pos, String Content) {
        if (this.LNMember[Pos] == null) {
            this.LNMember[Pos] = Content;
        } else {
            this.LNMember[Pos] = this.LNMember[Pos] + " " + Content;
        }
    }

    public void incCounter() {
        this.Counter++;

        if (this.Counter == this.LNMember.length) {
            if (this.LNType == 2) {
                this.LNType = 1;
            } else if (this.LNType == 4) {
                this.LNType = 3;
            } else if (this.LNType == 6) {
                this.LNType = 5;
            } else if (this.LNType == 8) {
                this.LNType = 7;
            }
        }
    }

    //Is used for split
    public void deleteMember(int Pos) {
        this.LNMember[Pos] = null;
        this.Counter--;
    }

}
