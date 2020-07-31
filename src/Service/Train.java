package Service;

import Domain.*;

import java.io.*;
import java.lang.annotation.Target;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Train {

    private Schema schema;
    private ArrayList<PairNumText> MTList;
    private ArrayList<PairNumText> setMTList;
    private ArrayList<FSM> FSMList;
    private Integer setCount, currentSet;
    private String Folder;
    private File[] trainFiles;

    private ArrayList<String> masterSimSeq, masterPath, masterTypeSet, masterContent;
    private Map<String, Map<String, ArrayList<ProbabilityDistribution>>> masterTable;


    public void run(String Folder) throws IOException {
        this.Folder = Folder;

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

            this.trainFiles = selectedfiles.toArray(new File[0]);
        }

        double elapsedSeconds;
        long tStart, tEnd, tDelta;
        tStart = System.currentTimeMillis();

        FSMConstruction();

        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        //System.out.println("FSM GENERATED TIME : "+formatter.format(elapsedSeconds)+" seconds");
    }

    private void FSMConstruction() throws IOException {
        ArrayList<LeafNodeSet> tableA = getFromTableA();
        ArrayList<SetLeafNodeSet> tableSetList = new ArrayList<>();

        countSet();
        currentSet = 0;

        this.setMTList = new ArrayList<>();

        if(setCount > 0){
            for(int iSet = 1; iSet <= setCount; iSet++){
                tableSetList.add(getTableSet(String.valueOf(iSet)));
            }

            for(SetLeafNodeSet o : tableSetList){
                this.MTList = getMT(o.getLeafNodeSets());
                if(this.MTList.size() > 0){
                    this.setMTList.addAll(this.MTList);
                }
            }
        }

        this.schema = new Schema();
        this.schema.setTableA(tableA);
        this.schema.setTableSetList(tableSetList);

        this.MTList = getMT(tableA);
        this.masterTable = new HashMap<>();
        this.masterSimSeq = getFromMasterSimSeq();
        this.masterPath = getFromMasterPath();
        this.masterTypeSet = getFromMasterDataType();
        this.masterContent = getFromMasterContent();
        this.FSMList = new ArrayList<>();

        getUniqMT();

        if(MTList.size() > 0){
            if(MTList.get(0).getIndex() != 0){
                //System.out.println("PUT DUMMY START");
                createFSM(-1, MTList.get(0).getIndex());
            }

            for(int i = 1; i < MTList.size(); i++){
                if(MTList.get(i).getIndex() - MTList.get(i-1).getIndex() > 1){
                    //System.out.println("CREATE SEGMENT BETWEEN "+MTList.get(i-1).getIndex()+ " AND "+ MTList.get(i).getIndex());
                    createFSM(MTList.get(i-1).getIndex(), MTList.get(i).getIndex());
                }
            }

            if(MTList.get(MTList.size()-1).getIndex() != tableA.size()-1){
                //System.out.println("PUT DUMMY END");
                createFSM(MTList.get(MTList.size()-1).getIndex(), -1);
            }
        }
        else {
            createFSM(-1, -1);
        }


        /*for(int i = 0; i< FSMList.size(); i++){
            System.out.println("-----------------------------");
            System.out.println(FSMList.get(i).getFSMId());
            System.out.println("    detail :");
            for(int j = 0; j<FSMList.get(i).getDetails().size(); j++){

                String codeNameOfSetFrom = FSMList.get(i).getDetails().get(j).getCodeNameOfSetFrom() == null ? "" : FSMList.get(i).getDetails().get(j).getCodeNameOfSetFrom();
                String codeNameOfSetTo = FSMList.get(i).getDetails().get(j).getCodeNameOfSetTo() == null ? "" : FSMList.get(i).getDetails().get(j).getCodeNameOfSetTo();

                System.out.print("    from "+ FSMList.get(i).getDetails().get(j).getColSymbolFrom()+""+FSMList.get(i).getDetails().get(j).getColIdFrom()+""+codeNameOfSetFrom);
                System.out.print(" to "+ FSMList.get(i).getDetails().get(j).getColSymbolTo()+""+FSMList.get(i).getDetails().get(j).getColIdTo()+""+codeNameOfSetTo);
                System.out.print(" prob: "+FSMList.get(i).getDetails().get(j).getProbability());
                System.out.println();
            }
            System.out.println();
        }*/
    }

    private void createFSM(Integer begin, Integer end){
        Common common = new Common();
        ArrayList<Segment> nodeList;
        Boolean isContainSet = false, isSetStart = false;
        Integer FSMId = FSMList.size()+1, ctc, d, s, e, i = 0, iSetStart = -1;

        FSM fsm; ArrayList<FSMDetail> details = new ArrayList<>();

        nodeList = createStates(begin, end, false,null);

        /*System.out.println("-----------------------------");
        System.out.println("SEGMENT");
        System.out.println("-----------------------------");
        for(int j = 0; j<nodeList.size(); j++){
            String codeNameOfSet = nodeList.get(j).getCodeNameOfSet() == null ? "" : nodeList.get(j).getCodeNameOfSet();
            System.out.print(nodeList.get(j).getColId() + codeNameOfSet
                    + " " + nodeList.get(j).getColClass());
            System.out.println();
        }*/

        for(int currIndex = 0; currIndex < nodeList.size() - 1; currIndex++){

            ctc = 0; d = 0; s = i;

            if(isContainSet == false && nodeList.get(currIndex).getCodeNameOfSet() != null){
                isContainSet = true;
            }

            if(isSetStart == false && nodeList.get(currIndex).getCodeNameOfSet() != null){
                isSetStart = true;
                iSetStart = currIndex;
            }

            if(isSetStart == true && nodeList.get(currIndex).getCodeNameOfSet() == null){
                isSetStart = false;
                iSetStart = -1;
            }

            if(nodeList.get(currIndex).getColClass() == BaseConstants.COLUMN_CLASS_MC ||
               nodeList.get(currIndex).getColClass() == BaseConstants.COLUMN_CLASS_OC){

                details.add(
                        new FSMDetail(
                                currIndex,
                                nodeList.get(currIndex).getColId(),
                                common.getSymbol(nodeList.get(currIndex).getColClass()),
                                nodeList.get(currIndex).getCodeNameOfSet(),
                                nodeList.get(currIndex).getColId(),
                                common.getSymbol(nodeList.get(currIndex).getColClass()),
                                nodeList.get(currIndex).getCodeNameOfSet(),
                                (double)nodeList.get(currIndex).getCounter()
                        )

                );
                ctc += nodeList.get(currIndex).getCounter();
                d++;
                i++;
            }

            for(int nextIndex = currIndex + 1; nextIndex < nodeList.size(); nextIndex++){

                details.add(
                        new FSMDetail(
                                currIndex,
                                nodeList.get(currIndex).getColId(),
                                common.getSymbol(nodeList.get(currIndex).getColClass()),
                                nodeList.get(currIndex).getCodeNameOfSet(),
                                nodeList.get(nextIndex).getColId(),
                                common.getSymbol(nodeList.get(nextIndex).getColClass()),
                                nodeList.get(nextIndex).getCodeNameOfSet(),
                                (double)nodeList.get(nextIndex).getCounter()

                        )
                );
                ctc += nodeList.get(nextIndex).getCounter();
                d++;
                i++;

                if(nodeList.get(nextIndex).getColClass() != BaseConstants.COLUMN_CLASS_OC &&
                   nodeList.get(nextIndex).getColClass() != BaseConstants.COLUMN_CLASS_OD &&
                   nodeList.get(nextIndex).getColClass() != BaseConstants.COLUMN_CLASS_OT)
                {
                    if(isSetStart == false || iSetStart == currIndex){
                        e = i;
                        details = getProbability(details, ctc, d, s, e);
                    }
                    break;
                }

            }

            if(currIndex > iSetStart && isSetStart == true){

                for(int nextSetIndex = iSetStart; nextSetIndex < currIndex; nextSetIndex++){
                    details.add(
                            new FSMDetail(
                                    currIndex,
                                    nodeList.get(currIndex).getColId(),
                                    common.getSymbol(nodeList.get(currIndex).getColClass()),
                                    nodeList.get(currIndex).getCodeNameOfSet(),
                                    nodeList.get(nextSetIndex).getColId(),
                                    common.getSymbol(nodeList.get(nextSetIndex).getColClass()),
                                    nodeList.get(nextSetIndex).getCodeNameOfSet(),
                                    (double)nodeList.get(nextSetIndex).getCounter()

                            )
                    );
                    ctc += nodeList.get(nextSetIndex).getCounter();
                    d++;
                    i++;

                    if(nodeList.get(nextSetIndex).getColClass() != BaseConstants.COLUMN_CLASS_OT &&
                            nodeList.get(nextSetIndex).getColClass() != BaseConstants.COLUMN_CLASS_OD &&
                            nodeList.get(nextSetIndex).getColClass() != BaseConstants.COLUMN_CLASS_OT)
                    {
                        e = i;
                        details = getProbability(details, ctc, d, s, e);
                        break;
                    }
                }
            }
        }

        if(isContainSet) {
            fsm = new FSM(FSMId, details, currentSet);
        }
        else {
            fsm = new FSM(FSMId, details, -1);
        }
        FSMList.add(fsm);

        /*CALCULATE PROBABILITY DISTRIBUTION OF NODE*/
        String key;
        Boolean isSet = false;
        Map<String, ArrayList<ProbabilityDistribution>> docDist;

        for(int segIndex = 1; segIndex < (nodeList.size() - 1); segIndex++){

            Segment o = nodeList.get(segIndex);

            if(o.getCodeNameOfSet() != null) {
                key = common.getSymbol(o.getColClass())+""+o.getColId()+""+o.getCodeNameOfSet();
                isSet = true;
            }
            else {
                key = common.getSymbol(o.getColClass())+""+o.getColId();
            }

            //Check value on master table;
            docDist = masterTable.get(key);

            if(docDist == null){
                //System.out.println(key + " are not found, Generate!!");
                if(isSet == true){
                    String [] arr = o.getCodeNameOfSet().split("-");
                    LeafNodeSet data = this.schema.getTableSetList().get(Integer.parseInt(arr[1])-1).getLeafNodeSets().get(Integer.parseInt(arr[2]));
                    ArrayList<String> codes = this.schema.getTableSetList().get(Integer.parseInt(arr[1])-1).getCodes();
                    docDist = generatedDocumentDistributionProbabilityTable(data.getLNMember(), codes);
                }
                else {
                    LeafNodeSet data = this.schema.getTableA().get(o.getColId());
                    docDist = generatedDocumentDistributionProbabilityTable(data.getLNMember(), null);
                }
                masterTable.put(key, docDist);
            }

            isSet = false;
        }
    }

    private ArrayList<LeafNodeSet> getFromTableA() throws IOException {
        ArrayList<LeafNodeSet> result = new ArrayList<>();
        ArrayList<String[]> contentWebPage = new ArrayList<>();
        ArrayList<String[]> content = new ArrayList<>();

        BufferedReader buf = new BufferedReader(new FileReader(Folder+"FSM/TXT/TableA-Index.txt"));

        String line = "";
        int count = 0;
        while ((line=buf.readLine())!=null){
            if(count >= 0 && count<= 6){
                content.add(line.split("\t"));
            }
            if(count > 6){
                if(line.contains("-oO End of Aligned Table Oo-")) break;
                else contentWebPage.add((line.split("\t")));
            }
            count++;
        }

        for (int i = 0; i < content.size(); i++){
            String [] a = content.get(i);
            //Path Id
            if(i == 0){
                for(int j=1; j<a.length; j++){
                    result.add(new LeafNodeSet(a[j].split(","), null, null, null, null, null, null, null, null));
                }
            }

            //SimSeq Id
            if(i == 1){
                for(int j=1; j<a.length; j++){
                    result.get(j-1).setSimSeqId(a[j].split(","));
                }
            }

            //PTypeSet Id
            if(i == 2){
                for(int j=1; j<a.length; j++){
                    result.get(j-1).setPTypeSetId(a[j].split(","));
                }
            }

            //TypeSet Id
            if(i == 3){
                for(int j=1; j<a.length; j++){
                    result.get(j-1).setTypeSetId(a[j].split(","));
                }
            }

            //Content Id
            if(i == 4){
                for(int j=1; j<a.length; j++){
                    result.get(j-1).setContentId(a[j].split(","));
                }
            }

            //Encoding
            if(i == 5){
                for(int j=1; j<a.length; j++){
                    result.get(j-1).setEncoding(Byte.parseByte(a[j]));
                }
            }
            //LN Type
            if(i == 6){
                for(int j=1; j<a.length; j++){
                    if(a[j].equals("MT")) {
                        result.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_MT);
                    }
                    else if(a[j].equals("OT")) {
                        result.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_OT);
                    }
                    else if(a[j].equals("MD")) {
                        result.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_MD);
                    }
                    else if(a[j].equals("OD")) {
                        result.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_OD);
                    }
                    else if(a[j].equals("MR")) {
                        result.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_MR);
                    }
                    else if(a[j].equals("OR")) {
                        result.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_OR);
                    }
                    else if(a[j].equals("MC")) {
                        result.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_MC);
                    }
                    else if(a[j].equals("OC")) {
                        result.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_OC);
                    }

                    String[] lnMembers = contentCopy(contentWebPage,j);
                    result.get(j-1).setLNMember(lnMembers);
                }
            }
        }
        return result;
    }

    private ArrayList<String> getFromMasterPath() {

        ArrayList<String> result = new ArrayList<>();

        try{
            BufferedReader buf = new BufferedReader(new FileReader(Folder+"Output/TXT/MasterPath.txt"));

            String line = "";
            int count = 0;
            while ((line=buf.readLine())!=null){
                if(count > 0){
                    Pattern pattern = Pattern.compile("\\d+\\b");
                    Matcher matcher = pattern.matcher(line);

                    String firstWord = null;
                    if (matcher.find()) {
                        firstWord = matcher.group();
                        result.add(firstWord);
                    }
                }
                count++;
            }
        }
        catch (IOException ex){
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    private ArrayList<String> getFromMasterSimSeq() {

        ArrayList<String> result = new ArrayList<>();
        try{
            BufferedReader buf = new BufferedReader(new FileReader(Folder+"Output/TXT/MasterSimTEC.txt"));

            String line = "";
            int count = 0;
            while ((line=buf.readLine())!=null){
                if(count > 0){
                    Pattern pattern = Pattern.compile("\\d+\\b");
                    Matcher matcher = pattern.matcher(line);

                    String firstWord = null;
                    if (matcher.find()) {
                        firstWord = matcher.group();
                        result.add(firstWord);
                    }
                }
                count++;
            }
        }
        catch (IOException ex){
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    private ArrayList<String> getFromMasterDataType() {

        ArrayList<String> result = new ArrayList<>();

        try {
            BufferedReader buf = new BufferedReader(new FileReader(Folder+"Output/TXT/MasterDataType.txt"));

            String line = "";
            int count = 0;
            while ((line=buf.readLine())!=null){
                if(count > 0){
                    Pattern pattern = Pattern.compile("\\d+\\b");
                    Matcher matcher = pattern.matcher(line);

                    String firstWord = null;
                    if (matcher.find()) {
                        firstWord = matcher.group();
                        result.add(firstWord);
                    }
                }
                count++;
            }
        }
        catch (IOException ex){
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    private ArrayList<String> getFromMasterContent() {

        ArrayList<String> result = new ArrayList<>();

        try{
            BufferedReader buf = new BufferedReader(new FileReader(Folder+"Output/TXT/MasterContent.txt"));

            String line = "";
            int count = 0;
            while ((line=buf.readLine())!=null){
                if(count > 0){
                    Pattern pattern = Pattern.compile("\\d+\\b");
                    Matcher matcher = pattern.matcher(line);

                    String firstWord = null;
                    if (matcher.find()) {
                        firstWord = matcher.group();
                        result.add(firstWord);
                    }
                }
                count++;
            }
        }
        catch (IOException ex){
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    private Map<String, ArrayList<ProbabilityDistribution>> generatedDocumentDistributionProbabilityTable(String[] lnMember, ArrayList<String> codes) {
        Map<String, ArrayList<ProbabilityDistribution>> docDist = new HashMap<>();
        docDist.put("Path", getDistribution(masterPath, lnMember, 'H', codes));
        docDist.put("SimSeq", getDistribution(masterSimSeq, lnMember, 'S', codes));
        docDist.put("PTypeSet", getDistribution(masterTypeSet, lnMember, 'P', codes));
        docDist.put("TypeSet", getDistribution(masterTypeSet, lnMember, 'T', codes));
        docDist.put("Content", getDistribution(masterContent, lnMember, 'C', codes));
        return docDist;
    }

    private ArrayList<ProbabilityDistribution> getDistribution( ArrayList<String> master,
                                                                String[] lnMember,
                                                                Character key,
                                                                ArrayList<String> codes)  {

        ArrayList<ProbabilityDistribution> result = new ArrayList<>();

        double sum = lnMember.length, alpha;
        int lnIndex;
        ArrayList<String[]> lines;

        for(String o : master){
            result.add(new ProbabilityDistribution(o, 0));
        }

        for(int i = 0; i < lnMember.length; i++){

            if(codes != null){
                String arr[] = codes.get(i).split("-");
                lnIndex = Integer.parseInt(arr[1]);
            }
            else {
                lnIndex = i;
            }

            Arrays.sort(trainFiles);
            lines = getDataFromTableL(trainFiles, lnMember[i], lnIndex);
            for(String[] lineArr : lines){
                if(key.equals('H')){
                    // path
                    int index = Collections.binarySearch(master, lineArr[9]);
                    if(index > 0) {
                        result.get(index).setProbability((result.get(index).getProbability() + 1));
                    }
                }
                else if(key.equals('S')){
                    // simTEC
                    int index = Collections.binarySearch(master, lineArr[10]);
                    if(index > 0) {
                        result.get(index).setProbability((result.get(index).getProbability() + 1));
                    }
                }

                else if(key.equals('P')){
                    // parent
                    int index = Collections.binarySearch(master, lineArr[6]);
                    if(index > 0) {
                        result.get(index).setProbability((result.get(index).getProbability() + 1));
                    }
                }

                else if(key.equals('T')){
                    // typeset
                    int index = Collections.binarySearch(master, lineArr[7]);
                    if(index > 0) {
                        result.get(index).setProbability((result.get(index).getProbability() + 1));
                    }
                }

                else if(key.equals('C')){
                    // content
                    int index = Collections.binarySearch(master, lineArr[8].substring(lineArr[8].indexOf("-") + 1));
                    if(index > 0) {
                        result.get(index).setProbability((result.get(index).getProbability() + 1));
                    }
                }
            }
        }

        if(result.size() <= 100) {
            alpha = 0.0001;
        }
        else {
            alpha = 0.000001;
        }

        for(ProbabilityDistribution o : result){
            o.setProbability((o.getProbability() + alpha)/ (sum + (alpha * result.size())));
        }
        return result;
    }

    private ArrayList<String[]> getDataFromTableL(File[] files, String lnMember, int iDoc){

        ArrayList<String[]> result = new ArrayList<>();
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
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private void countSet(){
        setCount = 0;
        File file = new File(Folder + "FSM/TXT"); //folder of train document
        File[] outputFiles = file.listFiles();
        if (!file.exists()) {
            System.out.println("Folder not found!");
        } else if (outputFiles.length > 0) {
            for (int iOutputDoc = 0; iOutputDoc < outputFiles.length; iOutputDoc++) {
                if(outputFiles[iOutputDoc].getName().contains("Set") && outputFiles[iOutputDoc].getName().contains("Index")){
                    setCount++;
                }
            }
        }
    }

    private SetLeafNodeSet getTableSet(String codeNameOfSet){
        SetLeafNodeSet result = new SetLeafNodeSet();
        ArrayList<LeafNodeSet> leafNodeSets = new ArrayList<>();
        ArrayList<String> codes = new ArrayList<>();
        ArrayList<String[]> content = new ArrayList<>();
        ArrayList<String[]> contentWebPage = new ArrayList<>();
        try {
            //Change Here
            BufferedReader buf = new BufferedReader(new FileReader(Folder+"FSM/TXT/Set-"+codeNameOfSet+"-Index.txt"));
            String line = "";
            int count = 1;
            while ((line=buf.readLine())!=null){
                if(count<= 7){
                    content.add(line.split("\t"));
                }
                if(count > 7){
                    contentWebPage.add((line.split("\t")));
                }
                count++;
            }

            for (int i = 0; i < content.size(); i++){
                String [] a = content.get(i);
                //Path Id
                if(i == 0){
                    for(int j=1; j<a.length; j++){
                        leafNodeSets.add(new LeafNodeSet(a[j].split(","), null, null, null, null, null, null, null, null));
                    }
                }

                //SimSeqId
                if(i == 1){
                    for(int j=1; j<a.length; j++){
                        leafNodeSets.get(j-1).setSimSeqId((a[j]).split(","));
                    }
                }

                //PTypeSet
                if(i == 2){
                    for(int j=1; j<a.length; j++){
                        leafNodeSets.get(j-1).setPTypeSetId((a[j]).split(","));
                    }
                }

                //TypeSet
                if(i == 3){
                    for(int j=1; j<a.length; j++){
                        leafNodeSets.get(j-1).setTypeSetId((a[j]).split(","));
                    }
                }

                //ContentId
                if(i == 4){
                    for(int j=1; j<a.length; j++){
                        leafNodeSets.get(j-1).setContentId((a[j]).split(","));
                    }
                }

                //Encoding
                if(i == 5){
                    for(int j=1; j<a.length; j++){
                        leafNodeSets.get(j-1).setEncoding(Byte.parseByte(a[j]));
                    }
                }
                //LN Type
                if(i == 6){
                    codes = new ArrayList<>(Arrays.asList(contentCopy(contentWebPage, 0)));
                    for(int j=1; j<a.length; j++){
                        if(a[j].equals("MT")) {
                            leafNodeSets.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_MT);
                        }
                        else if(a[j].equals("OT")) {
                            leafNodeSets.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_OT);
                        }
                        else if(a[j].equals("MD")) {
                            leafNodeSets.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_MD);
                        }
                        else if(a[j].equals("OD")) {
                            leafNodeSets.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_OD);
                        }
                        else if(a[j].equals("MR")) {
                            leafNodeSets.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_MR);
                        }
                        else if(a[j].equals("OR")) {
                            leafNodeSets.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_OR);
                        }
                        else if(a[j].equals("MC")) {
                            leafNodeSets.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_MC);
                        }
                        else if(a[j].equals("OC")) {
                            leafNodeSets.get(j-1).setLNType(BaseConstants.COLUMN_CLASS_OC);
                        }

                        String[] lnMembers = contentCopy(contentWebPage,j);
                        leafNodeSets.get(j-1).setLNMember(lnMembers);
                    }
                }

            }
        }
        catch (IOException ex) {
            Logger.getLogger(Train.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        result.setLeafNodeSets(leafNodeSets);
        result.setCodes(codes);

        return result;
    }

    private ArrayList<PairNumText> getMT(ArrayList<LeafNodeSet> tableA){
        ArrayList<PairNumText> result = new ArrayList<>();
        int index = 0;
        String code;
        for(LeafNodeSet o: tableA){
            code = "";
            if(o.getLNType()== BaseConstants.COLUMN_CLASS_MT){
                for(int i = 0; i < o.getPathId().length; i++){
                    for(int j = 0; j < o.getContentId().length; j++){

                        if(i+1 < o.getPathId().length || j+1 < o.getContentId().length) {
                            code = code + (o.getPathId()[i]+"-"+o.getContentId()[j])+",";
                        }
                        else {
                            code = code + (o.getPathId()[i]+"-"+o.getContentId()[j]);
                        }
                    }
                }
                result.add(new PairNumText(code, index));
            }
            index++;
        }
        return result;
    }

    private String[] contentCopy(ArrayList<String[]> contentWebPage, int index){
        String[] result = new String[contentWebPage.size()];
        int count = 0;
        for (String[] a: contentWebPage){
            // If Its Content
            //String[] leafIndex = a[index].split("( :: )");
            //result[count] = leafIndex.length == 2 ? leafIndex[1] : leafIndex[0];
            result[count] = a[index];
            count++;
        }
        return result;
    }

    private void getUniqMT(){

        Boolean check = true;
        ArrayList<PairNumText> UniqMT;
        PairNumText previous, current, next;

        do {
            UniqMT = new ArrayList<>();
            for(int i = 0; i < MTList.size(); i++){
                current = MTList.get(i);
                if(i == 0){
                    previous = null;
                    next = MTList.get(i+1);

                    if(!current.getCode().equals(next.getCode())){
                        if(checkMTonSet(current)){
                            UniqMT.add(current);
                        }
                    }
                }
                else if( i == MTList.size()-1){
                    previous = MTList.get(i-1);
                    next = null;

                    if(!current.getCode().equals(previous.getCode())){
                        if(checkMTonSet(current)){
                            UniqMT.add(current);
                        }
                    }
                }
                else {
                    previous = MTList.get(i-1);
                    next = MTList.get(i+1);

                    if((!current.getCode().equals(previous.getCode())) && (!current.getCode().equals(next.getCode()))){
                        if(checkMTonSet(current)){
                            UniqMT.add(current);
                        }
                    }
                }
            }

            if(UniqMT.size() == MTList.size()){
                check = false;
            }
            else {
                MTList = UniqMT;
            }

        } while (check);
    }

    private Boolean checkMTonSet(PairNumText data){

        if(this.setMTList.size() == 0){
            return true;
        }

        for(PairNumText o : this.setMTList){
            if(o.getCode().equals(data.getCode())){
                //return false If it is duplicate
                return false;
            }
        }
        //return true If it is not duplicate
        return true;
    }

    private ArrayList<Segment> createStates(Integer start, Integer end, Boolean isSet, Integer iSet){

        ArrayList<Segment> nodeList = new ArrayList<>();
        LeafNodeSet currNode;
        String state = "";

        if(isSet){
            ArrayList<LeafNodeSet> tableSet = this.schema.getTableSetList().get(currentSet).getLeafNodeSets();
            for(int i = 0; i < tableSet.size(); i++){
                currNode = tableSet.get(i);
                nodeList.add(new Segment(iSet, currNode.getLNType(), "-"+(currentSet+1)+"-"+i, currNode.getCounter()));
            }
        }
        else {
            if(start == -1){
                nodeList.add(new Segment(-1, BaseConstants.COLUMN_CLASS_MT, 0));
                start = 0;
            }
            
            if(end == -1){
                state = "IKOUZE";
                end = this.schema.getTableA().size()-1;
            }

            for(int i = start; i <= end; i++){
                currNode = this.schema.getTableA().get(i);
                if(!currNode.getLNType().equals(BaseConstants.COLUMN_CLASS_MR) && !currNode.getLNType().equals(BaseConstants.COLUMN_CLASS_OR)){
                    nodeList.add(new Segment(i, currNode.getLNType(), currNode.getCounter()));
                }
                else {
                    nodeList.addAll(createStates(null, null, true, i));
                    currentSet++;
                }
            }
            
            if(state.equals("IKOUZE")){
                nodeList.add(new Segment(end+1, BaseConstants.COLUMN_CLASS_MT, 0));
            }

        }
        return nodeList;
    }

    private ArrayList<FSMDetail> getProbability(ArrayList<FSMDetail> list, double ctc, int d, int start, int end){

        ArrayList<FSMDetail> result = list;

        double alpha = ctc <= 100 ? 0.1 : 1;

        for(int i = start; i < end; i++ ){
            double temp = result.get(i).getProbability();
            double prob =  (ctc == 0.0 || temp == 0.0) ? 0.0 : ((temp + alpha) / (ctc + (alpha* d)));
            result.get(i).setProbability(prob);
        }
        return list;
    }

    public Schema getSchema() { return schema; }

    public ArrayList<PairNumText> getMTList() {
        return MTList;
    }

    public ArrayList<FSM> getFSMList() {
        return FSMList;
    }

    public Map<String, Map<String, ArrayList<ProbabilityDistribution>>> getMasterTable() {
        return masterTable;
    }
}
