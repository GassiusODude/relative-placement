/**
 * Relative placement functionality
 *
 * @author Gassius ODude
 * @since July 22, 2019
 */
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
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.ConsoleHandler;
public abstract class Scorer {
    /** List of leader names */
    protected ArrayList<String> leaders = new ArrayList();

    /** List of follower names */
    protected ArrayList<String> followers = new ArrayList();

    /** List of judges */
    protected ArrayList<String> judges = new ArrayList();

    /** Matrix of ranks (num contestants x num judges). */
    protected ArrayList<ArrayList> ranks = new ArrayList<ArrayList>();

    protected int[][] ranksInt = new int[1][1];

    protected ArrayList<ArrayList> rawScore = new ArrayList<ArrayList>();
    protected float[][] rawScoreFloat = new float[1][1];

    /** Number of judges */
    protected int numJudges = 0;

    /** Number of contestants */
    protected int numContestants = 0;

    /** Number to constitute majority of judges */
    protected int majority = 3;

    /**
     * Typically the number of contestants but potentially a fixed number
     * less than number contestants.
     */
    protected int maxRank = 0;


    /**
     * The count of ranks passing a given placement.  This is set up
     * as [numContestants x numContestants + 1]
     */
    protected int[][] count;

    /**
     * The quality of the ranks.  This sums the ranks as a means for
     * breaking ties.
     */
    protected int[][] quality;

    /**
     * The sorted index of placements
     */
    protected int[] sortedIndex;

    /**
     * The logger object for the Scorer
     */
    protected Logger logger;

    /**
     * Scorer constructor
     */
    public Scorer() {
        logger = Logger.getLogger("Scorer");
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINER);
        logger.addHandler(handler);

