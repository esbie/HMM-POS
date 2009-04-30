import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Baseline
{
    public static void main(String[] args)
    {
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File("data/output.pos"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        HMMParser pTrain = new HMMParser("data/train.pos");
        pTrain.parseTrainer();
        
        HMMParser pTest = new HMMParser("data/test.pos");
        ArrayList<String> wordSequence = pTest.wordSequence();
        
        // Figure out most frequent tag overall
        int max = 0;
        String bestOverallTag = "NOTAG";
        for (String tag : pTrain.tagCounts.keySet()) {
            if (pTrain.tagCounts.get(tag)>max) {
                max = pTrain.tagCounts.get(tag);
                bestOverallTag = tag;
            }
        }
        
        for (String word : wordSequence) {
            String tag = bestOverallTag;
            
            if (pTrain.tagForWordCounts.containsKey(word)) {
                // Figure out tag
                int currentWordMax = 0;
                String bestTag = "NOTAG";
                for (String tagForWord : pTrain.tagForWordCounts.get(word).keySet()) {
                    if (pTrain.tagForWordCounts.get(word).get(tagForWord)>currentWordMax) {
                        currentWordMax = pTrain.tagForWordCounts.get(word).get(tagForWord);
                        bestTag = tagForWord;
                    }
                }
                tag = bestTag;
            } else {
                // System.out.println("Word not in training set: " + word);
                // Leave it as the bestOverallTag
            }
            
            try {
                writer.write(tag + " " + word + "\n");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        
        try {
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}