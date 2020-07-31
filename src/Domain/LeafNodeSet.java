package Domain;/*

 * LeafNode class is used to save a leaf node type and leaf nodes from all document that are in the same cluster.
 * Leaf node type: 1=Mandatory Template (MT), 2=Optional Template (OT), 3=Mandatory Data (MD),
 *                 4=Optional Data (OD), 5=Mandatory Set (MS), 6=Optional Set (0S), 7=Mandatory Composite (MC), 8=Optional Composite (0C)
 * StartRec: 1: RT, 2: RD, 3: SR (Start Record), 4: ER (End Record), 5: SE: (Start and End Record), 6: has char similarity,
 *           7: ST, 11: SDR, 12: EDR, 13: ESDR, 14: ED, 15:STD, 16: ETD
 */

import java.util.ArrayList;

public class LeafNodeSet {

    private String[] PathId;
    private String[] SimSeqId;
    private String[] PTypeSetId;
    private String[] TypeSetId;
    private String[] ContentId;
    private String[] TECId;
    private String CECId;
    private String[] LNMember;
    private Byte LNType;
    private Byte Encoding;
    private Byte StartRec;

    public LeafNodeSet(){

    }

    public LeafNodeSet(String[] PathId,
                       String[] SimSeqId,
                       String[] PTypeSetId,
                       String[] TypeSetId,
                       String[] ContentId,
                       String[] LNMember,
                       Byte LNType,
                       Byte Encoding,
                       Byte StartRec){
        this.PathId = PathId;
        this.SimSeqId = SimSeqId;
        this.PTypeSetId = PTypeSetId;
        this.TypeSetId = TypeSetId;
        this.ContentId = ContentId;
        this.LNMember = LNMember;
        this.LNType = LNType;
        this.Encoding = Encoding;
        this.StartRec = StartRec;

    }

    public LeafNodeSet(Element element){
        this.PathId = new String[]{element.getPathId()};
        this.SimSeqId = new String[]{element.getSimSeqId()};
        this.PTypeSetId = new String[]{element.getPTypeSetId()};
        this.TypeSetId =  new String[] {element.getTypeSetId()};
        this.ContentId = new String[]{element.getContentId()};
    }

    public String[] getPathId() {
        return PathId;
    }

    public void setPathId(String[] pathId) {
        PathId = pathId;
    }

    public String[] getSimSeqId() { return SimSeqId; }

    public void setSimSeqId(String[] simSeqId) { SimSeqId = simSeqId; }

    public String[] getPTypeSetId() { return PTypeSetId; }

    public void setPTypeSetId(String[] PTypeSetId) { this.PTypeSetId = PTypeSetId; }

    public String[] getTypeSetId() { return TypeSetId; }

    public void setTypeSetId(String[] typeSetId) { TypeSetId = typeSetId; }

    public String[] getContentId() {
        return ContentId;
    }

    public void setContentId(String[]contentId) {
        ContentId = contentId;
    }

    public String[] getTECId() { return TECId; }

    public void setTECId(String[] TECId) { this.TECId = TECId; }

    public String getCECId() { return CECId; }

    public void setCECId(String CECId) { this.CECId = CECId; }

    public String[] getLNMember() {
        return LNMember;
    }

    public void setLNMember(String[] LNMember) {
        this.LNMember = LNMember;
    }

    public Byte getLNType() {
        return LNType;
    }

    public void setLNType(Byte LNType) {
        this.LNType = LNType;
    }

    public Byte getEncoding() {
        return Encoding;
    }

    public void setEncoding(Byte encoding) {
        Encoding = encoding;
    }

    public int getCounter() {
        int count = 0;
        if(this.LNMember != null){
            for (int i = 0; i < this.LNMember.length; i++){
                if(!this.LNMember[i].equals(" ")) {
                    count++;
                }
            }
        }
        return count;
    }

    public byte getStartRec() {
        return StartRec;
    }

    public void setStartRec(Byte startRec) {
        StartRec = startRec;
    }

}
