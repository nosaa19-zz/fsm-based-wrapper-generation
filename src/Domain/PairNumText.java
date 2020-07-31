/*
 * Data structure for NeedlemanWunsch with string similarity.
 */
package Domain;

import java.util.Comparator;

public class PairNumText implements Comparable<PairNumText>, Comparator<PairNumText> {

    private String Code;
    private int Index;

    public PairNumText() {

    }

    public PairNumText(String Code, int Index) {
        this.Code = Code;
        this.Index = Index;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }

    @Override
    public int compareTo(PairNumText o) {
        return Index - o.getIndex();
    }

    @Override
    public int compare(PairNumText o1, PairNumText o2) {
        return o1.getIndex()-o2.getIndex();
    }
}
