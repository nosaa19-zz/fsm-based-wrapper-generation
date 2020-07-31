import Service.Preprocessing;
import Service.PrintJsonFormat;
import Service.Test;
import Service.Train;

public class Main {

    public static void main(String[] args) throws Exception {

        String Folder;

        if(args.length > 0) Folder = args[0];
        else Folder = "F:/dataset/ETL/Test5/";

        Preprocessing preprocessing = new Preprocessing();
        preprocessing.collectFeatures(Folder);
        /*System.out.println("Collecting Feature Done!");
        System.out.println("===================================================================================");*/

        Train training = new Train();
        training.run(Folder);
        /*System.out.println("Training Done!");
        System.out.println("===================================================================================");*/

        Test testing = new Test();
        testing.run(Folder,
                    training.getSchema(),
                    training.getMasterTable(),
                    training.getMTList(),
                    training.getFSMList());
        /*System.out.println("Testing Done!");
        System.out.println("===================================================================================");*/

        PrintJsonFormat out = new PrintJsonFormat();
        out.Run(Folder);

    }
}
