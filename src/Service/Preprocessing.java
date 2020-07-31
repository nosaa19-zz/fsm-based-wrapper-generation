package Service;

import Domain.PrepNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessing {

    int DSize = 0;
    int DSetSize = 0;
    String Folder;

    public void collectFeatures(String Folder) throws Exception {

        this.Folder = Folder;

        Common common = new Common();
        common.CheckFolder(Folder+"FSM/", "TXT");

        /*File checkFile = new File(Folder + "FSM/TXT/TableA-Index.txt");
        if(checkFile.exists()){
            return;
        }*/

        ArrayList<PrepNode> tableAContent = new ArrayList<>();
        ArrayList<PrepNode> tableALeafIndex = new ArrayList<>();
        ArrayList<String> curLineAList;
        String currentLine;
        String[] curLineArr;
        String[] LNMember;
        int lineNo = 1;
        int numSet = 0;
        int iFP;
        byte colmType;

        //Initial DSize
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(Folder + "Output/TXT/TableA.txt"), "UTF8"));) {
            while ((currentLine = br.readLine()) != null) {
                if(currentLine.contains("-oO End of Aligned Table Oo-")) break;

                DSize++;
            }
            DSize = DSize - 11;
        }

        //Split Content and LeafIndex
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(Folder + "Output/TXT/TableA.txt"), "UTF8"));) {

            while ((currentLine = br.readLine()) != null) {

                if(currentLine.contains("-oO End of Aligned Table Oo-")) break;

                curLineArr = currentLine.split("\t");
                if (lineNo == 6) {
                    for (int i = 1; i < curLineArr.length; i++) {
                        tableAContent.add(new PrepNode());
                        tableALeafIndex.add(new PrepNode());
                    }
                } else if (lineNo == 10){
                    for (int i = 1; i < curLineArr.length; i++) {
                        tableAContent.get(i-1).setEncoding(Byte.valueOf(curLineArr[i]));
                        tableALeafIndex.get(i-1).setEncoding(Byte.valueOf(curLineArr[i]));
                    }

                } else if (lineNo == 11) {
                    for (int i = 1; i < curLineArr.length; i++) {

                        colmType = getColumnType(curLineArr[i].trim());
                        if(colmType == 5 || colmType == 6){
                            numSet++;
                        }

                        tableAContent.get(i-1).setLNType((byte) colmType);
                        tableALeafIndex.get(i-1).setLNType((byte) colmType);
                    }
                } else if (lineNo > 11) {
                    for (int i = 1; i < curLineArr.length; i++) {
                        if (tableAContent.get(i-1).getLNMember() == null) {
                            LNMember = new String[DSize];
                            tableAContent.get(i-1).setLNMember(LNMember.clone());
                            tableALeafIndex.get(i-1).setLNMember(LNMember.clone());
                        }

                        if (curLineArr[i].contains(" :: ")) {
                            curLineAList = new ArrayList(Arrays.asList(curLineArr[i].split("( :: )")));
                            if (curLineAList.size() == 1) {
                                continue;
                            } else if ((curLineAList.size() % 2) > 0) { // maybe " :: " is exist part of Content, return back
                                int iA = 1;
                                String Tmp;
                                while (iA < curLineAList.size() - 1) {
                                    Tmp = curLineAList.get(0) + " :: " + curLineAList.get(iA);
                                    curLineAList.set(0, Tmp);
                                    curLineAList.remove(iA);
                                }
                            }

                            iFP = 0;
                            while (iFP < curLineAList.size()) {
                                if (!curLineAList.get(iFP).trim().equals("") && !curLineAList.get(iFP).equals("-1")) {
                                    //System.out.println("1 " + i + " " + iFP + " " + CurLineAList.get(iFP));
                                    if ((iFP % 2) == 0) {
                                        tableAContent.get(i-1).concatenateMember((lineNo - 12), curLineAList.get(iFP));
                                    }

                                    iFP++;

                                    if ((iFP % 2) != 0) {
                                        tableALeafIndex.get(i-1).concatenateMember((lineNo - 12), curLineAList.get(iFP));
                                    }
                                }
                                iFP++;
                            }
                        } else {//Store Set item
                            //System.out.println("1 " + i + " " + 1 + " " + CurLineArr[i]);
                            tableAContent.get(i-1).concatenateMember((lineNo - 12), curLineArr[i]);

                            tableALeafIndex.get(i-1).concatenateMember((lineNo - 12), curLineArr[i]);
                        }
                    }
                }

                lineNo++;
            }
        }

        updateWrongLNType(tableALeafIndex);
        tableALeafIndex = collectFeatureFromDoc(tableALeafIndex);

        if(numSet > 0){
            RunSets(numSet);
        }
        //PrintTableA(tableAContent, "TableA-Content.txt");
        //UpdateEncoding(tableALeafIndex); //-< STILL NOT NECESSARY NEED UPDATE ENCODING
        printTableA(tableALeafIndex, "FSM/TXT/TableA-Index.txt");

    }

    private ArrayList<PrepNode> collectFeatureFromDoc(List<PrepNode> TableA){

        ArrayList<PrepNode> result = new ArrayList<>(TableA);
        ArrayList<File> selectedfiles = new ArrayList<>();

        File files = new File(Folder + "Output/TXT/"); //folder of train document
        File[] outputFiles = files.listFiles();

        if (!files.exists()) {
            System.out.println("Folder not found!");
        } else if (outputFiles.length > 0) {

            for (int iOutputDoc = 0; iOutputDoc < outputFiles.length; iOutputDoc++) {
                if(outputFiles[iOutputDoc].getName().contains("page-")){
                    selectedfiles.add(outputFiles[iOutputDoc]);
                }
            }

            outputFiles = selectedfiles.toArray(new File[0]);
            Arrays.sort(outputFiles);

            for(PrepNode data : result){
                if(data.getLNType() == 5 || data.getLNType() == 6 ){
                    // Do Nothing
                }
                else {
                    data.setPathId(getFeature(outputFiles, data.getLNMember(), 'H'));
                    data.setSimSeqId(getFeature(outputFiles, data.getLNMember(), 'S'));
                    data.setPTypeSetId(getFeature(outputFiles, data.getLNMember(), 'P'));
                    data.setTypeSetId(getFeature(outputFiles, data.getLNMember(), 'T'));

                    if(data.getLNType() == 1 || data.getLNType() ==  2){
                        data.setContentId(getFeature(outputFiles, data.getLNMember(), 'C'));
                    }
                }
            }
        }

        return result;
    }

    private ArrayList<PrepNode> collectSetFeatureFromDoc(List<PrepNode> TableA, ArrayList<String> code){

        ArrayList<PrepNode> result = new ArrayList<>(TableA);
        ArrayList<File> selectedfiles = new ArrayList<>();

        File files = new File(Folder + "Output/TXT/"); //folder of train document
        File[] outputFiles = files.listFiles();
        if (!files.exists()) {
            System.out.println("Folder not found!");
        } else if (outputFiles.length > 0) {

            for (int iOutputDoc = 0; iOutputDoc < outputFiles.length; iOutputDoc++) {
                if(outputFiles[iOutputDoc].getName().contains("page-")){
                    selectedfiles.add(outputFiles[iOutputDoc]);
                }
            }

            outputFiles = selectedfiles.toArray(new File[0]);

            for(PrepNode data : result){
                if(data.getLNType() == 5 || data.getLNType() == 6 ){

                }
                else {
                    data.setPathId(getSetFeature(outputFiles, data.getLNMember(), 'H', code));
                    data.setSimSeqId(getSetFeature(outputFiles, data.getLNMember(), 'S', code));
                    data.setPTypeSetId(getSetFeature(outputFiles, data.getLNMember(), 'P', code));
                    data.setTypeSetId(getSetFeature(outputFiles, data.getLNMember(), 'T', code));

                    if(data.getLNType() == 1 || data.getLNType() ==  2){
                        data.setContentId(getSetFeature(outputFiles, data.getLNMember(), 'C', code));
                    }
                }
            }
        }

        return result;
    }

    private void updateWrongLNType(List<PrepNode> TableA){
        for(PrepNode data :  TableA){
            byte currEncoding = data.getEncoding();

            if(currEncoding == 0) {
                if(data.getLNType() == 3){
                    data.setLNType((byte) 1);
                }
                else if(data.getLNType() == 4){
                    data.setLNType((byte) 2);
                }
            }
        }
    }

    private void updateEncoding(List<PrepNode> TableA){

        for(PrepNode data :  TableA){
            byte bestEncoding, currEncoding = data.getEncoding();
            int min = 0;

            if(currEncoding == -1 || currEncoding == 0){
                continue;
            }
            else {
                int currCombination = getCombinationSize(data, currEncoding);
                min = currCombination;
                bestEncoding = currEncoding;

                for(byte i = 1; i <= 6; i++){
                    if(i == currEncoding) {
                        continue;
                    }
                    else {
                        if(min > getCombinationSize(data, i)){
                            min = getCombinationSize(data, i);
                            bestEncoding = i;
                        }
                    }
                }

                data.setEncoding(bestEncoding);
            }

        }
    }

    private Integer getCombinationSize(PrepNode leafNodeSet, Byte encoding){
        Integer length = null;

        switch(encoding) {
            case 0: //PathId - ContentId
                length = leafNodeSet.getPathId().size();
                break;
            case 1: //SimSeqId - TypeSetId
                length = leafNodeSet.getSimSeqId().size() * leafNodeSet.getTypeSetId().size();
                break;
            case 2: //SimSeqId - PTypeSetId
                length = leafNodeSet.getSimSeqId().size() * leafNodeSet.getPTypeSetId().size();
                break;
            case 3: //SimSeqId
                length = leafNodeSet.getSimSeqId().size();
                break;
            case 4: //PathId - TypeSetId
                length = leafNodeSet.getPathId().size() * leafNodeSet.getTypeSetId().size();
                break;
            case 5: //PathId - PTypeSetId
                length = leafNodeSet.getPathId().size() * leafNodeSet.getPTypeSetId().size();
                break;
            case 6: //PathId
                length = leafNodeSet.getPathId().size();
                break;
        }
        return length;
    }

    private ArrayList<String> getFeature(File[] files, String[] lnMember, Character key){
        ArrayList<String> result = new ArrayList<>();

        List<String[]> lines;

        for(int i = 0; i < lnMember.length; i++){
            lines = getDataFromTableL(files, lnMember[i], i);
            for(String[] lineArr : lines){
                if(key.equals('H')){
                    // path
                    int index = Collections.binarySearch(result, lineArr[9]);
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[9]);
                    }
                }
                else if(key.equals('S')){
                    // sim
                    int index = Collections.binarySearch(result, lineArr[10]);
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[10]);
                    }
                }

                else if(key.equals('P')){
                    // parent
                    int index = Collections.binarySearch(result, lineArr[6]);
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[6]);
                    }
                }

                else if(key.equals('T')){
                    // typeset
                    int index = Collections.binarySearch(result, lineArr[7]);
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[7]);
                    }
                }

                else if(key.equals('C')){
                    // content
                    int index = Collections.binarySearch(result, lineArr[8].substring(lineArr[8].indexOf("-") + 1));
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[8].substring(lineArr[8].indexOf("-") + 1));
                    }
                }
            }
        }

        return result;
    }

    private ArrayList<String> getSetFeature(File[] files, String[] lnMember, Character key, ArrayList<String> code){
        ArrayList<String> result = new ArrayList<>();

        List<String[]> lines;

        for(int i = 0; i < lnMember.length; i++){
            String[] arr = code.get(i).split("-");
            lines = getDataFromTableL(files, lnMember[i], Integer.parseInt(arr[1]));
            for(String[] lineArr : lines){
                if(key.equals('H')){
                    // path
                    int index = Collections.binarySearch(result, lineArr[9]);
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[9]);
                    }
                }
                else if(key.equals('S')){
                    // sim
                    int index = Collections.binarySearch(result, lineArr[10]);
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[10]);
                    }
                }

                else if(key.equals('P')){
                    // parent
                    int index = Collections.binarySearch(result, lineArr[6]);
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[6]);
                    }
                }

                else if(key.equals('T')){
                    // typeset
                    int index = Collections.binarySearch(result, lineArr[7]);
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[7]);
                    }
                }

                else if(key.equals('C')){
                    // content
                    int index = Collections.binarySearch(result, lineArr[8].substring(lineArr[8].indexOf("-") + 1));
                    if(index < 0) {
                        result.add(Math.abs(index)-1, lineArr[8].substring(lineArr[8].indexOf("-") + 1));
                    }
                }
            }
        }

        return result;
    }

    private List<String[]> getDataFromTableL(File[] files, String lnMember, int iDoc){

        List<String[]> result = new ArrayList<>();
        String[] CurLineArr;
        String line = "";
        String[] members = lnMember.split(" ");
        try (BufferedReader buf = new BufferedReader(
                new InputStreamReader(new FileInputStream(files[iDoc]), "UTF8"));) {
            int count = 0;
            while ((line=buf.readLine())!=null){
                if(members.length > 0){
                    if(count > 0){
                        CurLineArr = line.split("\t");

                        for(int i = 0; i < members.length; i++){
                            if(CurLineArr[0].equals(members[i]))
                            {
                                result.add(CurLineArr);
                            }
                        }
                    }
                    count++;

                    if (result.size()==members.length){
                        break;
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private void printTableA(ArrayList<PrepNode> Table, String FileName) throws FileNotFoundException, UnsupportedEncodingException {
        PrepNode aRow;
        String TypeLN = "";
        String[] LNMember;

        PrintStream fileStream;
        fileStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(Folder + FileName, false)), true, "UTF-8");
        System.setOut(fileStream);

        //Show PathId
        System.out.print("PathId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getPathId()) + "\t");
        }
        System.out.println();

        //Show SimSeqId
        System.out.print("SimSeqId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getSimSeqId()) + "\t");
        }
        System.out.println();

        //Show PTypeSetId
        System.out.print("PTypeSetId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getPTypeSetId()) + "\t");
        }
        System.out.println();

        //Show TypeSetId
        System.out.print("TypeSetId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getTypeSetId()) + "\t");
        }
        System.out.println();

        //Show ContentId
        System.out.print("ContentId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getContentId()) + "\t");
        }
        System.out.println();

        //Show Encoding
        System.out.print("Encoding\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(aRow.getEncoding() + "\t");
        }
        System.out.println();

        //Show ColType
        System.out.print("ColType\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            switch (aRow.getLNType()) {
                case 1:
                    TypeLN = "MT";
                    break;
                case 2:
                    TypeLN = "OT";
                    break;
                case 3:
                    TypeLN = "MD";
                    break;
                case 4:
                    TypeLN = "OD";
                    break;
                case 5:
                    TypeLN = "MR";
                    break;
                case 6:
                    TypeLN = "OR";
                    break;
                case 7:
                    TypeLN = "MC";
                    break;
                case 8:
                    TypeLN = "OC";
                    break;
            }
            System.out.print(TypeLN + "\t");
        }
        System.out.println();

        for (int Page = 0; Page < DSize; Page++) {
            System.out.print(Page+"\t");
            for (int i = 0; i < Table.size(); i++) {
                aRow = Table.get(i);
                LNMember = aRow.getLNMember();

                if (LNMember[Page] == null) {
                    System.out.print(" \t");
                } else {
                    System.out.print(LNMember[Page] + "\t"); //this is for next step
                }
            }
            System.out.println();
        }
        System.setOut(System.out);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }

    private void printSet(ArrayList<PrepNode> Table, String FileName, ArrayList<String> code) throws FileNotFoundException, UnsupportedEncodingException {
        PrepNode aRow;
        String TypeLN = "";
        String[] LNMember;

        PrintStream fileStream;
        fileStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(Folder + FileName, false)), true, "UTF-8");
        System.setOut(fileStream);

        //Show PathId
        System.out.print("PathId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getPathId()) + "\t");
        }
        System.out.println();

        //Show SimSeqId
        System.out.print("SimSeqId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getSimSeqId()) + "\t");
        }
        System.out.println();

        //Show PTypeSetId
        System.out.print("PTypeSetId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getPTypeSetId()) + "\t");
        }
        System.out.println();

        //Show TypeSetId
        System.out.print("TypeSetId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getTypeSetId()) + "\t");
        }
        System.out.println();

        //Show ContentId
        System.out.print("ContentId\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(swapNullWithSpace(aRow.getContentId()) + "\t");
        }
        System.out.println();

        //Show Encoding
        System.out.print("Encoding\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            System.out.print(aRow.getEncoding() + "\t");
        }
        System.out.println();

        //Show ColType
        System.out.print("ColType\t");
        for (int i = 0; i < Table.size(); i++) {
            aRow = Table.get(i);
            switch (aRow.getLNType()) {
                case 1:
                    TypeLN = "MT";
                    break;
                case 2:
                    TypeLN = "OT";
                    break;
                case 3:
                    TypeLN = "MD";
                    break;
                case 4:
                    TypeLN = "OD";
                    break;
                case 5:
                    TypeLN = "MR";
                    break;
                case 6:
                    TypeLN = "OR";
                    break;
                case 7:
                    TypeLN = "MC";
                    break;
                case 8:
                    TypeLN = "OC";
                    break;
            }
            System.out.print(TypeLN + "\t");
        }
        System.out.println();

        for (int Page = 0; Page < DSetSize; Page++) {
            System.out.print(code.get(Page)+"\t");
            for (int i = 0; i < Table.size(); i++) {
                aRow = Table.get(i);
                LNMember = aRow.getLNMember();

                if (LNMember[Page] == null) {
                    System.out.print(" \t");
                } else {
                    System.out.print(LNMember[Page] + "\t"); //this is for next step
                }
            }
            System.out.println();
        }

        System.out.close();
        System.setOut(System.out);
    }

    private String swapNullWithSpace(ArrayList<String> value){
        if(value == null) return " ";

        String result = "";

        for(int i = 0; i < value.size(); i++){
            if(i < (value.size() - 1)) result = result+ value.get(i)+ ",";
            else result = result+value.get(i);
        }

        return result;
    }

    private void RunSets(int numSet) throws Exception {
        ArrayList<ArrayList<PrepNode>> listTableSetContent = new ArrayList<>();
        ArrayList<ArrayList<PrepNode>> listTableSetLeafIndex = new ArrayList<>();

        for(int indexSet = 1; indexSet<=numSet; indexSet++){

            ArrayList<String> code = new ArrayList<>();
            String currentLine;
            String[] curLineArr;
            byte colmType;
            String[] LNMember;

            int LineNo = 1;
            DSetSize = 0;
            int iFP;

            ArrayList<PrepNode> tableSetContent = new ArrayList<>();
            ArrayList<PrepNode> tableSetLeafIndex = new ArrayList<>();
            ArrayList<String> curLineAList;

            //Initial DSetSize
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(Folder + "Output/TXT/Set-"+indexSet+".txt"), "UTF8"));) {
                while ((currentLine = br.readLine()) != null) {
                    DSetSize++;
                }
                DSetSize = DSetSize - 9;
            }

            //Split Content and LeafIndex
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(Folder + "Output/TXT/Set-"+indexSet+".txt"), "UTF8"));) {

                while ((currentLine = br.readLine()) != null) {

                    curLineArr = currentLine.split("\t");
                    if (LineNo == 4) {
                        for (int i = 1; i < curLineArr.length; i++) {
                            tableSetContent.add(new PrepNode());
                            tableSetLeafIndex.add(new PrepNode());
                        }
                    } else if (LineNo == 8){
                        for (int i = 1; i < curLineArr.length; i++) {
                            tableSetContent.get(i-1).setEncoding(Byte.valueOf(curLineArr[i]));
                            tableSetLeafIndex.get(i-1).setEncoding(Byte.valueOf(curLineArr[i]));
                        }

                    } else if (LineNo == 9) {
                        for (int i = 1; i < curLineArr.length; i++) {
                            colmType = getColumnType(curLineArr[i].trim());
                            if(colmType == 5 || colmType == 6){
                                numSet++;
                            }
                            tableSetContent.get(i-1).setLNType((byte) colmType);
                            tableSetLeafIndex.get(i-1).setLNType((byte) colmType);
                        }
                    } else if (LineNo > 9) {
                        for (int i = 0; i < curLineArr.length; i++) {
                            if(i == 0){
                                code.add(curLineArr[i]);
                            }
                            else {
                                if (tableSetContent.get(i-1).getLNMember() == null) {
                                    LNMember = new String[DSetSize];
                                    tableSetContent.get(i-1).setLNMember(LNMember.clone());
                                    ///Arrays.fill(LNMember, "-1");
                                    tableSetLeafIndex.get(i-1).setLNMember(LNMember.clone());
                                }

                                if (curLineArr[i].contains(" :: ")) {
                                    curLineAList = new ArrayList(Arrays.asList(curLineArr[i].split("( :: )")));
                                    if (curLineAList.size() == 1) {
                                        continue;
                                    } else if ((curLineAList.size() % 2) > 0) { // maybe " :: " is exist part of Content, return back
                                        int iA = 1;
                                        String Tmp;
                                        while (iA < curLineAList.size() - 1) {
                                            Tmp = curLineAList.get(0) + " :: " + curLineAList.get(iA);
                                            curLineAList.set(0, Tmp);
                                            curLineAList.remove(iA);
                                        }
                                    }

                                    iFP = 0;
                                    while (iFP < curLineAList.size()) {
                                        if (!curLineAList.get(iFP).trim().equals("") && !curLineAList.get(iFP).equals("-1")) {
                                            //System.out.println("1 " + i + " " + iFP + " " + CurLineAList.get(iFP));
                                            if ((iFP % 2) == 0) {
                                                tableSetContent.get(i-1).concatenateMember((LineNo - 10), curLineAList.get(iFP));
                                            }

                                            iFP++;

                                            if ((iFP % 2) != 0) {
                                                tableSetLeafIndex.get(i-1).concatenateMember((LineNo - 10), curLineAList.get(iFP));
                                            }
                                        }
                                        iFP++;
                                    }
                                } else {//Store Set item
                                    //System.out.println("1 " + i + " " + 1 + " " + CurLineArr[i]);
                                    tableSetContent.get(i-1).concatenateMember((LineNo - 10), curLineArr[i]);

                                    tableSetLeafIndex.get(i-1).concatenateMember((LineNo - 10), curLineArr[i]);
                                }

                            }
                        }
                    }

                    LineNo++;
                }
            }

            tableSetLeafIndex = collectSetFeatureFromDoc(tableSetLeafIndex, code);

            printSet(tableSetLeafIndex, "FSM/TXT/Set-"+indexSet+"-Index.txt", code);

            listTableSetContent.add(tableSetContent);
            listTableSetLeafIndex.add(tableSetLeafIndex);

        }

    }

    private Byte getColumnType(String input){
        if ("MT".equals(input)) {
            return 1;
        } else if ("OT".equals(input)) {
            return 2;
        } else if ("MD".equals(input)) {
            return 3;
        } else if ("OD".equals(input)) {
            return 4;
        } else if ("MR".equals(input)) {
            return 5;
        } else if ("OR".equals(input)) {
            return 6;
        } else if ("MC".equals(input)) {
            return  7;
        } else {//"OC"
            return 8;
        }
    }

}
