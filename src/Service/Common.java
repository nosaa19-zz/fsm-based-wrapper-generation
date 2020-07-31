package Service;

import Domain.BaseConstants;
import Domain.LeafNodeSet;
import Domain.PairNumText;
import Domain.SetLeafNodeSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Common {

    public Character getSymbol(byte colClass){
        char result = 'x';

        if(colClass == BaseConstants.COLUMN_CLASS_MT || colClass == BaseConstants.COLUMN_CLASS_OT) return 't';
        else if(colClass == BaseConstants.COLUMN_CLASS_MD || colClass == BaseConstants.COLUMN_CLASS_OD) return 'B';
        else if(colClass == BaseConstants.COLUMN_CLASS_MR || colClass == BaseConstants.COLUMN_CLASS_OR) return 's';
        else if(colClass == BaseConstants.COLUMN_CLASS_MC || colClass == BaseConstants.COLUMN_CLASS_OC) return 'g';

        return result;
    }

    public String decodingLNType(byte lnType){
        String result = "";
        switch (lnType){
            case BaseConstants.COLUMN_CLASS_MT :
                result = "MT";
                break;
            case BaseConstants.COLUMN_CLASS_OT :
                result = "OT";
                break;
            case BaseConstants.COLUMN_CLASS_MD :
                result = "MD";
                break;
            case BaseConstants.COLUMN_CLASS_OD :
                result = "OD";
                break;
            case BaseConstants.COLUMN_CLASS_MR :
                result = "MR";
                break;
            case  BaseConstants.COLUMN_CLASS_OR :
                result = "OR";
                break;
            case BaseConstants.COLUMN_CLASS_MC :
                result = "MC";
                break;
            case BaseConstants.COLUMN_CLASS_OC :
                result = "OC";
                break;
            default:
                result = "NONE";
                break;
        }
        return result;
    }

    public void printOnFile(File file, String text){
        FileWriter fr = null;
        BufferedWriter br = null;
        try {
            // to append to file, you need to initialize FileWriter using below constructor
            fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            br.write(text);
            br.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String [] initArrayWithSpace(int length){

        String [] result = new String[length];

        for(int i = 0; i < length; i++){
            result[i] = " ";
        }

        return result;
    }

    public String getSetLine(String [] arrLine, String codeOfSet, int countLine){
        String result = codeOfSet +"-"+countLine+"\t";

        for(int i = 0; i < arrLine.length; i++){
            if(i < arrLine.length - 1){
                result = result+arrLine[i]+"\t";
            }
            else {
                result = result+arrLine[i];
            }
        }
        //System.out.println("CETAK ==>"+result);
        return result;
    }

    public void printSchemaOnTableTest(File file, List<LeafNodeSet> table){

        List<LeafNodeSet> tableTest = new ArrayList<>(table);

        String Path = "PathId\t";
        String SimSeq = "SimSeqId\t";
        String PTypeSet = "PTypeSetId\t";
        String TypeSet = "TypeSetId\t";
        String Content = "ContentId\t";
        String Encoding = "Encoding\t";
        String ColType = "Col Type\t";

        for(LeafNodeSet o : tableTest){
            Path = Path + swapNullWithSpace(o.getPathId())+"\t";
            SimSeq = SimSeq + swapNullWithSpace(o.getSimSeqId())+"\t";
            PTypeSet = PTypeSet + swapNullWithSpace(o.getPTypeSetId())+"\t";
            TypeSet = TypeSet + swapNullWithSpace(o.getTypeSetId())+"\t";
            Content = Content + swapNullWithSpace(o.getContentId())+"\t";
            Encoding = Encoding + o.getEncoding()+"\t";
            ColType = ColType + decodingLNType(o.getLNType())+"\t";
        }

        printOnFile(file, Path);
        printOnFile(file, SimSeq);
        printOnFile(file, PTypeSet);
        printOnFile(file, TypeSet);
        printOnFile(file, Content);
        printOnFile(file, Encoding);
        printOnFile(file, ColType);
    }

    public void printSchemaOnTableSetTest(File file, List<LeafNodeSet> table){

        List<LeafNodeSet> tableSet = new ArrayList<>(table);

        String Path = "PathId\t";
        String SimSeq = "SimSeqId\t";
        String PTypeSet = "PTypeSetId\t";
        String TypeSet = "TypeSetId\t";
        String Content = "ContentId\t";
        String Encoding = "Encoding\t";
        String ColType = "Col Type\t";

        for(LeafNodeSet o : tableSet){
            Path = Path + swapNullWithSpace(o.getPathId())+"\t";
            SimSeq = SimSeq + swapNullWithSpace(o.getSimSeqId())+"\t";
            PTypeSet = PTypeSet + swapNullWithSpace(o.getPTypeSetId())+"\t";
            TypeSet = TypeSet + swapNullWithSpace(o.getTypeSetId())+"\t";
            Content = Content + swapNullWithSpace(o.getContentId())+"\t";
            Encoding = Encoding + o.getEncoding()+"\t";
            ColType = ColType + decodingLNType(o.getLNType())+"\t";
        }

        printOnFile(file, Path);
        printOnFile(file, SimSeq);
        printOnFile(file, PTypeSet);
        printOnFile(file, TypeSet);
        printOnFile(file, Content);
        printOnFile(file, Encoding);
        printOnFile(file, ColType);
    }

    public String printVerificationFailure(String filename, int tableASize){
        String result = filename+"\t";

        List<String> leafnodes = new ArrayList<>();

        for(int i = 0; i < tableASize; i++){
            leafnodes.add(" ");
        }

        for(int i = 0; i < leafnodes.size(); i++){

            if(i == leafnodes.size()-1){
                result = result+ leafnodes.get(i);
            }
            else {
                result = result+ leafnodes.get(i)+"\t";
            }
        }
        return result;
    }

    private String swapNullWithSpace(String[] value){
        if(value == null) return " ";
        String result = "";

        for(int i = 0; i < value.length; i++){
            if(i < (value.length - 1)) result = result+ value[i]+ ",";
            else result = result+value[i];
        }

        return result;
    }

    private String swapNullWithSpace(String value){
        if(value == null) return " ";
        return value;
    }

    public void CheckFolder(String Folder, String Sub) {
        File CheckingFolder = new File(Folder+Sub);
        if (!CheckingFolder.exists()) {
            CheckingFolder.mkdirs();
        }
        else {
            deleteDir(new File(Folder));
            CheckingFolder.mkdirs();
        }
    }

    public void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if(files != null) {
            for (final File file : files) {
                deleteDir(file);
            }
        }
        dir.delete();
    }

}
