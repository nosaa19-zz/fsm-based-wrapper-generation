package Service;

import Domain.*;
import com.google.gson.Gson;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Test {

    private ArrayList<Element> aTestDoc;
    private ArrayList<PairNumText> UniqMT, S, P; // S is Schema (TTrain); P is Page(TTest)
    private ArrayList<FSM> FSMList;
    private ArrayList<String> fileNameList;
    private ArrayList<Double> logTime = new ArrayList<>();
    private ArrayList<File> setFiles;
    private Schema train, test;
    private Map<String, Map<String, ArrayList<ProbabilityDistribution>>> masterTable;
    private String Folder, finalResult;
    private Integer sequenceOfFilePassed, passCount = 0, failCount = 0, fsmc, Lp;
    private File tableTestFile; //logFile;
    private ArrayList<LogFSM> logFSMS;


    public void run(String Folder,
                    Schema schema, Map<String,
                    Map<String, ArrayList<ProbabilityDistribution>>> masterTable,
                    ArrayList<PairNumText> UniqMT,
                    ArrayList<FSM> FSMList){


        /*logFile = new File(Folder+"FSM/log.txt");
        try {
            Files.deleteIfExists(logFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        this.Folder = Folder;
        this.train = schema;
        this.masterTable = masterTable;
        this.UniqMT = UniqMT;
        this.FSMList = FSMList;
        this.logFSMS = new ArrayList<>();
        this.finalResult = "Success";

        Common common = new Common();
        this.test = new Schema();
        fileNameList = new ArrayList<>();

        tableTestFile = new File(Folder+"FSM/TXT/TableAFSM.txt");
        try {
            Files.deleteIfExists(tableTestFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        setFiles = new ArrayList<>();
        for(int i = 0; i< train.getTableSetList().size(); i++){
            File tableSetTestFile = new File(Folder+"FSM/TXT/Set-"+(i+1)+"-FSM.txt");
            try {
                Files.deleteIfExists(tableSetTestFile.toPath());
                setFiles.add(tableSetTestFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sequenceOfFilePassed = 0;
        readFolderTrain();

        File inputFile = new File(Folder + "Test"); //folder of test document
        File[] files = inputFile.listFiles();
        Arrays.sort(files);
        if (!inputFile.exists()) {
            System.out.println("Folder not found!");
        } else if (files.length > 0) {
            copySchemaFromTableA(files.length);
            copySchemaFromListSets();
            common.printSchemaOnTableTest(tableTestFile, test.getTableA());

            for( int i = 0; i< test.getTableSetList().size(); i++){
                common.printSchemaOnTableSetTest(setFiles.get(i), test.getTableSetList().get(i).getLeafNodeSets());
            }

            for (int iDoc = 0; iDoc < files.length; iDoc++) {
                universalWrapper(files[iDoc]);
            }
        }

        Gson gson = new Gson();
        SummaryLogFSM summary = new SummaryLogFSM();
        summary.setStatus(finalResult);
        summary.setDetails(logFSMS);
        String printLog = gson.toJson(summary);
        System.out.println(printLog);

        /*double averageTime = 0;

        for(double time : logTime){
            averageTime += time;
        }

        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        System.out.println("===================================================================================");
        System.out.println("VERIFICATION PROCESS DONE!!");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("TOTAL TIME : "+formatter.format(averageTime)+" seconds for "+ logTime.size()+ " documents");
        System.out.println("PASSED : "+passCount+ " documents");
        System.out.println("FAILED : "+failCount+ " documents");
        System.out.println("AVERAGE TIME : "+formatter.format(averageTime / logTime.size())+" seconds");
        System.out.println("===================================================================================");*/

    }

    void universalWrapper(File file){

        NWSim nwSim = new NWSim();
        Common common = new Common();

        String result = "PASSED";
        String fileName = file.getName();

        Lp = null;
        fsmc = 0;

        double elapsedSeconds;
        long tStart, tEnd, tDelta;

        tStart = System.currentTimeMillis();

        //System.out.println("------------------------------------------------------------");
        //System.out.println("NOW VERIFICATION :  "+ fileName);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF8"));) {

            S =  new ArrayList<>(UniqMT);

            aTestDoc = new ArrayList<>();
            P = new ArrayList<>();
            Read1TestingDoc(br);

            nwSim.seqAlign(S, P);
            S = nwSim.GetSOut();
            P = nwSim.GetTOut();

            //ShowInput();
            ArrayList<SegmentVerification> verificationList = null;

            while (true){
                verificationList = findVerificationList(S, P);

                if(verificationList == null) {
                    result = "REJECT - TEMPLATE ALIGNMENT FAILURE";

                    if(finalResult.equals("Success")){
                        this.finalResult = "Failure";
                    }

                    common.printOnFile(tableTestFile, common.printVerificationFailure(file.getName(), test.getTableA().size()));
                    failCount++;
                    break;
                }
                else if (Lp != null && Lp == -99){
                    break;
                }
                //ShowSegmentVerification(verificationList);

                FSM fsm = findMatchFSM(verificationList);

                if(fsm == null){
                    result = "REJECT - FSM NOT FOUND";

                    if(finalResult.equals("Success")){
                        this.finalResult = "Failure";
                    }

                    common.printOnFile(tableTestFile, common.printVerificationFailure(file.getName(), test.getTableA().size()));
                    failCount++;
                    break;
                }

                ArrayList<FSMVerification> nodeVerificationList = fsmDriver(verificationList, fsm);

                if(nodeVerificationList == null) {
                    result = "REJECT - VERIFICATION FAILURE";

                    if(finalResult.equals("Success")){
                        this.finalResult = "Failure";
                    }

                    common.printOnFile(tableTestFile, common.printVerificationFailure(file.getName(), test.getTableA().size()));
                    failCount++;
                    break;
                }
                else {
                    //Update Null value in the S for further pointer to mapping the result
                    updateS(nodeVerificationList);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }

        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        logTime.add(elapsedSeconds);

        //common.printOnFile(logFile, file.getName()+" "+result);
        //System.out.println(file.getName()+" "+result);
        logFSMS.add(new LogFSM(file.getName(), result));
        if(result.contains("PASSED")){
            //ShowInput();
            common.printOnFile(tableTestFile, printVerificationResult(fileName, test.getTableA().size(), sequenceOfFilePassed, setFiles));
            sequenceOfFilePassed++;
            passCount++;
        }
    }

    private void ShowSegmentVerification(ArrayList<SegmentVerification> verification){
        for (int j = 0; j < verification.size(); j++) {
            System.out.print(verification.get(j).getColIdTrain()
                    + " " + verification.get(j).getColIdTest());
            System.out.println();
        }
        System.out.println();

    }

    public ArrayList<FSMVerification> fsmDriver (ArrayList<SegmentVerification> verificationList,
                                            FSM fsm) throws IOException {

        ArrayList<FSMVerification> result = null;

        result = cloneATestDocToFSMVerification(verificationList);
        result = findCandidateSet(result, fsm);

        /*for(FSMVerification o : result){
                System.out.print(o.getContent()+ "|"+ o.getPathId()+ "|"+ o.getSimSeqId() + "|"+ o.getPTypeSetId()+ "|"+ o.getTypeSetId()+ "|"+ o.getContentId()+"|Candidate Set: [ ");
                for(State d : o.getCandidateList()){
                    String codeNameOfSet = d.getCodeNameOfSet() == null ? "" : d.getCodeNameOfSet();
                    System.out.print(d.getColSymbolId()+""+d.getColId()+""+codeNameOfSet);
                    System.out.print(" ");
                }
                System.out.println("]");
        }

        System.out.println("-----------------------------------------------------------------------------");*/

        for(int j = 0; j<result.size(); j++){
            if(result.get(j).getCandidateList().size() == 0){
                //System.out.println("NO CANDIDATE AVAILABE!");
                return null;
            }
            else {
                if(result.get(j).getCandidateList().size() == 1) {
                    State finalState = result.get(j).getCandidateList().get(0);

                    //SPECIAL CONSTRAINT
                    if(j-2 >= 0)
                    {
                        if((result.get(j-1).getFinalState().getColId() >= finalState.getColId()) && (result.get(j-1).getCandidateList().size() > 1) &&
                                ((finalState.getColSymbolId() == 'B' || finalState.getColSymbolId() == 't') && finalState.getCodeNameOfSet() == null) )
                        {
                            //System.out.println("HERE SPECIAL CONSTRAINT I HAPPENED");
                            State prevFinalState = result.get(j-1).getFinalState();
                            result.get(j-1).getCandidateList().remove(prevFinalState);
                            j = j-2;
                            continue;
                        }
                    }

                    result.get(j).setFinalState(finalState);
                }
                else if(result.get(j).getCandidateList().size() > 1){

                    //FSM Verification START
                    State finalState = null;

                    //Find Best Candidate with the Highest Probability
                    if(j-1 >= 0){
                        finalState = findFinalState(result.get(j-1).getFinalState(), result.get(j), fsm);
                    }
                    else {
                        finalState = findFinalState(null, result.get(j), fsm);
                    }


                    if(finalState == null) return null;
                    //SPECIAL CONSTRAINT
                    if(j-2 >= 0)
                    {
                        /*if(finalState.getCodeNameOfSet() != null &&
                           result.get(j-2).getState().getCodeNameOfSet() != null &&
                           result.get(j-1).getCandidateSet().size() > 1){

                            if(finalState.getColId().equals(result.get(j-2).getState().getColId())
                                    && checkingSpecialConstraintII(result.get(j-1).getCandidateSet()))
                            {
                                System.out.println("HERE SPECIAL CONSTRAINT II HAPPENED");
                                List<FSMVerificationDetail> copyPrevFinal = new ArrayList(result.get(j-1).getCandidateSet());
                                for(FSMVerificationDetail prevFinalState : copyPrevFinal){
                                    if(prevFinalState.getCodeNameOfSet() == null) {
                                        result.get(j-1).getCandidateSet().remove(prevFinalState);
                                    }
                                }
                                j = j-2;
                                continue;
                            }
                        }*/

                        if((result.get(j-1).getFinalState().getColId() > finalState.getColId()) && (result.get(j-1).getCandidateList().size() > 1))
                        {
                            //System.out.println("HERE SPECIAL CONSTRAINT III HAPPENED");
                            State prevFinalState = result.get(j-1).getFinalState();
                            result.get(j-1).getCandidateList().remove(prevFinalState);
                            j = j-2;
                            continue;
                        }
                    }

                    result.get(j).setFinalState(finalState);
                }
            }
        }


        /*System.out.println("============FINAL RESULT================");

        for(int j = 0; j<result.size(); j++){
            System.out.print(result.get(j).getLeafIndex()+" | "+result.get(j).getContent() + "   : [ ");
            if(result.get(j).getState().getCodeNameOfSet() != null) {
                System.out.print(result.get(j).getState().getColSymbolId()+""+result.get(j).getState().getColId()+""+result.get(j).getState().getCodeNameOfSet()+" ");
            }
            else {
                System.out.print(result.get(j).getState().getColSymbolId()+""+result.get(j).getState().getColId()+" ");
            }
            System.out.println("]");
        }
        System.out.println("========================================");
        System.out.println();*/

        return result;
    }
    private void readFolderTrain(){
        File inputFile = new File(Folder + "Output/TXT");
        File[] files = inputFile.listFiles();
        if (!inputFile.exists()) {
            System.out.println("Folder not found!");
        } else if (files.length > 0) {
            for (int iDoc = 0; iDoc < files.length; iDoc++) {
                if(files[iDoc].getName().contains("page-")){
                    fileNameList.add(files[iDoc].getName());
                }
            }
        }
    }

    private void copySchemaFromTableA(Integer numberOfTestDoc){
        ArrayList<LeafNodeSet> tableTest = new ArrayList<>();

        for(LeafNodeSet o : train.getTableA()){
            tableTest.add(new LeafNodeSet(o.getPathId(), o.getSimSeqId(), o.getPTypeSetId(), o.getTypeSetId(), o.getContentId(), new String[numberOfTestDoc], o.getLNType(), o.getEncoding(), null));
        }

        this.test.setTableA(tableTest);
    }

    private void copySchemaFromListSets(){
        ArrayList<SetLeafNodeSet> listSetsTest = new ArrayList<>();

        for(SetLeafNodeSet i : train.getTableSetList()){

            ArrayList<LeafNodeSet> leafNodeSets = new ArrayList<>();
            ArrayList<String> codes = new ArrayList<>(i.getCodes());

            for(LeafNodeSet o : i.getLeafNodeSets()){
                leafNodeSets.add(new LeafNodeSet(o.getPathId(), o.getSimSeqId(), o.getPTypeSetId(), o.getTypeSetId(), o.getContentId(), null, o.getLNType(), o.getEncoding(), null));
            }

            SetLeafNodeSet sets = new SetLeafNodeSet();
            sets.setLeafNodeSets(leafNodeSets);
            sets.setCodes(codes);

            listSetsTest.add(sets);
        }

        this.test.setTableSetList(listSetsTest);
    }

    private void Read1TestingDoc(BufferedReader br) {
        String CurrentLine;
        String[] CurLineArr;
        ArrayList<String> Path;
        String[] VPath;

        try {
            br.readLine(); //to read the field names in the fist line of file
            CurrentLine = br.readLine();

            while (CurrentLine != (null) && !CurrentLine.isEmpty()) {

                CurLineArr = CurrentLine.split("\t");

                VPath = CurLineArr[2].split(" ");

                for (int i = 0; i < CurLineArr.length; i++) {
                    CurLineArr[i] = CurLineArr[i].replace("[", "");
                    CurLineArr[i] = CurLineArr[i].replace(",", "");
                    CurLineArr[i] = CurLineArr[i].replace("]", "");
                }

                Path = new ArrayList<>();
                for (int iP = 0; iP < VPath.length; iP++) {
                    Path.add(VPath[iP]);
                }

                aTestDoc.add(new Element(CurLineArr[0], CurLineArr[9], CurLineArr[10], CurLineArr[6], CurLineArr[7], CurLineArr[8].substring(CurLineArr[8].indexOf("-") + 1), CurLineArr[1]));
                this.P.add(new PairNumText(CurLineArr[8], Integer.parseInt(CurLineArr[0])));

                CurrentLine = br.readLine();
            }

        } catch (IOException ex) {
            Logger.getLogger(Test.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ShowInput() {
        System.out.println("Training: ("+S.size()+")");
        for (int i = 0; i < S.size(); i++) {
            if (S.get(i) != null) {
                System.out.println(S.get(i).getIndex() + "\t" + S.get(i).getCode());
            } else {
                System.out.println("null");
            }
        }

        System.out.println("Testing : ("+P.size()+")");
        for (int i = 0; i < P.size(); i++) {
            if (P.get(i) != null) {
                System.out.println(P.get(i).getIndex() + "\t" + P.get(i).getCode());
            } else {
                System.out.println("null");
            }
        }
    }

    private ArrayList<SegmentVerification> findVerificationList(ArrayList<PairNumText> TTrain, ArrayList<PairNumText> TTest){

        int START_OF_COLLECTION = Lp == null? 0 : Lp;
        int END_OF_COLLECTION = TTrain.size()-1;
        Byte status = BaseConstants.NOT_SEGMENT;

        ArrayList<SegmentVerification> result = new ArrayList<>();

        for(int i = START_OF_COLLECTION; i <= END_OF_COLLECTION; i++){

            if(TTest.get(i) == null) {
                return null;
            }
            if(TTrain.get(i) != null){

                if((i+1) <= END_OF_COLLECTION)
                {
                    if(TTrain.get(i+1) == null)
                    {
                        if(status == BaseConstants.END_SEGMENT)
                        {
                            result.add(new SegmentVerification(TTrain.get(i).getIndex(), TTest.get(i).getIndex()));

                            return result;

                        }

                        if(status == BaseConstants.NOT_SEGMENT)
                        {
                            status = BaseConstants.START_SEGMENT;
                            result = new ArrayList<>();

                            result.add(new SegmentVerification(TTrain.get(i).getIndex(), TTest.get(i).getIndex()));
                        }

                    }
                    else if(TTrain.get(i+1) != null)
                    {
                        if(status == BaseConstants.END_SEGMENT){
                            result.add(new SegmentVerification(TTrain.get(i).getIndex(), TTest.get(i).getIndex()));

                            return result;
                        }
                    }
                }
                else if((i+1) > END_OF_COLLECTION && status == BaseConstants.END_SEGMENT){
                    result.add(new SegmentVerification(TTrain.get(i).getIndex(), TTest.get(i).getIndex()));

                    return result;
                }
            }
            else if (TTrain.get(i) == null)
            {
                if((i+1) <= END_OF_COLLECTION)
                {
                    if(TTrain.get(i+1) != null || (i+1) == END_OF_COLLECTION){
                        status = BaseConstants.END_SEGMENT;
                    }

                    result.add(new SegmentVerification(null, TTest.get(i).getIndex()));
                }

                //For Case Only One Segment -- If There is an error consider this code
                else if((i+1) > END_OF_COLLECTION){
                    result.add(new SegmentVerification(null, TTest.get(i).getIndex()));
                    return result;
                }
            }

            if(status == BaseConstants.NOT_SEGMENT  && i == END_OF_COLLECTION){
                Lp = -99;
                return result;
            }
        }

        return result;
    }

    private String printVerificationResult(String filename, int tableASize, int seqFile, List<File> setFiles){
        Common common = new Common();
        String result = filename+"\t",
                currentText = "",
                content = "";
        int setCount = 0;

        ArrayList<String> leafnodes = new ArrayList<>();
        ArrayList<ArrayList<PairNumText>> setDatas = new ArrayList<>();

        if(setFiles.size() > 0){

            //INITIALIZE HASHMAP FOR SET
            LinkedHashMap<Integer, String> setData;
            for(int i = 0; i < setFiles.size(); i++){
                setDatas.add(new ArrayList<>());

            }
        }

        for(int i = 0; i < tableASize; i++){
            leafnodes.add(" ");
        }

        for(int indexOfTTrain = 0;  indexOfTTrain < S.size(); indexOfTTrain++){

            currentText = leafnodes.get(S.get(indexOfTTrain).getIndex());

            if(currentText.equals(" ")){
                if(S.get(indexOfTTrain).getCode() != null && S.get(indexOfTTrain).getCode().matches("S[0-9+#-]+"))
                {
                    setCount = Integer.parseInt(S.get(indexOfTTrain).getCode().split("-")[1]);
                    leafnodes.set(S.get(indexOfTTrain).getIndex(), setCount+"-"+seqFile);

                    //Put Set Data Here
                    setDatas.get(setCount-1).add(new PairNumText(S.get(indexOfTTrain).getCode(), P.get(indexOfTTrain).getIndex()));
                }
                else {
                    leafnodes.set(S.get(indexOfTTrain).getIndex(), getContentFromTestDoc(P.get(indexOfTTrain).getIndex()));
                }
            }
            else {

                if(S.get(indexOfTTrain).getCode() != null && S.get(indexOfTTrain).getCode().matches("S[0-9+#-]+"))
                {
                    setCount = Integer.parseInt(S.get(indexOfTTrain).getCode().split("-")[1]);
                    setDatas.get(setCount-1).add(new PairNumText(S.get(indexOfTTrain).getCode(), P.get(indexOfTTrain).getIndex()));
                }
                else {
                    currentText = currentText+" "+ getContentFromTestDoc(P.get(indexOfTTrain).getIndex());
                    leafnodes.set(S.get(indexOfTTrain).getIndex(), currentText);
                }
            }
        }

        for(int i = 0; i < leafnodes.size(); i++){
            if(i == leafnodes.size()-1){
                result = result+ leafnodes.get(i);
            }
            else {
                result = result+ leafnodes.get(i)+"\t";
            }
        }

        for(int setIndex = 0; setIndex < setDatas.size(); setIndex++){

            if(!setDatas.get(setIndex).isEmpty()){
                printSetOnFile(test.getTableSetList(), setFiles.get(setIndex), setDatas.get(setIndex), setIndex+1, seqFile);
            }
        }

        return result;
    }

    public void printSetOnFile(ArrayList<SetLeafNodeSet> listSets, File file, ArrayList<PairNumText> setData, int setCount, int seqFile){
        Common common = new Common();
        ArrayList<SetLeafNodeSet> listSetsTest = new ArrayList<>(listSets);

        String codeOfSet = (setCount)+"-"+seqFile;
        int countLine = 1;
        int indexLine = 0;
        int size = listSetsTest.get(setCount-1).getLeafNodeSets().size();
        String line;

        String [] arrLine = common.initArrayWithSpace(size);

        for(PairNumText o : setData){
            //System.out.println(o.getCode().split("-")[2]);
            do {
                //System.out.println("INI NIH : "+indexLine);
                if(Integer.parseInt(o.getCode().split("-")[2]) == indexLine){
                    arrLine[indexLine] = getContentFromTestDoc(o.getIndex());
                    indexLine++;
                    break;
                }
                else{
                    indexLine++;
                }

                if(indexLine >= size){
                    line = common.getSetLine(arrLine, codeOfSet, countLine);
                    common.printOnFile(file, line);

                    indexLine = 0;
                    arrLine = common.initArrayWithSpace(size);
                    countLine++;
                }
            } while (indexLine < size);
        }

        if(indexLine > 0 && indexLine <= size){
            line = common.getSetLine(arrLine, codeOfSet, countLine);
            common.printOnFile(file, line);
        }

        /*for (Map.Entry<Integer, String> entry : setData.entrySet()) {

            String[] arr = entry.getValue().split("-");

            substaction = Integer.parseInt(arr[2]) - previousIndex;
            System.out.println(entry.getKey()+":"+entry.getValue()+ "=>"+ substaction);
            //System.out.println("PAIR :" + Integer.parseInt(entry.getValue().substring(3)) +" - " + previousIndex+ " = "+substaction);

            //Its Mean First Line
            if(substaction == 0) {
                line = codeOfSet +"-"+countLine+"\t" + entry.getKey();
            }
            else if(substaction == 1){

                if(line.equals("")) line = codeOfSet +"-"+countLine+"\t"+" ";

                line = line+"\t" + entry.getKey();
                previousIndex = Integer.parseInt(arr[2]);
            }
            else if(substaction > 1 && substaction < endLine){

                if(line.equals("")) line = codeOfSet +"-"+countLine;

                for(int i = 1; i < substaction; i++){
                    line = line+"\t" +space;
                }
                line = line+"\t" + entry.getKey();
                previousIndex = Integer.parseInt(arr[2]);
            }
            else if(substaction < 0){
                PrintOnFile(file, line);
                previousIndex = Integer.parseInt(arr[2]);
                countLine++;
                line = codeOfSet +"-"+countLine+"\t" + entry.getKey();

            }
        }
        PrintOnFile(file, line);*/
    }

    private ArrayList<FSMVerification> cloneATestDocToFSMVerification (ArrayList<SegmentVerification> data){
        ArrayList<FSMVerification> result = new ArrayList<>();

        for(int i = 0; i<data.size(); i++){
            result.add(new FSMVerification(aTestDoc.get(data.get(i).getColIdTest())));
        }

        return result;
    }

    private FSM findMatchFSM(ArrayList<SegmentVerification> data){
        Integer start = data.get(0).getColIdTrain() == null ? -1 : data.get(0).getColIdTrain();

        for(int i = fsmc; i< this.FSMList.size(); i++){
            for(int j = 0; j<this.FSMList.get(i).getDetails().size(); j++){
                if(this.FSMList.get(i).getDetails().get(j).getColIdFrom().equals(start)){
                    this.fsmc = i;
                    return this.FSMList.get(i);
                }
            }
        }
        return null;
    }

    private void updateS(ArrayList<FSMVerification> nodeVerificationList){

        ArrayList<FSMVerification> result = new ArrayList<>(nodeVerificationList);

        for (FSMVerification o : result){
            if(S.get(findIndexOnP(o)) == null){
                if(o.getFinalState().getCodeNameOfSet() != null){
                    S.set(findIndexOnP(o), new PairNumText("S"+o.getFinalState().getCodeNameOfSet(), o.getFinalState().getColId()));
                }
                else {
                    S.set(findIndexOnP(o), new PairNumText(null, o.getFinalState().getColId()));
                }
            }
        }
    }

    private Integer findIndexOnP(FSMVerification fsmVerification){
        for(int i = 0; i< P.size(); i++){
            if(P.get(i).getIndex() == Integer.parseInt(fsmVerification.getLeafIndex())) return i;
        }
        return null;
    }

    private ArrayList<FSMVerification> findCandidateSet(ArrayList<FSMVerification> data, FSM fsm){
        Common common = new Common();

        int START_FSM = 0;
        int END_FSM = fsm.getDetails().size() - 1;

        int START_TEST_DATA = 0;
        int END_TEST_DATA = data.size()-1;

        int countSet = fsm.getNumberOfSet();
        boolean isSet;

        if(fsm.getDetails().get(START_FSM).getColIdFrom() != -1){
            String codeNameOfSet = fsm.getDetails().get(START_FSM).getCodeNameOfSetFrom() == null ? null : fsm.getDetails().get(START_FSM).getCodeNameOfSetFrom();
            data.get(START_TEST_DATA).getCandidateList().add(
                    new State(
                            fsm.getDetails().get(START_FSM).getColIdFrom(),
                            fsm.getDetails().get(START_FSM).getColSymbolFrom(),
                            codeNameOfSet
                    )
            );
        }
        else {
            START_TEST_DATA = START_TEST_DATA - 1;
        }

        if(fsm.getDetails().get(END_FSM).getProbability() != 0.0){
            String codeNameOfSet = fsm.getDetails().get(END_FSM).getCodeNameOfSetTo() == null ? null : fsm.getDetails().get(END_FSM).getCodeNameOfSetTo();
            data.get(END_TEST_DATA).getCandidateList().add(
                    new State(
                            fsm.getDetails().get(END_FSM).getColIdTo(),
                            fsm.getDetails().get(END_FSM).getColSymbolTo(),
                            codeNameOfSet
                    )
            );
        }
        else {
            END_TEST_DATA = END_TEST_DATA + 1;
        }

        int colIdVerificationStart = getMinIndexOfLeafnode(fsm) + 1;
        int colIdVerificationEnd = getMaxIndexOfLeafnode(fsm) - 1;


        for (int i = (START_TEST_DATA + 1); i < END_TEST_DATA; i++){

            for (int j = colIdVerificationStart; j <= colIdVerificationEnd; j++){

                isSet = false;

                String[] encodingResultFSMs;
                String encodingResultTestData;

                if(train.getTableA().get(j).getEncoding() == -1) isSet = true;

                if(isSet == true){

                    int startSet = getStartOfSet(train.getTableA().get(j).getLNMember());

                    for(int iSet = startSet; iSet < countSet; iSet++){
                        ArrayList<State> newCandidates = getCandidateFromSet((iSet+1), train.getTableSetList().get(iSet).getLeafNodeSets(), j, data.get(i));

                        if(!newCandidates.isEmpty()) {
                            for(State o : newCandidates){
                                data.get(i).getCandidateList().add(o);
                            }
                        }
                    }
                }
                else {
                    encodingResultFSMs = getEncodingResults(train.getTableA().get(j));
                    encodingResultTestData = getEncodingResult(train.getTableA().get(j).getEncoding(), data.get(i));

                    here: for(String encodingResultFSM : encodingResultFSMs){

                        //System.out.println(encodingResultFSM.replaceAll("-0", "") +" : "+encodingResultTestData);

                        if(encodingResultFSM.equals(encodingResultTestData)) {
                            data.get(i).getCandidateList().add(
                                    new State(
                                            j, common.getSymbol(train.getTableA().get(j).getLNType()), null
                                    )
                            );
                            break here;
                        }
                    }

                }
            }
        }

        return data;
    }

    private int getStartOfSet(String[] LNMembers){
        for(String o : LNMembers){
            if(!o.equals(" ")){
                return (Integer.parseInt(o.split("-")[0]) - 1);
            }
        }
        return 0;
    }

    private State findFinalState(State previousState, FSMVerification fsmVerification, FSM fsm) throws IOException {

        State finalState = fsmVerification.getCandidateList().get(0);
        Double prob = null, max = 0.0;

        if(previousState == null) return finalState;

        ArrayList<State> candidates = new ArrayList<>(fsmVerification.getCandidateList());

        //System.out.println("------------------------------------------------------------------");
        for(State candidate : candidates){

            double TP = getTransitionProbability(fsm, previousState, candidate);

            if(TP == 0.0) {
                prob = 0.0;
            }
            else {
                if(candidate.getColSymbolId()== 't') prob = 10.0;
                else prob = 0.0;

                prob = prob + (TP + getNaiveBayes(candidate, fsmVerification));
            }

            //System.out.println(previousState.getColSymbolId()+""+previousState.getColId()+ " -> " +candidate.getColSymbolId()+""+candidate.getColId()+" : "+ prob);

            if(prob > max){
                max = prob;
                finalState = candidate;
            }

        }

        /*if(finalState != null) {
            System.out.println("===FINAL STATE====");
            if(finalState.getCodeNameOfSet()== null) System.out.println(finalState.getColSymbolId()+""+finalState.getColId()+" : "+ max);
            else System.out.println(finalState.getColSymbolId()+""+finalState.getColId()+""+finalState.getCodeNameOfSet()+" : "+ max);
        }*/
        return finalState;
    }

    private Double getTransitionProbability(FSM fsm, State previousState, State candidate){
        double result = 0.0;
        for(FSMDetail fsmDetail :fsm.getDetails()){
            if(previousState.getCodeNameOfSet() != null && candidate.getCodeNameOfSet() == null){
                if(fsmDetail.getColIdFrom().equals(previousState.getColId())
                        && fsmDetail.getCodeNameOfSetFrom().equals(previousState.getCodeNameOfSet())
                        && fsmDetail.getColIdTo().equals(candidate.getColId())){
                    return fsmDetail.getProbability();
                }
            }
            else if(previousState.getCodeNameOfSet() == null && candidate.getCodeNameOfSet() != null){
                if(fsmDetail.getColIdFrom().equals(previousState.getColId())
                        && fsmDetail.getColIdTo().equals(candidate.getColId())
                        && fsmDetail.getCodeNameOfSetTo().equals(candidate.getCodeNameOfSet())){
                    return fsmDetail.getProbability();
                }
            }
            else if(previousState.getCodeNameOfSet() != null && candidate.getCodeNameOfSet() != null) {
                if (fsmDetail.getColIdFrom().equals(previousState.getColId())
                        && fsmDetail.getCodeNameOfSetFrom().equals(previousState.getCodeNameOfSet())
                        && fsmDetail.getColIdTo().equals(candidate.getColId())
                        && fsmDetail.getCodeNameOfSetTo().equals(candidate.getCodeNameOfSet())) {
                    return fsmDetail.getProbability();
                }
            }
            else {
                if(fsmDetail.getColIdFrom().equals(previousState.getColId()) && fsmDetail.getColIdTo().equals(candidate.getColId())){
                    return fsmDetail.getProbability();
                }
            }
        }
        return result;
    }

    private Double getNaiveBayes(State candidate, FSMVerification fsmVerification){

        Map<String, ArrayList<ProbabilityDistribution>> docDist;
        Double result;
        String key;

        // Differentiate of Set Leafnode or Non Set Leafnode

        if(candidate.getCodeNameOfSet() != null) {
            key = candidate.getColSymbolId()+""+candidate.getColId()+""+candidate.getCodeNameOfSet();
        }
        else {
            key = candidate.getColSymbolId()+""+candidate.getColId();
        }

        //Check value on master table;
        docDist = masterTable.get(key);

        result = getProbabilityFromDistributionProbabilityTable(docDist.get("Path"), fsmVerification.getPathId()) *
                 getProbabilityFromDistributionProbabilityTable(docDist.get("SimSeq"), fsmVerification.getSimSeqId()) *
                 getProbabilityFromDistributionProbabilityTable(docDist.get("PTypeSet"), fsmVerification.getPTypeSetId()) *
                 getProbabilityFromDistributionProbabilityTable(docDist.get("TypeSet"), fsmVerification.getTypeSetId()) *
                 getProbabilityFromDistributionProbabilityTable(docDist.get("Content"), fsmVerification.getContentId());

        return result;
    }

    private Double getProbabilityFromDistributionProbabilityTable(List<ProbabilityDistribution> table, String key){
        for(ProbabilityDistribution data : table){
            if(data.getCode().equals(key)) return data.getProbability();
        }
        return 0.0;
    }

    private String[] getEncodingResults(LeafNodeSet leafNodeSet){

        String[] result = null;
        int length, index = 0;


        switch(leafNodeSet.getEncoding()) {
            case 0: //PathId - ContentId
                length = leafNodeSet.getPathId().length * leafNodeSet.getContentId().length;
                result = new String[length];

                for(int i = 0; i < leafNodeSet.getPathId().length; i++){
                    for(int j = 0; j < leafNodeSet.getContentId().length; j++) {
                        result[index] = leafNodeSet.getPathId()[i] + "-" + leafNodeSet.getContentId()[j];
                        index++;
                    }
                }
                break;
            case 1: //SimSeqId - TypeSetId
                length = leafNodeSet.getSimSeqId().length * leafNodeSet.getTypeSetId().length;
                result = new String[length];

                for(int i = 0; i < leafNodeSet.getSimSeqId().length; i++){
                    for(int j = 0; j < leafNodeSet.getTypeSetId().length; j++){
                        result[index] = leafNodeSet.getSimSeqId()[i]+"-"+leafNodeSet.getTypeSetId()[j];
                        index++;
                    }
                }
                break;
            case 2: //SimSeqId - PTypeSetId
                length = leafNodeSet.getSimSeqId().length * leafNodeSet.getPTypeSetId().length;
                result = new String[length];

                for(int i = 0; i < leafNodeSet.getSimSeqId().length; i++){
                    for(int j = 0; j < leafNodeSet.getPTypeSetId().length; j++){
                        result[index] = leafNodeSet.getSimSeqId()[i]+"-"+leafNodeSet.getPTypeSetId()[j];
                        index++;
                    }
                }
                break;
            case 3: //SimSeqId
                length = leafNodeSet.getSimSeqId().length;
                result = new String[length];

                for(int i = 0; i < leafNodeSet.getSimSeqId().length; i++){
                    result[index] = leafNodeSet.getSimSeqId()[i];
                    index++;
                }
                break;
            case 4: //PathId - TypeSetId
                length = leafNodeSet.getPathId().length * leafNodeSet.getTypeSetId().length;
                result = new String[length];

                for(int i = 0; i < leafNodeSet.getPathId().length; i++){
                    for(int j = 0; j < leafNodeSet.getTypeSetId().length; j++){
                        result[index] = leafNodeSet.getPathId()[i]+"-"+leafNodeSet.getTypeSetId()[j];
                        index++;
                    }
                }
                break;
            case 5: //PathId - PTypeSetId
                length = leafNodeSet.getPathId().length * leafNodeSet.getPTypeSetId().length;
                result = new String[length];

                for(int i = 0; i < leafNodeSet.getPathId().length; i++){
                    for(int j = 0; j < leafNodeSet.getPTypeSetId().length; j++){
                        result[index] = leafNodeSet.getPathId()[i]+"-"+leafNodeSet.getPTypeSetId()[j];
                        index++;
                    }
                }
                break;
            case 6: //PathId
                length = leafNodeSet.getPathId().length;
                result = new String[length];

                for(int i = 0; i < leafNodeSet.getPathId().length; i++){
                    result[index] = leafNodeSet.getPathId()[i];
                    index++;
                }
                break;
        }

        return result;
    }

    private String getEncodingResult(byte encoding, Element element){

        String result = null;

        switch(encoding) {
            case 0: //PathId - ContentId
                result = element.getPathId()+"-"+element.getContentId();
                break;
            case 1: //SimSeqId - TypeSetId
                result = element.getSimSeqId()+"-"+element.getTypeSetId();
                break;
            case 2: //SimSeqId - PTypeSetId
                result = element.getSimSeqId()+"-"+element.getPTypeSetId();
                break;
            case 3: //SimSeqId
                result = element.getSimSeqId();
                break;
            case 4: //PathId - TypeSetId
                result = element.getPathId()+"-"+element.getTypeSetId();
                break;
            case 5: //PathId - PTypeSetId
                result = element.getPathId()+"-"+element.getPTypeSetId();
                break;
            case 6: //PathId
                result = element.getPathId();
                break;
        }

        return result;
    }

    private ArrayList<State> getCandidateFromSet(int setNumber, List<LeafNodeSet> tableSet, int colId, Element data){

        Common common = new Common();
        ArrayList<State> result = new ArrayList<>();
        String[] encodingResultFSMs;
        String encodingResultTestData;
        int count = 0;

        for(LeafNodeSet o : tableSet){
            encodingResultFSMs = getEncodingResults(o);
            encodingResultTestData = getEncodingResult(o.getEncoding(), data);

            here: for(String encodingResultFSM : encodingResultFSMs){

                if(encodingResultFSM.equals(encodingResultTestData)){
                    result.add(new State(
                            colId, common.getSymbol(o.getLNType()), "-"+setNumber+"-"+count
                    ));
                    break here;
                }
            }

            count++;
        }
        return result;
    }

    public Integer getMaxIndexOfLeafnode(FSM fsm){

        int max = 0;

        for(FSMDetail o : fsm.getDetails()){
            if(max < o.getColIdTo()){ max = o.getColIdTo();}
        }

        return max;
    }

    public Integer getMinIndexOfLeafnode(FSM fsm){

        int min = 0;

        for(FSMDetail o : fsm.getDetails()){
            if(min > o.getColIdFrom()){ min = o.getColIdFrom();}
        }

        return min;
    }

    String getContentFromTestDoc(Integer i){
        int idx = Collections.binarySearch(aTestDoc, new Element(i.toString(), null, null, null, null, null, null));
        return aTestDoc.get(idx).getContent();
    }
}
