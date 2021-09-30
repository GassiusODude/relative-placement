package relative_placement;
import relative_placement.ScorerAvgRaw;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
public class ScorerAvgRaw extends Scorer {
    private double[] avgScores;
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
                logger.finer(indC + ", " + indJ + ",\t" + rawScoreFloat[indC][indJ]);
                averageScores[indC] += rawScoreFloat[indC][indJ] / (numJudges - 1);
            }
        }
        logger.info("Finished calculating average scores");
        return averageScores;
    }

    @Override
    public void rankContestants(){
        // sort list and give rankings
        avgScores = getAverageScores();

        logger.info("Update sortedIndex");
        sortedIndex = Scorer.argSort(avgScores);
    }

    @Override
    public void loadCSV(String path, boolean firstRowHeader, String token) {
        loadCSVRaw(path, firstRowHeader, token);
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
            writer.write("Average Raw");

            // -------------------  write each row  ------------------------
            int tmpC;
            for (int indC = 0; indC < numContestants; indC++) {
                tmpC = sortedIndex[indC];
                writer.write("\n" + (indC + 1) + token);

                writer.write(leaders.get(tmpC) + token +
                    followers.get(tmpC) + token);

                // write ranks
                for (int indJ = 0; indJ < numJudges; indJ++)
                    writer.write(rawScoreFloat[tmpC][indJ] + token);

                writer.write(avgScores[tmpC] + token);

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
        columnNames.add("Average Raw");

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
                cData.add(Float.toString(rawScoreFloat[tmpC][indJ]));
            }


            // add average score
            cData.add(Double.toString(avgScores[tmpC]));

            data.addRow(cData);
        }
    }
}