/*
 * Element Class is used for save the property of a path tag and a node properties.
 */
package Domain;

/**
 * @author Oviliani
 */
public class Element implements Comparable<Element> {

    private String LeafIndex;
    private String PathId;
    private String SimSeqId;
    private String PTypeSetId;
    private String TypeSetId;
    private String ContentId;
    private String Content;

    public Element(){}

    public Element(String leafIndex, String pathId, String simSeqId, String pTypeSetId, String typeSetId, String contentId, String content){
        this.LeafIndex = leafIndex;
        this.PathId= pathId;
        this.SimSeqId = simSeqId;
        this.PTypeSetId = pTypeSetId;
        this.TypeSetId = typeSetId;
        this.ContentId = contentId;
        this.Content = content;
    }

    public String getLeafIndex() {
        return LeafIndex;
    }

    public void setLeafIndex(String leafIndex) {
        LeafIndex = leafIndex;
    }

    public String getPathId() {
        return PathId;
    }

    public void setPathId(String pathId) {
        PathId = pathId;
    }

    public String getSimSeqId() { return SimSeqId; }

    public void setSimSeqId(String simSeqId) { SimSeqId = simSeqId; }

    public String getPTypeSetId() { return PTypeSetId; }

    public void setPTypeSetId(String PTypeSetId) { this.PTypeSetId = PTypeSetId; }

    public String getTypeSetId() {
        return TypeSetId;
    }

    public void setTypeSetId(String typeSetId) {
        TypeSetId = typeSetId;
    }

    public String getContentId() {
        return ContentId;
    }

    public void setContentId(String contentId) {
        ContentId = contentId;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    @Override
    public int compareTo(Element o) {
        return extractInt(this.LeafIndex).compareTo(extractInt(o.getLeafIndex()));
    }

    Integer extractInt(String s) {
        String num = s.replaceAll("\\D", "");
        // return 0 if no digits found
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }
}
