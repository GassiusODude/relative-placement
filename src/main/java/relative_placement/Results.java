package relative_placement;
import javax.swing.table.DefaultTableModel;
import relative_placement.Scorer;

public class Results extends DefaultTableModel {
    private int numRows = 0;
    private int numCols = 0;

    Scorer scorer;
    public Results(){
        scorer = new Scorer();
    }
    public void load(String path){
        load(path, ",");
    }
    public void load(String path, String token){
        scorer.loadCSV(path, true, token);
        scorer.process();

        scorer.getSortedRank(this);
    }
    public void export(String path){
        scorer.writeCSV(path, ",");
    }
}