        reset();
    }

    /**
     * Set the log level of the logger
     * @param newLevel The log level to use.
     */
    public void setLogLevel(Level newLevel) {
        logger.setLevel(newLevel);
    }

    /** Reset the internal variables */
    public void reset() {
        logger.info("Resetting the Scorer");
        leaders = new ArrayList();
        followers = new ArrayList();
        judges = new ArrayList();
        ranks = new ArrayList<ArrayList>();
        numJudges = 0;
        numContestants = 0;
        majority = 3;
        maxRank = 0;
    }

    /**
     * Get the number of contestants
     * @return Return the number of currently loaded judges.
     */
    public int getNumContestants() { return numContestants; }

    /**
     * Get the number of judges loaded in.
     * @return The number of judges
     */
    public int getNumJudges() { return numJudges; }

    /**
     * Get the sorted index list of the computed placement
     * @return The sorting based on computed placement of input list.
     */
    public int[] getSorted() { return sortedIndex; }

    /**
     * Display the current state of the Scorer
     */
    public void display() {
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
     * Ordinals are the rankings (int 1...N)
     * This updates the Scorer's ranks
     * @param path Path to the file
     * @param firstRowHeader Boolean describing if first row is header
     * @param token String token to separate values, typically commas
     */
    public void loadCSVOrdinal(String path, boolean firstRowHeader, String token) {
        File f = new File(path);
        BufferedReader br = null;

        // ----------------------  clear lists  -----------------------------
        reset();
        try {
            // open file and get buffered stream
            FileInputStream fis = new FileInputStream(f);
            br = new BufferedReader(new InputStreamReader(fis));

            // allocate string to read one line of the file
            // allocate array of string for parsed output
            String strLine;

            String[] elements;
            int numFeatures = 0;
            while ((strLine = br.readLine()) != null) {
                // parse the file looking for comma separation
                elements = strLine.split(token);

                if (numFeatures == 0) {
                    // first line should be [Leader, Follower, Judges]
                    if (elements.length < 6)
                        // leader, follow, min 3 judges + 1 head judge
                        throw new RuntimeException("Expecting 6 or more columns");

                    numFeatures = elements.length;
                    numJudges = elements.length - 2;

                    // subtract one to account for chief judge
                    // Floor( (numJudges - 1) / 2 ) + 1 supports > 50% even for even number judges
                    majority = (int) Math.floor((numJudges - 1.0)/ 2 + 1);
                    for (int ind0 = 2; ind0 < numJudges + 2; ind0++){
                        // track judges (odd) + 1 head judge
                        judges.add(elements[ind0]);
                    }
                    continue;
                }
                else {
                    if (elements.length != numFeatures){
                        throw new RuntimeException("Number of columns should match!");
                    }
                }

                leaders.add(elements[0]);
                followers.add(elements[1]);
                ArrayList currRank = new ArrayList<Integer>();
                for (int ind0 = 2; ind0 < numFeatures; ind0 ++) {
                    currRank.add(Integer.parseInt(elements[ind0]));
                }
                ranks.add(currRank);
                numContestants++;
            }
            // ---------  update ranksInt as a convenience variable  --------
            ranksInt = new int[numContestants][numJudges];
            for (int indC = 0; indC < numContestants; indC++) {
                for (int indJ = 0; indJ < numJudges; indJ++ ) {
                    ranksInt[indC][indJ] = Integer.parseInt(
                        ranks.get(indC).get(indJ).toString());
                }
            }
            validateOrdinals();
            sortedIndex = new int[numContestants];
            for (int indC = 0; indC < numContestants; indC++){
                sortedIndex[indC] = indC;
            }
        }
        catch(java.io.FileNotFoundException fnfe) {
            logger.warning("File not found");
        }
        catch(java.io.IOException ioe) {
            logger.warning("IO Exception " + ioe);
        }
        finally {
            try {
                if (br != null)
                    br.close();
            }
            catch(IOException ioe) {}
        }
    }

    /**
     * Load in judge's raw scores from a comma-separated variable file.
     * This updates the Scorer's rawScoreFloat
     * @param path Path to the file
     * @param firstRowHeader Boolean describing if first row is header
     * @param token String token to separate values, typically commas
     */
    public void loadCSVRaw(String path, boolean firstRowHeader, String token) {
        File f = new File(path);
        BufferedReader br = null;

        // ----------------------  clear lists  -----------------------------
        reset();
        try {
            // open file and get buffered stream
            FileInputStream fis = new FileInputStream(f);
            br = new BufferedReader(new InputStreamReader(fis));

            // allocate string to read one line of the file
            // allocate array of string for parsed output
            String strLine;

            String[] elements;
            int numFeatures = 0;
            while ((strLine = br.readLine()) != null) {
                // parse the file looking for comma separation
                elements = strLine.split(token);

                if (numFeatures == 0) {
                    // first line should be [Leader, Follower, Judges]
                    if (elements.length < 6)
                        // leader, follow, min 3 judges + 1 head judge
                        throw new RuntimeException("Expecting 6 or more columns");

                    numFeatures = elements.length;
                    numJudges = elements.length - 2;

                    // subtract one to account for chief judge
                    // Floor( (numJudges - 1) / 2 ) + 1 supports > 50% even for even number judges
                    majority = (int) Math.floor((numJudges - 1.0)/ 2 + 1);
                    for (int ind0 = 2; ind0 < numJudges + 2; ind0++){
                        // track judges (odd) + 1 head judge
                        judges.add(elements[ind0]);
                    }
                    continue;
                }
                else {
                    if (elements.length != numFeatures){
                        throw new RuntimeException("Number of columns should match!");
                    }
                }

                leaders.add(elements[0]);
                followers.add(elements[1]);
                ArrayList currRank = new ArrayList<Float>();
                for (int ind0 = 2; ind0 < numFeatures; ind0 ++) {
                    currRank.add(Float.parseFloat(elements[ind0]));
                }
                rawScore.add(currRank);
                numContestants++;
            }
            // ---------  update ranksInt as a convenience variable  --------
            rawScoreFloat = new float[numContestants][numJudges];
            for (int indC = 0; indC < numContestants; indC++) {
                for (int indJ = 0; indJ < numJudges; indJ++ ) {
                    rawScoreFloat[indC][indJ] = Float.parseFloat(
                        rawScore.get(indC).get(indJ).toString());
                }
            }

            sortedIndex = new int[numContestants];
            for (int indC = 0; indC < numContestants; indC++){
                sortedIndex[indC] = indC;
            }
        }
        catch(java.io.FileNotFoundException fnfe) {
            logger.warning("File not found");
        }
        catch(java.io.IOException ioe) {
            logger.warning("IO Exception " + ioe);
        }
        finally {
            try {
                if (br != null)
                    br.close();
            }
            catch(IOException ioe) {}
        }
    }

    /**
     * Validate the input ordinals
     *
     * Verify unique ranking per judge (but allow a max less than numContestants)
     */
    protected void validateOrdinals() {
        // verify that each judge ranks is unique 1 .. numContestants
        // in some cases, all ranks past a max are just labeled as that max < numContestants
        int[] numVals = new int[numContestants];
        int curr;
        int max = 0;
        for (int indJ = 0; indJ < numJudges-1; indJ++) {
            Arrays.fill(numVals, 0);
            for (int indC = 0; indC < numContestants; indC++) {
                curr = ranksInt[indC][indJ]-1;
                numVals[curr]++;
            }

            for (int indC = 0; indC < numContestants; indC++) {
                if (max == 0) {
                    // the first count that is above 1,
                    if (numVals[indC] > 1)
                        max = indC;
                    else if (numVals[indC] != 1)
                        throw new RuntimeException(judges.get(indJ) + " missing rank " + (indC+1));
                }
                else {
                    // max declared...the rest should be zeros.
                    if (indC > max) {
                        if (numVals[indC] != 0)
                            throw new RuntimeException(judges.get(indJ) + " has duplicate ranking" + (indC+1));
                    }
                    else if (indC < max) {
                        if(numVals[indC] == 0)
                            throw new RuntimeException(judges.get(indJ) + " missing rank " + (indC+1));
                        else if (numVals[indC] > 1)
                            throw new RuntimeException(judges.get(indJ) + " duplicate rank " + (indC+1));
                    }
                }
            }
        }
        // update the maxRank used
        if (max == 0) {
            maxRank = numContestants;
        }
        else {
            maxRank = max + 1;
        }
    }

    /**
     * Return the indices to a sorted list with descending order, Max value indexed first.
     * @param values List of values to sort
     * @return The indices of the sorted (descending order)
     */
    public static int[] argSort(double[] values) {
        int[] out = new int[values.length];
        ArrayList exclude = new ArrayList<Integer>();

        for (int ind=0; ind<values.length; ind++) {
            double max = Double.NEGATIVE_INFINITY;
            int maxInd = 0;
            for (int ind1 = 0; ind1 < values.length; ind1++) {
                // -----------------  skip if in exclude list  ---------------------
                boolean skip = false;
                for (int ind2=0; ind2 < exclude.size(); ind2++) {
                    if (ind1 == ((Integer)exclude.get(ind2)).intValue())
                        skip = true;
                }
                if (skip)
                    continue;

                // track next max
                if (max < values[ind1]) {
                    max = values[ind1];
                    maxInd = ind1;
                }
            }
            // update the output vector
            out[ind] = maxInd;
            exclude.add(Integer.valueOf(maxInd));
        }
        return out;
    }
    /**
     * Print the current results.
     */
    public void printState() {
        String tmpS;
        int tmpC;
        // -----------------------  display  --------------------------------
        for (int indC = 0; indC < numContestants; indC++) {
            tmpC = sortedIndex[indC];
            System.out.print(leaders.get(tmpC) + "\t");
            System.out.print(followers.get(tmpC) + "\t");

            for (int indP = 0; indP < numContestants; indP++) {
                if (count[tmpC][indP] == 0){
                    tmpS = "    ";
                }
                else if (count[tmpC][indP] < 0) {
                    tmpS = " -- ";
                }
                else {
                    tmpS = String.format("%3d,", count[tmpC][indP]);
                }
                System.out.printf(tmpS );
            }
            System.out.println();
        }
    }

    /**
     * Update a data model with the results
     *
     * The column names are Placement, Leader, Follower, Judges, Count
     * @param data The data model
     */
    public abstract void getSortedRank(DefaultTableModel data);
    /**
     * Write the output CSV file for Relative placement
     * @param path Path for the output file
     * @param token Token to use.
     */
    public abstract void writeCSV(String path, String token);


    /**
     * Calculate and update count and quality matrices.
     */
    public abstract void rankContestants();


    public abstract void loadCSV(String path, boolean firstRowHeader, String token);

}
