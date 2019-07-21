package relative_placement;
import javax.swing.table.DefaultTableModel;
import relative_placement.Scorer;

public class Results extends DefaultTableModel {
    private int numRows = 0;
    private int numCols = 0;
    private Scorer scorer;

    public Results(){
        scorer = new Scorer();
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
        scorer.process();

        scorer.getSortedRank(this);
    }

    /**
     * Export the computed results
     * @param path Path of the output file.
     */
    public void export(String path){
        scorer.writeCSV(path, ",");
    }
}