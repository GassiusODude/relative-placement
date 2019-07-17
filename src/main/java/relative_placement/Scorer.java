package relative_placement;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.RuntimeException;
import java.util.Arrays;

public class Scorer {
    private ArrayList<String> leaders = new ArrayList();
    private ArrayList<String> followers = new ArrayList();
    private ArrayList<String> judges = new ArrayList();
    private ArrayList<ArrayList> ranks = new ArrayList<ArrayList>();
    private int[][] ranksInt = new int[1][1];
    private int numJudges = 0;
    private int numContestants = 0;
    private int majority = 3;
    private int[][] count;
    private int[][] quality;
    private int[] sortedIndex;
    public Scorer(){
        leaders = new ArrayList();
        followers = new ArrayList();
        judges = new ArrayList();
        ranks = new ArrayList<ArrayList>();
        numJudges = 0;
        numContestants = 0;
        majority = 3;
    }
    public int[] getSorted(){
        return sortedIndex;
    }
    public void display(){
        System.out.println("Judges");
        for (String judge : judges){
            System.out.println("\t" + judge);
        }
        System.out.println("Leaders");
        for (String leader : leaders){
            System.out.println("\t" + leader);
        }
        System.out.println("Followers");
        for (String follow : followers){
            System.out.println("\t" + follow);
        }
        System.out.println("Num Contestants = " + numContestants);
    }

    /**
     * Load in judge's rankings from a comma-separated variable file.
     * @param path Path to the file
     * @param firstRowHeader Boolean describing if first row is header
     * @param token String token to separate values, typically commas
     */
    public void loadCSV(String path, boolean firstRowHeader, String token){
        File f = new File(path);
        BufferedReader br = null;
        try{
            // open file and get buffered stream
            FileInputStream fis = new FileInputStream(f);
            br = new BufferedReader(new InputStreamReader(fis));
            
            // allocate string to read one line of the file
            // allocate array of string for parsed output
            String strLine;

            String[] elements;
            int numFeatures = 0;
            while ((strLine = br.readLine()) != null)
            {
                // parse the file looking for comma separation
                elements = strLine.split(token);

                if (numFeatures == 0){
                    // first line should be [Leader, Follower, Judges]
                    if (elements.length % 2 == 1)
                        throw new RuntimeException("Expecting even number of columns");
                    if (elements.length < 6)
                        // leader, follow, min 3 judges + 1 head judge
                        throw new RuntimeException("Expecting 6 or more columns");
                    numFeatures = elements.length;
                    numJudges = elements.length - 2;
                    majority = numJudges / 2;
                    for (int ind0 = 2; ind0 < numJudges + 2; ind0++){
                        // track judges (odd) + 1 head judge
                        judges.add(elements[ind0]);
                    }
                    continue;
                }
                else
                    if (elements.length != numFeatures){
                        throw new RuntimeException("Number of columns should match!");
                    }

                leaders.add(elements[0]);
                followers.add(elements[1]);
                ArrayList currRank = new ArrayList<Integer>();
                for (int ind0 = 2; ind0 < numFeatures; ind0 ++){
                    currRank.add(Integer.parseInt(elements[ind0]));
                }
                ranks.add(currRank);
                numContestants++;
            }
            // ---------  update ranksInt as a convenience variable  --------
            ranksInt = new int[numContestants][numJudges];
            for (int indC = 0; indC < numContestants; indC++){
                for (int indJ = 0; indJ < numJudges; indJ++ ){
                    ranksInt[indC][indJ] = Integer.parseInt(
                        ranks.get(indC).get(indJ).toString());
                }
            }
            validateRanks();
            sortedIndex = new int[numContestants];
            for (int indC = 0; indC < numContestants; indC++){
                sortedIndex[indC] = indC;
            }
        }
        catch(java.io.FileNotFoundException fnfe){System.out.println("File not found.");}
        catch(java.io.IOException ioe){System.out.println("IO Exception");}
        finally{
            try{
                if (br != null)
                    br.close();
            }catch(IOException ioe){

            }
        }
    }
    private void validateRanks(){
        // verify that each judge ranks is unique 1 .. numContestants
        int[] numVals = new int[numContestants];
        int curr;
        for (int indJ = 0; indJ < numJudges-1; indJ++){
            Arrays.fill(numVals, 0);
            for (int indC = 0; indC < numContestants; indC++){
                curr = ranksInt[indC][indJ]-1;
                numVals[curr]++;
                if (numVals[curr] > 1)
                    throw new RuntimeException(judges.get(indJ) + " has duplicate ranking");
            }
        }
    }

