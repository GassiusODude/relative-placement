import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import relative_placement.Scorer;
import relative_placement.ScorerAvgOrdinals;

public class TestAvgOrdinal {
    @Test
    public void testAverageFunction() {
        String s = getClass().getResource("rel_placement.csv").getFile();
        ScorerAvgOrdinals scorer = new ScorerAvgOrdinals();
        scorer.loadCSV(s, true, ",");
        scorer.display();
        double[] avg = scorer.getAverageScores();
        double[] exp = {9.43, 6.86, 5.86, 2.29, 6.00, 8.57,4.43, 8.14, 2.57, 7.00, 10.71, 6.14};
        for (int ind0=0; ind0<exp.length; ind0++) {
            System.out.println("Expected[" + ind0 + "] = " + exp[ind0] + ",\tactual = " + avg[ind0]);
            assertEquals(exp[ind0], avg[ind0], 5e-2);
        }
    }

    @Test
    public void testAvgRawScore() {
        String s = getClass().getResource("rel_placement.csv").getFile();
        ScorerAvgOrdinals scorer = new ScorerAvgOrdinals();
        scorer.loadCSV(s, true, ",");
        scorer.rankContestants();
        int[] results = scorer.getSorted();

        int[] expRanks = {4, 9, 7,3,5,12,2,10,8, 6,1,11};
        //assertArrayEquals("Check 1", expected, results);
        for (int ind0 = 0; ind0 < results.length; ind0++){
            assertEquals(expRanks[ind0], results[ind0] + 1);
        }

    }
}