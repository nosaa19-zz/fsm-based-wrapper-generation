/*
 * Needleman Wunsch is used to multiple sequence alignments via pairwise alignments based on only TECId similarity.
 */
package Service;

import Domain.PairNumText;

import java.util.ArrayList;
import java.util.List;

public class NWSim {

    private ArrayList<PairNumText> OutS;
    private ArrayList<PairNumText> OutT;
    private boolean Matched;

    public void seqAlign(ArrayList<PairNumText> CodeLongest, ArrayList<PairNumText> CodeInsert) {
        int Match = 1;
        int MisMatch = -4;
        int Gap = -2;
        ArrayList<PairNumText> StarDoc = new ArrayList<>(CodeLongest);
        StarDoc.add(0, null);
        ArrayList<PairNumText> AnotherDoc = new ArrayList<>(CodeInsert);
        AnotherDoc.add(0, null);

        int[][] Sim = CountSim(StarDoc, AnotherDoc, Match, MisMatch);
        int[][] Score = CountScore(StarDoc.size(), AnotherDoc.size(), Sim, Gap);
        TraceBack(StarDoc, AnotherDoc, Score, Sim, Gap);
    }

    private void TraceBack(ArrayList<PairNumText> S, ArrayList<PairNumText> T, int[][] Score, int[][] Sim, int Gap) {
        int i = S.size() - 1;
        int j = T.size() - 1;
        ArrayList<Character> ArrSelected;
        OutS = new ArrayList<>();
        OutT = new ArrayList<>();
        Matched = false;
        char Direction;
        int Match = Integer.MIN_VALUE;
        int Delete = Integer.MIN_VALUE;
        int Insert = Integer.MIN_VALUE;
        int MaxScore;
        while (i > 0 || j > 0) {
            ArrSelected = new ArrayList<>();

            if (i > 0 && j > 0) {
                Match = Score[i - 1][j - 1];
                if ((Match + Sim[i][j]) == Score[i][j]) {
                    ArrSelected.add('M');
                }
            }

            if (i > 0) {
                Delete = Score[i - 1][j];
                if ((Delete + Gap) == Score[i][j]) {
                    ArrSelected.add('D');
                }
            }

            if (j > 0) {
                Insert = Score[i][j - 1];
                if ((Insert + Gap) == Score[i][j]) {
                    ArrSelected.add('I');
                }
            }

            Direction = ArrSelected.get(0);
            switch (Direction) {
                case 'M':
                    MaxScore = Match;
                    break;
                case 'D':
                    MaxScore = Delete;
                    break;
                default:
                    MaxScore = Insert;
                    break;
            }

            for (int iD = 1; iD < ArrSelected.size(); iD++) {
                switch (ArrSelected.get(iD)) {
                    case 'M':
                        if (Match > MaxScore) {
                            MaxScore = Match;
                            Direction = 'M';
                        }
                        break;
                    case 'D':
                        if (Delete > MaxScore) {
                            MaxScore = Delete;
                            Direction = 'D';
                        }
                        break;
                    default:
                        MaxScore = Insert;
                        Direction = 'I';
                        break;
                }
            }

            switch (Direction) {
                case 'M':
                    OutS.add(0, S.get(i));
                    OutT.add(0, T.get(j));
                    Matched = true;
                    j--;
                    i--;
                    break;
                case 'D':
                    OutS.add(0, S.get(i));
                    OutT.add(0, null);
                    i--;
                    break;
                default:
                    OutS.add(0, null);
                    OutT.add(0, T.get(j));
                    j--;
                    break;
            }

        }

        /*for (int iS = 0; iS < OutS.size(); iS++) {
            System.out.print(OutS.get(iS) + "\t");
        }
        System.out.println();

        for (int iT = 0; iT < OutT.size(); iT++) {
            System.out.print(OutT.get(iT) + "\t");
        }
        System.out.println();*/
    }

    private int[][] CountScore(int M, int N, int[][] Sim, int Gap) {
        int[][] Score = new int[M][N];
        int Match, Delete, Insert;

        for (int i = 1; i < M; i++) {
            Score[i][0] = i * Gap;
        }

        for (int j = 1; j < N; j++) {
            Score[0][j] = j * Gap;
        }

        for (int i = 1; i < M; i++) {
            for (int j = 1; j < N; j++) {
                Match = Score[i - 1][j - 1] + Sim[i][j];
                Delete = Score[i - 1][j] + Gap;
                Insert = Score[i][j - 1] + Gap;

                Score[i][j] = Maximum(Match, Delete, Insert);

            }
        }
        return Score;
    }

    private int Maximum(int a, int b, int c) {
        if (a > b) {
            return a > c ? a : c;
        } else {
            return b > c ? b : c;
        }
    }

    private int[][] CountSim(ArrayList<PairNumText> S, ArrayList<PairNumText> T, int Match, int MisMatch) {
        PairNumText SiS;
        PairNumText TiT;
        int SSize = S.size();
        int TSize = T.size();
        String [] codes;

        int[][] Sim = new int[SSize][TSize];


        for (int iS = 1; iS < SSize; iS++) {
            for (int iT = 1; iT < TSize; iT++) {
                SiS = S.get(iS);
                TiT = T.get(iT);

                Sim[iS][iT] = MisMatch;

                codes = SiS.getCode().split(",");

                here: for(String code : codes){
                    if (code.equals(TiT.getCode())) {
                        Sim[iS][iT] = Match;
                        break here;
                    }
                }
            }
        }

        return Sim;
    }

    public ArrayList<PairNumText> GetSOut() {
        return this.OutS;
    }

    public ArrayList<PairNumText> GetTOut() {
        return this.OutT;
    }

    public boolean GetMatched() {
        return this.Matched;
    }
}
