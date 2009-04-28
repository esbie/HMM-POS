import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Scorer
{
    public static void main(String[] args)
    {
        int agreeCount = 0;
        int disagreeCount = 0;
        
        HashMap<String, HashMap<String, Integer>> tagMistakenForTag = new HashMap<String, HashMap<String, Integer>>();
        
        File testFile = new File("data/test.pos");
        File outputFile = new File("data/output.pos");
        try {
            Scanner testScanner = new Scanner(testFile);
            Scanner outputScanner = new Scanner(outputFile);
            
            String testPOS;
            String outputPOS;
            while (testScanner.hasNext() && outputScanner.hasNext()) {
                testPOS = testScanner.next();
                outputPOS = outputScanner.next();
                
                // Count agreement and disagreement
                if (testPOS.equals(outputPOS)) {
                    agreeCount++;
                } else {
                    disagreeCount++;
                    // Count which tag is mistaken for which
                    if (!tagMistakenForTag.containsKey(testPOS)) {
                        tagMistakenForTag.put(testPOS, new HashMap<String, Integer>());
                    }
                    if (!tagMistakenForTag.get(testPOS).containsKey(outputPOS)) {
                        tagMistakenForTag.get(testPOS).put(outputPOS, 0);
                    }
                    tagMistakenForTag.get(testPOS).put(outputPOS, tagMistakenForTag.get(testPOS).get(outputPOS)+1);
                }
                
                // Get through the associated words
                if(!testScanner.next().equals(outputScanner.next())) {
                    System.out.println("ERROR: The words don't match!!");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        System.out.println("Agree: " + agreeCount);
        System.out.println("Disagree: " + disagreeCount);
        System.out.println("Percentage Right: " + ((double)agreeCount/(double)(agreeCount+disagreeCount)));
        
        HMMParser p = new HMMParser("data/train.pos");
        p.parseTrainer();
        for (String tag : p.tagCounts.keySet()) {
            System.out.print("\t" + tag);
        }
        System.out.println();
        for (String testTag : p.tagCounts.keySet()) {
            System.out.print(testTag);
            for (String outputTag : p.tagCounts.keySet()) {
                if (tagMistakenForTag.containsKey(testTag) && tagMistakenForTag.get(testTag).containsKey(outputTag)) {
                    System.out.print("\t"+tagMistakenForTag.get(testTag).get(outputTag));
                } else {
                    System.out.print("\t0");
                }
            }
            System.out.println();
        }
    }
}