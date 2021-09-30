package relative_placement;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class ScorerRelPlacement extends Scorer {
    @Override
    /**
     * Rank constestants by relative placement scoring.  This is called
     * after loadCSV.  With Relative Placement, the input should be
     * ordinals (integer rankings) of each contestant by the judges.
     *
     * Ordinals are typically in the range of [1, numContestants].
     * However, it can also be maxRank, less than numContestants.
     */
    public void rankContestants() {
        // ----------------  calculate count and quality  -------------------
        // initiate count and quality
        count = new int[numContestants][numContestants+1];
        quality =  new int[numContestants][numContestants+1];

        // prepare variables
        int val;
        ArrayList<Integer> contestantRanks;

        for (int indContestant = 0; indContestant < numContestants; indContestant++) {
            // get the current ranks for given contestant
            contestantRanks = ranks.get(indContestant);

            // update majority count and quality matrices
            for (int indJudge = 0; indJudge < numJudges - 1; indJudge++) {
                val = contestantRanks.get(indJudge);
                for (int indPlacement = val; indPlacement < count[0].length; indPlacement++) {
                    // track the count of ranks under a placment
                    count[indContestant][indPlacement - 1] ++;

                    // sum ranks
                    quality[indContestant][indPlacement - 1] += val;
                }
            }
        }

        // ----------------  rankContestants based on meeting majority  -------------
        processMajority();

        // update sorted
        int tmp1;
        logger.finest("sortedIndex length = " + sortedIndex.length);
        for (int indContestant = 0; indContestant < numContestants; indContestant++) {
            try {
                tmp1 = count[indContestant][count[0].length - 1];
                logger.finest("tmp1 = " + tmp1);
                sortedIndex[tmp1 - 1] = indContestant;
            }
            catch (Exception e) {
                logger.severe("Exception caught = " + e);
            }
        }
        logger.finer("Finished sorting");
    }


    /**
     * Begin processing the rankings, starting at a low placement and
     * gradually increasing.  At each placement, identify contestants
     * that have a majority of ranks less than or equal to the placement.
     *
     * If multiple contestants meet this criteria at a given placement
     * level, proceed to run through the tie breaker.
     */
    protected void processMajority() {
        // initialize variables
        int lastAssignedPlace = 1;
        int[] tieBreaks;
        ArrayList<Integer> activeList;

        for (int indP = 0; indP < count[0].length - 1; indP++) {

            // ---------------- track contestant with majority  -------------
            // identify contestants meeting majority at curent placement
            activeList = new ArrayList();
            for (int indC = 0; indC < numContestants; indC++) {
                if (count[indC][indP] >= majority){
                    activeList.add(indC);
                }
            }

            if (activeList.size() == 1) {
                // --------------------  single placement  ------------------
                for (int indC = indP + 1; indC < count[0].length; indC++)
                    // set counts past this to -1 so it is not looked at
                    // for the next iteration
                    count[activeList.get(0)][indC] = -1;

                // last column is the ranking
                count[activeList.get(0)][count[0].length - 1] = lastAssignedPlace++;

            }
            else if (activeList.size() > 1) {
                // multiple passing, run tie break
                tieBreaks = processTieBreaks(activeList, indP);

                int cWinner;
                for (int ind0 = 0; ind0 < tieBreaks.length; ind0++) {
                    cWinner = tieBreaks[ind0];

                    // change counts to -1 so already ranked contestants
                    // are no long considered for next round
                    for (int indC = indP + 1; indC < count[0].length; indC++)
                        count[cWinner][indC] = -1;

                    // update last column with the placement
                    count[cWinner][count[0].length - 1] = lastAssignedPlace++;
                }
            }
        }
    }

    /**
     * Process tie breaks
     * @param select A list of contestants under consideration
     * @param place The placement level
     * @return The sorted list...breaking the tie
     */
    protected int[] processTieBreaks(ArrayList<Integer> select, int place) {
        int[] out = new int[select.size()];
        int completedOut = 0;
        int[] tmp;
        ArrayList<Integer> tmp1 = new ArrayList();
        ArrayList<Integer> tmp2 = new ArrayList();

        if (out.length == 1) {
            // exit condition, single selection, return
            out[0] = select.get(0);
            return out;
        }
        else if (place == maxRank) {
            // final round of tie break
            // NOTE: assuming 2 couples at this stage
            int ratio = 0;
            for (int indJ = 0; indJ < numJudges - 1; indJ++){

                if (ranksInt[select.get(0)][indJ] < ranksInt[select.get(1)][indJ])
                    ratio += 1;
                else if (ranksInt[select.get(0)][indJ] > ranksInt[select.get(1)][indJ])

                    ratio -= 1;
            }
            if (ratio > 0) {
                out[0] = select.get(0);
                out[1] = select.get(1);
            }
            else {
                out[0] = select.get(1);
                out[1] = select.get(0);
            }
            return out;
        }
        else {
            // ---------------------  identify majority  -------------------
            // break tie based on number judge votes at this placement
            tmp1 = tieBreakCount(select, place);

            // if majority breaks tie, rankContestants
            if (tmp1.size() == 1) {
                // tie broken, rankContestants
                out[completedOut] = tmp1.get(0);
                completedOut++;

                // update tmp for removal
                tmp = new int[1];
                tmp[0] = tmp1.get(0);
            }
            else {
                // use 2nd tie break, quality
                // ---------------------  identify best quality ----------------
                tmp2 = tieBreakQuality(tmp1, place);

                // ---------------------  rankContestants this block  -------------------
                if (tmp2.size() == 1) {
                    out[completedOut] = tmp2.get(0);
                    completedOut++;

                    // update tmp for removal
                    tmp = new int[1];
                    tmp[0] = tmp2.get(0);
                }
                else {
                    if (place >= maxRank) {
                        tmp = new int[tmp2.size()];
                        for (int ind=0; ind<tmp2.size(); ind++) {
                            tmp[ind] = tmp2.get(ind);
                            logger.finer("Tie detected with " + tmp2.get(ind));
                            out[completedOut] = tmp2.get(ind);
                            completedOut++;
                        }
                    }
                    else {
                        tmp = new int[0];

                    }
                }
            }

            if (completedOut == out.length)
                return out;

            if (tmp.length > 0) {
                // remove subset that passed
                for (int elem : tmp) {
                    for (int ind0 = 0; ind0 < select.size(); ind0++) {
                        if (elem == select.get(ind0)){
                            logger.finest("Removing element " + elem);
                            select.remove(ind0);
                            break;
                        }
                    }
                }

                // ----------------  rankContestants remaining  -----------------------
                if (select.size() == 0) {
                    logger.finer("Select list cleared...exiting processTieBreaks");
                    // Done!!
                    return out;
                }

                // continue procressing remaining list
                logger.finer("Continue processing on " + select.size() + " elements");
                tmp = processTieBreaks(select, place);
            }
            else {
                logger.finer("A batch of contestants are tied, returning all of them");
                logger.warning("The placement is still monotonically increasing...they should all be set the same to denote a tie");
                for (int ind0 = 0; ind0 < select.size(); ind0++) {
                    out[completedOut] = select.get(ind0);
                    completedOut++;
                }
                return out;
            }

            for (int ind0 = 0; ind0 < tmp.length; ind0++) {
                out[completedOut] = tmp[ind0];
                completedOut ++;
            }
        }
        return out;
    }

    /**
     * Break ties based on number of positive judge votes for givent placement
     *
     * @param select The list of couples left under consideration
     * @param place The placement level to look at.
     * @return The subset of the input list that has the best count.
     */
    protected ArrayList<Integer> tieBreakCount(ArrayList<Integer> select, int place) {
        ArrayList<Integer> tmp1 = new ArrayList();
        int bestCount = 0;
        for (int indC : select) {
            if (count[indC][place] > bestCount) {
                // new best...reset the output array
                bestCount = count[indC][place];
                tmp1 = new ArrayList<>();
                tmp1.add(indC);
            }
            else if (count[indC][place] == bestCount) {
                // a tie to the best
                tmp1.add(indC);
            }
        }
        logger.finer(tmp1.size() + " of " + select.size() + " in tieBreakCount");
        return tmp1;
    }

    /**
     * Apply tie break based on quality, a sum of the ranks meeting the placement
     * @param select The subset of contestants being considered
     * @param place The current placements under consideration
     * @return The chosen best or a subset that is still tied.
     */
    protected ArrayList<Integer> tieBreakQuality(ArrayList<Integer> select, int place) {
        ArrayList<Integer> tmp2 = new ArrayList();
        int bestQuality = 32756;//numJudges * numJudges;
        for (int indC : select){
            if (quality[indC][place] < bestQuality) {
                // track best quality (lower is better)
                bestQuality = quality[indC][place];
                tmp2 = new ArrayList<>();
                tmp2.add(indC);
            }
            else if (quality[indC][place] == bestQuality) {
                // append to list of new ties
                tmp2.add(indC);
            }
        }
        logger.finer(tmp2.size() + " of " + select.size() + " in tieBreakQuality (" + place + ")");
        return tmp2;
    }

    @Override
    /**
     * Load CSV
     *
     * The Relative placement scorer uses loadCSVOrdinal.
     */
    public void loadCSV(String path, boolean firstRowHeader, String token) {
        loadCSVOrdinal(path, firstRowHeader, token);
    }

    @Override
    /**
     * Write the output CSV file for Relative placement
     * @param path Path for the output file
     * @param token Token to use.
     */
    public void writeCSV(String path, String token) {
        String tmpS;
        try {
            File f = new File(path);
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));

            // ------------------  write header  ----------------------------
            writer.write("Leader" + token + "Follower" + token);
            for (String judge : judges)
                writer.write(judge + token);

            for (int indC = 0; indC < count[0].length - 1; indC++) {
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
            for (int indC = 0; indC < numContestants; indC++) {
                tmpC = sortedIndex[indC];
                writer.write("\n");

                writer.write(leaders.get(tmpC) + token +
                    followers.get(tmpC) + token);

                // write ranks
                for (int indJ = 0; indJ < numJudges; indJ++)
                    writer.write(ranksInt[tmpC][indJ] + token);

                // write counts
                for (int indC2 = 0; indC2 < count[0].length; indC2++) {
                    if (count[tmpC][indC2] == 0) {
                        tmpS = "    ";
                    }
                    else if (count[tmpC][indC2] < 0) {
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

    @Override
    /**
     * Update a data model with the results
     *
     * The column names are Placement, Leader, Follower, Judges, Count
     * @param data The data model
     */
    public void getSortedRank(DefaultTableModel data) {
        // clear old data
        data.setRowCount(0);
        data.setColumnCount(0);

        // -------------------  setup column names  -------------------------
        Vector columnNames = new Vector();
        columnNames.add("Placement");
        columnNames.add("Leaders");
        columnNames.add("Followers");
        for (String judge: judges){
            columnNames.add(judge);
        }
        for (int indP = 0; indP < count[0].length - 1; indP++){
            columnNames.add(Integer.toString(indP + 1));
        }

        data.setColumnIdentifiers(columnNames);

        // ---------------------  setup data  -------------------------------
        int tmpC;
        String tmpS;
        Vector cData;
        for (int indC = 0; indC < numContestants; indC++) {
            tmpC = sortedIndex[indC];
            cData = new Vector();
            cData.add(Integer.toString(indC + 1));
            cData.add(leaders.get(tmpC));
            cData.add(followers.get(tmpC));

            // add judges ranks
            for (int indJ = 0; indJ < numJudges; indJ++) {
                cData.add(Integer.toString(ranksInt[tmpC][indJ]));
            }

            // add counts
            for (int indP = 0; indP < count[0].length; indP++) {
                if (count[tmpC][indP] == 0) {
                    tmpS = "";
                }
                else if (count[tmpC][indP] < 0) {
                    tmpS = "--";
                }
                else{
                    tmpS = String.format("%3d", count[tmpC][indP]);
                }
                cData.add(tmpS);
            }
            data.addRow(cData);
        }
    }

    /**
     * Load in a CSV of judges rankings.  Perform relative placement.
     * Save sorted results to an output CSV file.
     *
     * @param args 3 element argument (INPUT.CSV OUTPUT.CSV TOKEN)
     */
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3)
            throw new RuntimeException("Expecting 2 input arguments, input csv and output csv");
        String token = ",";
        if (args.length == 3)
            token = args[2];

        System.out.println("Input CSV = " + args[0]);
        System.out.println("Output CSV = " + args[1]);

        Scorer myScorer = new ScorerRelPlacement();
        myScorer.loadCSV(args[0], true, token);
        myScorer.rankContestants();
        myScorer.writeCSV(args[1], token);
    }
}