import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import relative_placement.Scorer;

public class TestScore{
        
    @Test
    public void testScore1(){
        Scorer scorer = new Scorer();
        String s = getClass().getResource("rel_placement.csv").getFile();
        System.out.println("s = " + s);
        scorer.loadCSV(s, true, ",");
        scorer.process();
        int[] results = scorer.getSorted();
        int[] expected = {11, 8, 5, 1, 6, 10, 3, 9, 2, 7, 12, 4};


        //assertArrayEquals("Check 1", expected, results);
        for (int ind0 = 0; ind0 < results.length; ind0++){
            assertEquals("Match", ind0+1, expected[results[ind0]]);
        }
    }

    @Test
    public void testScore2(){
        Scorer scorer = new Scorer();
        String s = getClass().getResource("rel_placement2.csv").getFile();
        System.out.println("s = " + s);
        scorer.loadCSV(s, true, ",");
        scorer.process();
        int[] results = scorer.getSorted();
        int[] expected = {3,1,2,5,4,7,6};


        //assertArrayEquals("Check 1", expected, results);
        for (int ind0 = 0; ind0 < results.length; ind0++){
            assertEquals("Match", ind0+1, expected[results[ind0]]);
        }
    }
}
