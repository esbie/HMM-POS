import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class HMM {
    
    public static void main(String[] args){
        HMMParser p = new HMMParser("data/train.pos");
        p.parseTrainer();
        HMM hmm = new HMM(p);
        System.out.println("likelihood of 'NN' corresponding to 'agreement': "+ hmm.calcLikelihood("NN", "agreement"));
        System.out.println("prior probability of NN -> VBG: "+ hmm.calcPriorProb("NN", "VBG"));
        
        HMMParser p2 = new HMMParser("data/test.pos");
        hmm.viterbi(p2.wordSequence());
    }

    HashMap<String, Integer> tagCounts;
    HashMap<String, HashMap<String, Integer>> wordCounts;
    HashMap<String, HashMap<String, Integer>> tagBigramCounts; 
    HashMap<String, HashMap<String, Integer>> tagForWordCounts;
    HashMap<String, HashMap<String, Double>> goodTuringTagBigramCounts;
    HashMap<String, Double> goodTuringTagUnigramCounts;
    HashMap<Integer, Integer> numberOfBigramsWithCount;
    boolean goodTuringCountsAvailable = false;
    int numTrainingBigrams;
    String mostFreqTag;
    FileWriter writer;
    
    final boolean ADDONE = true;
    final boolean GOODTURING = false;
    
    public HMM(HMMParser p){
        this.tagCounts = p.tagCounts;
        this.wordCounts = p.wordCounts;
        this.tagBigramCounts = p.tagBigramCounts;
        this.tagForWordCounts = p.tagForWordCounts;
        this.mostFreqTag = p.mostFreqTag;
        
        this.goodTuringTagBigramCounts = new HashMap<String, HashMap<String, Double>>();
        this.goodTuringTagUnigramCounts = new HashMap<String, Double>();
        this.numberOfBigramsWithCount = new HashMap<Integer, Integer>();
        this.numTrainingBigrams = p.numTrainingBigrams;
        try {
            writer = new FileWriter(new File("data/output.pos"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    //returns map[key]
    private int counts(HashMap<String, Integer> map, String key){
        return (map.containsKey(key)) ? map.get(key) : 0;
    }
    
    //returns map[key1][key2]
    private int counts(HashMap<String, HashMap<String,Integer>> map, String key1, String key2){
        return (map.containsKey(key1))? counts(map.get(key1), key2) : 0;
    }
    
    //returns map[key]
    private double counts(HashMap<String, Double> map, String key){
        return (map.containsKey(key)) ? map.get(key) : 0.0;
    }
    
    //returns map[key1][key2]
    private double counts(HashMap<String, HashMap<String,Double>> map, String key1, String key2){
        return (map.containsKey(key1))? counts(map.get(key1), key2) : 0.0;
    }
    
    private int numberOfBigramsWithCount(int count){
        if (numberOfBigramsWithCount.containsKey(count)) {
            return numberOfBigramsWithCount.get(count);
        } else {
            return 0;
        }
    }
    
    private void makeGoodTuringCounts(){
        // Fill numberOfBigramsWithCount
        for (String tag1 : tagBigramCounts.keySet()) {
            HashMap<String, Integer> innerMap = tagBigramCounts.get(tag1);
            for (String tag2 : innerMap.keySet()) {
                int count = innerMap.get(tag2);
                if (numberOfBigramsWithCount.containsKey(count)) {
                    numberOfBigramsWithCount.put(count, 1+numberOfBigramsWithCount.get(count));
                } else {
                    numberOfBigramsWithCount.put(count, 1);
                }
            }
        }
        
        // Fill goodTuringTagBigramCounts
        for (String tag1 : tagBigramCounts.keySet()) {
            HashMap<String, Integer> innerMap = tagBigramCounts.get(tag1);
            HashMap<String, Double> innerGTMap = new HashMap<String, Double>();
            goodTuringTagBigramCounts.put(tag1, innerGTMap);
            
            double unigramCount = 0;
            for (String tag2 : innerMap.keySet()) {
                int count = innerMap.get(tag2);
                // c* = (c+1) * N(c+1) / N(c)
                double newCount = ((double)count+1.0)*((double)numberOfBigramsWithCount(count+1))/((double)numberOfBigramsWithCount(count));
                innerGTMap.put(tag2, newCount);
                unigramCount += newCount;
            }
            goodTuringTagUnigramCounts.put(tag1, unigramCount);
        }
        goodTuringCountsAvailable = true;
    }
    
    /*
     * Calculates P(word|tag)
     */
    public double calcLikelihood(String tag, String word){
        if(ADDONE){
            int vocabSize = tagForWordCounts.keySet().size();
            return (double) (counts(wordCounts,tag,word)+1) / (double) (counts(tagCounts,tag)+vocabSize);
        } else if(GOODTURING) {
            return (double) counts(wordCounts,tag,word) / (double) counts(tagCounts,tag);
        } else {
            return (double) counts(wordCounts,tag,word) / (double) counts(tagCounts,tag);
        }
    }
    
    /*
     * Calculates P(tag2|tag1) 
     */
    public double calcPriorProb(String tag1, String tag2){
        if(ADDONE) {
            int vocabSize = tagCounts.keySet().size();
            return (double) (counts(tagBigramCounts,tag1,tag2)+1) / (double) (counts(tagCounts,tag1)+vocabSize);
        } else if(GOODTURING) {
            if(!goodTuringCountsAvailable) {
                System.out.println("Making good turing counts...");
                makeGoodTuringCounts();
                System.out.println("Done making good turing counts.");
            }
            double gtcount = counts(goodTuringTagBigramCounts, tag1, tag2);
            // If this bigram has occurred, return good turing probability
            if (gtcount > 0.0) {
                return gtcount / counts(goodTuringTagUnigramCounts, tag1);
            }
            // Otherwise, return N1/N as per book (page 101)
            return numberOfBigramsWithCount(1) / (double)numTrainingBigrams;
        } else {
            return (double) counts(tagBigramCounts,tag1,tag2) / (double) counts(tagCounts,tag1);
        }
    }
    
    public void viterbi(ArrayList<String> words){
        //two-dimensional Viterbi Matrix
        boolean sentenceStart = true;
        HashMap<String, Node> prevMap = null;
        for(int i=0; i<words.size(); i++){
            if (i%500==0) {
                System.out.println("working on "+i+" of "+words.size()+" words");
            }
            String word = words.get(i);
            HashMap<String, Node> subMap = new HashMap<String,Node>();
            
            if(sentenceStart){
                Node n = new Node(word, "<s>", null, 1.0);
                subMap.put(word, n);
                sentenceStart = false;
            } else {
                //add all possible tags (given the current word)
                //to the Viterbi matrix                
                if(tagForWordCounts.containsKey(word)){
                    HashMap<String, Integer> tagcounts = tagForWordCounts.get(word);
                    for(String tag : tagcounts.keySet()){
                        subMap.put(tag, calcNode(word, tag, prevMap));               
                    }
                } else if (word.matches("[A-Z]\\w*")) {
                    subMap.put("NNP", calcNode(word, "NNP", prevMap));
                } else if (word.matches("\\p{Digit}*.\\p{Digit}*") || word.matches("(\\p{Punct}+|\\p{Digit}+)+")) {
                    subMap.put("CD", calcNode(word, "CD", prevMap));
                } else if (word.contains("-") || word.matches(".*able")) {
                    subMap.put("JJ", calcNode(word, "JJ", prevMap));
                } else if (word.matches(".*ing")) {
                    subMap.put("VBG", calcNode(word, "VBG", prevMap));
                } else if (word.matches(".*ly")) {
                    subMap.put("RB", calcNode(word, "RB", prevMap));
                } else if (word.matches(".*ed")) {
                    subMap.put("VBN", calcNode(word, "VBN", prevMap));
                } else if (word.matches(".*s")) {
                    subMap.put("NNS", calcNode(word, "NNS", prevMap));
                } else {
                    //never-before seen words we can't guess for
                    //subMap.put(mostFreqTag, calcNode(word, mostFreqTag, prevMap));
                    Node newNode = calcUnseenWordNode(word, prevMap);
                    subMap.put(newNode.tag, newNode);
                }
                
                if((i == words.size()-1) || words.get(i+1).equals("<s>")){
                    backtrace(subMap);
                    sentenceStart = true;
                }
            }
            prevMap = subMap;
        }
        try {
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* This method computes the probability that String tag
     * is the appropriate tag for String word,
     * given the probabilities before it (found in prevMap) 
     * 
     * probability = max ( previous Viterbi path probability *
     *                     transition probability *
     *                     state observation likelihood )
     *                     
     * more info in the book, pg. 148
     */
    private Node calcNode(String word, String tag, HashMap<String, Node> prevMap){
        Node n = new Node(word,tag);
        double maxProb = 0.0;
        for(String prevTag : prevMap.keySet()){
            Node prevNode = prevMap.get(prevTag);
            //this is the previous Viterbi path probability
            double prevProb = prevNode.prob;
            //this is the transition probability
            prevProb *= calcPriorProb(prevTag, tag);
            if(prevProb >= maxProb){
                maxProb = prevProb;
                n.parent = prevNode;
            }
        }
        //this is the state observation likelihood
        n.prob = maxProb * calcLikelihood(tag, word);
        return n;
    }
    
    private Node calcUnseenWordNode(String word, HashMap<String, Node> prevMap) {
        double maxProb = 0.0;
        String bestTag = "NOTAG";
        Node bestParent = null;
        for (String prevTag : prevMap.keySet()) {
            Node prevNode = prevMap.get(prevTag);
            // Previous Viterbi path probability
            double prevProb = prevNode.prob;
            
            // Find the best transition given the previous tag
            HashMap<String, Integer> possibleTagMap = tagBigramCounts.get(prevTag);
            int maxCount = 0;
            String nextTag = "NOTAG";
            for (String possibleTag : possibleTagMap.keySet()) {
                if (possibleTagMap.get(possibleTag)>maxCount) {
                    maxCount = possibleTagMap.get(possibleTag);
                    nextTag = possibleTag;
                }
            }
            
            // Transition probability
            prevProb *= calcPriorProb(prevTag, nextTag);
            
            // Check if we have the best tag
            if (prevProb >= maxProb) {
                maxProb = prevProb;
                bestTag = nextTag;
                bestParent = prevNode;
            }
        }
        // Return node with prob as state observation likelihood
        return new Node(word, bestTag, bestParent, maxProb*calcLikelihood(bestTag, word));
    }
    
    /*
     * prints out the linked list of the correctly tagged words
     */
    private void backtrace(HashMap<String, Node> map) {
        Node n = new Node("NOMAX", "NOMAX");
        for(String key : map.keySet()){
            Node currentNode = map.get(key);
            if(currentNode.prob >= n.prob){
                n = currentNode;
            }
        }
        
        Stack<Node> stack = new Stack<Node>();
        while(n != null){
            stack.push(n);
            n = n.parent;
        }

        while(!stack.isEmpty()){
            n = stack.pop();
            try {
                writer.write(n.tag + " " + n.word + "\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
