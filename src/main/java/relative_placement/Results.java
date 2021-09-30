/**
 * Results for Relative Placement.
 *
 * @author Gassius ODude
 * @since July 22, 2019
 */
package relative_placement;
import javax.swing.table.DefaultTableModel;
import relative_placement.Scorer;
import relative_placement.ScorerRelPlacement;

public class Results extends DefaultTableModel {
    private Scorer scorer;

    /**
     * Constructor of the Results
     */
    public Results(){
        scorer = new ScorerRelPlacement();
    }

    /**
     * Constructor of the Results
     * @param newScorer The scorer
     */
    public Results(Scorer newScorer){
        scorer = newScorer;
    }

    public void setScorer(Scorer newScorer) {
        scorer = newScorer;
    }

    public int getNumJudges() {
        return scorer.getNumJudges();
    }

    /**
     * Load CSV file, use default comma (",") token
     * @param path Path to the CSV file
     */
    public void load(String path){
        load(path, ",");
    }

    /**
     * Load CSV file
     * @param path Path to the CSV file
     * @param token Token to use for the CSV file.
     */
    public void load(String path, String token){
        scorer.loadCSV(path, true, token);

        scorer.rankContestants();


        scorer.getSortedRank(this);
    }

    /**
     * Export the computed results, using default comma token
     * @param path Path of the output file.
     */
    public void export(String path){
       export(path, ",");
    }

    /**
     * Export the computed results
     * @param path Path of the output file.
     * @param token Token used to separate entries.
     */
    public void export(String path, String token){
        scorer.writeCSV(path, token);
    }
}