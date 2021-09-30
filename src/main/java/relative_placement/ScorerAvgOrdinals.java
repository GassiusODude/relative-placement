package relative_placement;
import relative_placement.ScorerAvgRaw;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
public class ScorerAvgOrdinals extends Scorer {
    private double[] avgOrd;
    /**
     * Calculate on the rawScoreFloat that is populated through
     * Scorer.loadCSVRaw
     * @return
     */
    public double[] getAverageScores() {
        double[] averageScores = new double[numContestants];

        for (int indC=0; indC<numContestants; indC++) {
            // sum up to judges - 1 (exclude chief judge)
            for (int indJ=0; indJ<numJudges - 1; indJ++) {
                logger.finer(indC + ", " + indJ + ",\t" + ranksInt[indC][indJ]);
                averageScores[indC] += ranksInt[indC][indJ] * 1.0 / (numJudges - 1);
            }
        }
        logger.info("Finished calculating average scores");
        return averageScores;
    }

    @Override
    public void rankContestants(){
        // sort list and give rankings
        avgOrd = getAverageScores();

        logger.info("Update sortedIndex");

        int[] tmp = Scorer.argSort(avgOrd);
        sortedIndex = new int[tmp.length];
        for (int ind=0; ind < tmp.length; ind++) {
            sortedIndex[ind] = tmp[tmp.length - 1 - ind];
        }
    }

    @Override
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
        try {
            File f = new File(path);
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            // ------------------  write header  ----------------------------

            writer.write("Placement" + token);
            writer.write("Leader" + token + "Follower" + token);
            for (String judge : judges)
                writer.write(judge + token);
            writer.write("Average Ordinal");

            // -------------------  write each row  ------------------------
            int tmpC;
            for (int indC = 0; indC < numContestants; indC++) {
                tmpC = sortedIndex[indC];
                writer.write("\n" + (indC + 1) + token);

                writer.write(leaders.get(tmpC) + token +
                    followers.get(tmpC) + token);

                // write ranks
                for (int indJ = 0; indJ < numJudges; indJ++)
                    writer.write(ranksInt[tmpC][indJ] + token);

                writer.write(avgOrd[tmpC] + token);

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
        logger.info("Entering getSortedRank");
        // clear old data
        data.setRowCount(0);
        data.setColumnCount(0);

        // -------------------  setup column names  -------------------------
        Vector columnNames = new Vector();
        columnNames.add("Placement");
        columnNames.add("Leaders");
        columnNames.add("Followers");
        for (String judge: judges) {
            columnNames.add(judge);
        }
        columnNames.add("Average Ordinal");

        data.setColumnIdentifiers(columnNames);
        logger.info("Column headers updated");

        // ---------------------  setup data  -------------------------------
        int tmpC;
        Vector cData;
        for (int indC = 0; indC < numContestants; indC++) {
            logger.info("Writing data row " + indC);
            cData = new Vector();

            // update placement
            tmpC = sortedIndex[indC];
            cData.add(Integer.toString(indC + 1));

            // update leaders and followers
            cData.add(leaders.get(tmpC));
            cData.add(followers.get(tmpC));

            // add judges ranks
            for (int indJ = 0; indJ < numJudges; indJ++) {
                cData.add(Integer.toString(ranksInt[tmpC][indJ]));
            }


            // add average score
            cData.add(Double.toString(avgOrd[tmpC]));

            data.addRow(cData);
        }
    }
}