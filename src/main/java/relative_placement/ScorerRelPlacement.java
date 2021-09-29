package relative_placement;
import java.util.ArrayList;

public class ScorerRelPlacement extends Scorer {
    @Override
    public void rankContestants() {
        // ----------------  calculate count and quality  -------------------
        // initiate count and quality
        count = new int[numContestants][numContestants];
        quality =  new int[numContestants][numContestants];

        // prepare variables
        int val;
        ArrayList<Integer> contestantRanks;

        for (int indContestant = 0; indContestant < numContestants; indContestant++) {
            // get the current ranks for given contestant
            contestantRanks = ranks.get(indContestant);

            // update majority count and quality matrices
            for (int indJudge = 0; indJudge < numJudges - 1; indJudge++) {
                val = contestantRanks.get(indJudge);
                for (int indPlacement = val; indPlacement < numContestants; indPlacement++) {
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
                tmp1 = count[indContestant][numContestants - 1];
                logger.finest("tmp1 = " + tmp1);
                sortedIndex[tmp1 - 1] = indContestant;
            }
            catch (Exception e) {
                logger.info("count length = " + count.length);
                logger.info("sortedIndex length = " + sortedIndex.length);
                logger.severe("Exception caught = " + e);
            }
        }
    }


    /**
     * process on contestants that meet the majority
     */
    protected void processMajority() {
        // initialize variables
        int lastAssignedPlace = 1;
        int[] tieBreaks;
        ArrayList<Integer> activeList;

        for (int indP = 0; indP < numContestants - 1; indP++) {

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
                for (int indC = indP + 1; indC < numContestants; indC++)
                    // set counts past this to -1 so it is not looked at
                    // for the next iteration
                    count[activeList.get(0)][indC] = -1;

                // last column is the ranking
                count[activeList.get(0)][numContestants - 1] = lastAssignedPlace++;

            }
            else if (activeList.size() > 1) {
                // multiple passing, run tie break
                tieBreaks = rankContestantsTieBreak(activeList, indP);

                int cWinner;
                for (int ind0 = 0; ind0 < tieBreaks.length; ind0++) {
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

    protected int[] rankContestantsTieBreak(ArrayList<Integer> select, int place) {
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
                    logger.info("maxRank = " + maxRank);
                    logger.info("place = " + place);
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
                    logger.finer("Select list cleared...exiting rankContestantsTieBreak");
                    // Done!!
                    return out;
                }

                // continue procressing remaining list
                logger.finer("Continue processing on " + select.size() + " elements");
                tmp = rankContestantsTieBreak(select, place);
            }
            else {
                /*
                if (place == numContestants - 1) {
                    // last round... still tied.
                    for (int ind0 = 0; ind0 < select.size(); ind0++) {
                        out[completedOut] = select.get(ind0);
                        completedOut++;
                    }
                    return out;
                }
                else {
                    logger.fine("no elements removed...move to next placement for tiebreaker");
                    tmp = rankContestantsTieBreak(select, place+1);
                }
                */
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