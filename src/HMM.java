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
    String mostFreqTag;
    FileWriter writer;
    
    final boolean ADDONE = true;
    
    public HMM(HMMParser p){
        this.tagCounts = p.tagCounts;
        this.wordCounts = p.wordCounts;
        this.tagBigramCounts = p.tagBigramCounts;
        this.tagForWordCounts = p.tagForWordCounts;
        this.mostFreqTag = p.mostFreqTag;
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
    
    /*
     * Calculates P(word|tag)
     */
    public double calcLikelihood(String tag, String word){
        if(ADDONE){
            int vocabSize = tagForWordCounts.keySet().size();
            return (double) (counts(wordCounts,tag,word)+1) / (double) (counts(tagCounts,tag)+vocabSize);
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
        } else {
            return (double) counts(tagBigramCounts,tag1,tag2) / (double) counts(tagCounts,tag1);
        }
    }
    
    public void viterbi(ArrayList<String> words){
        //two-dimensional Viterbi Matrix
        ArrayList<HashMap<String, Node>> list = new ArrayList<HashMap<String, Node>>();
        boolean sentenceStart = true;
        for(int i=0; i<words.size(); i++){
            System.out.println("working on "+i+" of "+words.size()+" words");
            String word = words.get(i);
            HashMap<String, Node> subMap = new HashMap<String,Node>();
            list.add(i, subMap);
            
            if(sentenceStart){
                Node n = new Node(word, "<s>", null, 1.0);
                subMap.put(word, n);
                sentenceStart = false;
            } else {
                HashMap<String, Node> prevMap = list.get(i-1);
                //add all possible tags (given the current word)
                //to the Viterbi matrix                
                if(tagForWordCounts.containsKey(word)){
                    HashMap<String, Integer> tagcounts = tagForWordCounts.get(word);
                    for(String tag : tagcounts.keySet()){
                        subMap.put(tag, calcNode(word, tag, prevMap));               
                    }
                } else {
                    //never-before seen words
                    subMap.put(mostFreqTag, calcNode(word, mostFreqTag, prevMap));
                }
                
                if((i == words.size()-1) || words.get(i+1).equals("<s>")){
                    backtrace(subMap);
                    sentenceStart = true;
                }
            }
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