    private int[] processTieBreak(ArrayList<Integer> select, int place){
        int[] out = new int[select.size()];
        int completedOut = 0;
        int[] tmp;
        ArrayList<Integer> tmp1 = new ArrayList();
        ArrayList<Integer> tmp2 = new ArrayList();

        if (out.length == 1){
            // exit condition, single selection, return
            out[0] = select.get(0);
            return out;
        }
        else if (place == numJudges){
            // final round of tie break
            // NOTE: assuming 2 couples at this stage
            int ratio = 0;
            for (int indJ = 0; indJ < numJudges - 1; indJ++){

                if (ranksInt[select.get(0)][indJ] < ranksInt[select.get(1)][indJ])
                    ratio += 1;
                else
                    ratio -= 1;
            }
            if (ratio > 0){
                out[0] = select.get(0);
                out[1] = select.get(1);
            }
            else{
                out[0] = select.get(1);
                out[1] = select.get(0);
            }
            return out;
        }
        else{
            // ---------------------  identify majority  -------------------
            int bestCount = 0;
            for (int indC : select){
                if (count[indC][place] > bestCount){
                    bestCount = count[indC][place];
                    tmp1 = new ArrayList<>();
                    tmp1.add(indC);
                }
                else if (count[indC][place] == bestCount){
                    // append to list of new ties
                    tmp1.add(indC);
                }
            }

            // if majority breaks tie, process
            if (tmp1.size() == 1){
                // tie broken, process
                out[completedOut] = tmp1.get(0);
                completedOut++;

                // update tmp for removal
                tmp = new int[1];
                tmp[0] = tmp1.get(0);
            }
            else{
                // use 2nd tie break, quality
                // ---------------------  identify best quality ----------------
                int bestQuality = numJudges * numJudges;
                for (int indC : tmp1){
                    if (quality[indC][place] < bestQuality){
                        // track best quality (lower is better)
                        bestQuality = quality[indC][place];
                        tmp2 = new ArrayList<>();
                        tmp2.add(indC);
                    }
                    else if (quality[indC][place] == bestQuality){
                        // append to list of new ties
                        tmp2.add(indC);
                    }
                }

                // ---------------------  process this block  -------------------
                if (tmp2.size() == 1){
                    out[completedOut] = tmp2.get(0);
                    completedOut++;

                    // update tmp for removal
                    tmp = new int[1];
                    tmp[0] = tmp2.get(0);
                }
                else{
                    // break new tie
                    tmp = processTieBreak(tmp2, place + 1);
                    for (int ind0 = 0; ind0 < tmp.length; ind0++){
                        out[completedOut] = tmp[ind0];
                        completedOut++;
                    }
                }
            }
            for (int elem : tmp){
                for (int ind0 = 0; ind0 < select.size(); ind0++){
                    if (elem == select.get(ind0)){
                        select.remove(ind0);
                        break;
                    }
                }
            }
            
            // ----------------  process remaining  -------------------------
            if (select.size() == 0){
                // Done!!
                return out;
            }
            tmp = processTieBreak(select, place);
            for (int ind0 = 0; ind0 < tmp.length; ind0++){
                out[completedOut] = tmp[ind0];
                completedOut ++;
            }
        }
        return out;
    }

    private void processMajority(){
        // ----------------------  place  -----------------------------------
        int lastAssignedPlace = 1;
        int[] tieBreaks;

        ArrayList<Integer> tmp;

        for (int indP = 0; indP < numContestants - 1; indP++){

            // ---------------- track contestant with majority  -------------
            tmp = new ArrayList();
            for (int indC = 0; indC < numContestants; indC++){
                if (count[indC][indP] >= majority){
                    tmp.add(indC);

                }
            }
            
            if (tmp.size() == 1){
                // --------------------  single placement  ------------------
                for (int indC = indP + 1; indC < numContestants; indC++)
                    // set counts past this to -1 so it is not looked at 
                    // for the next iteration
                    count[tmp.get(0)][indC] = -1;
                
                // last column is the ranking
                count[tmp.get(0)][numContestants - 1] = lastAssignedPlace++;
                
            }
            else if (tmp.size() > 1){
                // multiple passing, run tie break
                tieBreaks = processTieBreak(tmp, indP);

                int cWinner;
                for (int ind0 = 0; ind0 < tieBreaks.length; ind0++){
                    cWinner = tieBreaks[ind0];

                    // change counts to -1 so already ranked contestants 
                    // are no long considered for next round
                    for (int indC = indP + 1; indC < numContestants; indC++)
                        count[cWinner][indC] = -1;

                    // update last column with the placement
                    count[cWinner][numContestants - 1] = lastAssignedPlace++;
                }
            }
        }
    }

