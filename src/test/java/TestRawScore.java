import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import relative_placement.Scorer;
import relative_placement.ScorerAvgRaw;
public class TestRawScore {
    @Test
    public void testAverageFunction() {
        String s = getClass().getResource("raw_score.csv").getFile();
        ScorerAvgRaw scorer = new ScorerAvgRaw();
        scorer.loadCSVRaw(s, true, ",");
        scorer.display();
        double[] avg = scorer.getAverageScores();
        double[] exp = {9.05, 8.99, 8.74, 8.60, 8.59, 8.59, 8.49, 8.60, 8.43, 8.38, 8.31, 8.18};
        for (int ind0=0; ind0<exp.length; ind0++) {
            System.out.println("Expected[" + ind0 + "] = " + exp[ind0] + ",\tactual = " + avg[ind0]);
            assertEquals(exp[ind0], avg[ind0], 5e-2);
        }
    }

    @Test
    public void testArgSort() {
        double[] exp = {9.05, 8.99, 8.74, 8.61, 8.59, 8.591, 8.49, 8.60, 8.43, 8.38, 8.31, 8.18};
        int[] ranking = Scorer.argSort(exp);
        int[] expRanks = {1,2,3,4,8,6,5,7,9,10,11,12};
        for (int ind=0; ind<expRanks.length; ind++) {
            assertEquals(expRanks[ind], ranking[ind] + 1);
        }
    }

    @Test
    public void testAvgRawScore() {
        String s = getClass().getResource("raw_score.csv").getFile();
        ScorerAvgRaw scorer = new ScorerAvgRaw();
        scorer.loadCSVRaw(s, true, ",");
        scorer.rankContestants();
        int[] results = scorer.getSorted();
        int[] expRanks = {1,2,3,4,8,6,5,7,9,10,11,12};
        //assertArrayEquals("Check 1", expected, results);
        for (int ind0 = 0; ind0 < results.length; ind0++){
            assertEquals(expRanks[ind0], results[ind0] + 1);
        }

    }
}