import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import org.junit.BeforeClass;
import relative_placement.Scorer;
import relative_placement.ScorerRelPlacement;
import java.util.logging.Level;
public class TestScore{
    @BeforeClass
    public static void beforeClass()
    {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }
    @Test
    public void testScore1(){
        Scorer scorer = new ScorerRelPlacement();
        scorer.setLogLevel(Level.FINE);
        String s = getClass().getResource("rel_placement.csv").getFile();
        System.out.println("s = " + s);
        scorer.loadCSV(s, true, ",");
        scorer.rankContestants();
        int[] results = scorer.getSorted();
        int[] expected = {11, 8, 5, 1, 6, 10, 3, 9, 2, 7, 12, 4};


        //assertArrayEquals("Check 1", expected, results);
        for (int ind0 = 0; ind0 < results.length; ind0++){
            assertEquals("Match", ind0+1, expected[results[ind0]]);
        }
    }

    @Test
    public void testScore2(){
        Scorer scorer = new ScorerRelPlacement();
        scorer.setLogLevel(Level.FINE);
        String s = getClass().getResource("rel_placement2.csv").getFile();
        System.out.println("s = " + s);
        scorer.loadCSV(s, true, ",");
        scorer.rankContestants();
        int[] results = scorer.getSorted();
        int[] expected = {3,1,2,5,4,7,6};


        //assertArrayEquals("Check 1", expected, results);
        for (int ind0 = 0; ind0 < results.length; ind0++){
            assertEquals("Match", ind0+1, expected[results[ind0]]);
        }
    }

    @Test
    public void testScore3(){
        Scorer scorer = new ScorerRelPlacement();
        scorer.setLogLevel(Level.FINE);
        String s = getClass().getResource("rel_placement3.csv").getFile();
        System.out.println("s = " + s);
        scorer.loadCSV(s, true, ",");
        scorer.rankContestants();
        int[] results = scorer.getSorted();
        int[] expected = {12,9,1,6,11,5,7,8,4,10,3,2};//{3,12,11,9,6,4,7,8,2,10,5,1};


        //assertArrayEquals("Check 1", expected, results);
        for (int ind0 = 0; ind0 < results.length; ind0++){
            assertEquals("Match", ind0+1, expected[results[ind0]]);
        }
    }

    @Test
    public void testScore4(){
        Scorer scorer = new ScorerRelPlacement();
        scorer.setLogLevel(Level.FINER);
        String s = getClass().getResource("rel_placement4.csv").getFile();
        System.out.println("s = " + s);
        scorer.loadCSV(s, true, ",");
        scorer.rankContestants();
        int[] results = scorer.getSorted();
        int[] expected = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25};


        //assertArrayEquals("Check 1", expected, results);
        for (int ind0 = 0; ind0 < results.length; ind0++){
            assertEquals("Match", ind0+1, expected[results[ind0]]);
        }
    }
}