    public void printState(){
        String tmpS;
        int tmpC;
        // -----------------------  display  --------------------------------
        for (int indC = 0; indC < numContestants; indC++){
            tmpC = sortedIndex[indC];
            System.out.print(leaders.get(tmpC) + "\t");
            System.out.print(followers.get(tmpC) + "\t");

            for (int indP = 0; indP < numContestants; indP++){
                if (count[tmpC][indP] == 0){
                    tmpS = "    ";
                }
                else if (count[tmpC][indP] < 0){
                    tmpS = " -- ";
                }
                else{
                    tmpS = String.format("%3d,", count[tmpC][indP]);
                }
                System.out.printf(tmpS );
            }
            System.out.println();
        }
    }
    /**
     * Write the output CSV file
     * @param path Path for the output file
     * @param token Token to use.
     */
    public void writeCSV(String path, String token){
        String tmpS;
        try{
            File f = new File(path);
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            // ------------------  write header  ----------------------------
            writer.write("Leader" + token + "Follower" + token);
            for (String judge : judges)
                writer.write(judge + token);

            for (int indC = 0; indC < numContestants - 1; indC++){
                if (indC == 0)
                    writer.write("1st" + token);
                else if (indC == 1)
                    writer.write("1st - 2nd" + token);
                else if (indC == 2)
                    writer.write("1st - 3rd" + token);
                else
                    writer.write("1st - " + (indC + 1) + "th" + token);

            }
            writer.write("Placement");

            // -------------------  write each row  ------------------------
            int tmpC;
            for (int indC = 0; indC < numContestants; indC++){
                tmpC = sortedIndex[indC];
                writer.write("\n");

                writer.write(leaders.get(tmpC) + token + 
                    followers.get(tmpC) + token);

                // write ranks
                for (int indJ = 0; indJ < numJudges; indJ++)
                    writer.write(ranksInt[tmpC][indJ] + token);
                
                // write counts
                for (int indC2 = 0; indC2 < numContestants; indC2++){
                    if (count[tmpC][indC2] == 0){
                        tmpS = "    ";
                    }
                    else if (count[tmpC][indC2] < 0){
                        tmpS = " -- ";
                    }
                    else{
                        tmpS = String.format("%3d ", count[tmpC][indC2]);
                    }
                    writer.write(tmpS + token);
                }
            }
            writer.close();

        }     
        catch(java.io.FileNotFoundException fnfe){System.out.println("File not found.");}
        catch(java.io.IOException ioe){System.out.println("IO Exception");}
    }

    /**
     * Calculate and update count and quality matrices.
     */
    public void process(){
        // ----------------  calculate count and quality  -------------------
        count = new int[numContestants][numContestants];
        quality =  new int[numContestants][numContestants];
        int val;

        ArrayList<Integer> tmp;

        for (int indC = 0; indC < numContestants; indC++){
            // get the current ranks for given contestant
            tmp = ranks.get(indC);

            // update majority count and quality matrices
            for (int indJ = 0; indJ < numJudges - 1; indJ++){
                val = tmp.get(indJ);
                for (int indP = val; indP < numContestants; indP++){
                    count[indC][indP - 1] ++;
                    quality[indC][indP - 1] += val;
                }
            }
        }

        // ----------------  process based on meeting majority  -------------
        processMajority();

        // update sorted
        int tmp1;
        for (int indC = 0; indC < numContestants; indC++){
            tmp1 = count[indC][numContestants - 1];

            sortedIndex[tmp1 - 1] = indC;
        }
        printState();
    }
    /**
     * Load in a CSV of judges rankings.  Perform relative placement.
     * Save sorted results to an output CSV file.
     * 
     * @param args 3 element argument (INPUT.CSV OUTPUT.CSV TOKEN)
     */
    public static void main(String[] args){
        if (args.length < 2 || args.length > 3)
            throw new RuntimeException("Expecting 2 input arguments, input csv and output csv");
        String token = ",";
        if (args.length == 3)
            token = args[2];

        System.out.println("Input CSV = " + args[0]);
        System.out.println("Output CSV = " + args[1]);

        Scorer myScorer = new Scorer();
        myScorer.loadCSV(args[0], true, token);
        myScorer.process();
        myScorer.writeCSV(args[1], token);
    }
}
