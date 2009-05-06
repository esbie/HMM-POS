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
    
    public HMM(HMMParser p){
        this.tagCounts = p.tagCounts;
        this.wordCounts = p.wordCounts;
        this.tagBigramCounts = p.tagBigramCounts;
        this.tagForWordCounts = p.tagForWordCounts;
        this.mostFreqTag = p.mostFreqTag;
    }
    
    /*
     * Calculates P(word|tag)
     */
    public double calcLikelihood(String tag, String word){
        if(wordCounts.containsKey(tag)){
            HashMap<String, Integer> subMap = wordCounts.get(tag);
            if(subMap.containsKey(word) && tagCounts.containsKey(tag)){
                return  (double) subMap.get(word) / (double) tagCounts.get(tag);
            }
        }
        return 0.0;
    }
    
    /*
     * Calculates P(tag2|tag1) 
     */
    public double calcPriorProb(String tag1, String tag2){
        if(tagBigramCounts.containsKey(tag1)){
            HashMap<String, Integer> subMap = tagBigramCounts.get(tag1);
            if(subMap.containsKey(tag2) && tagCounts.containsKey(tag1)){
                return (double) subMap.get(tag2) / (double) tagCounts.get(tag1);
            }
        }
        return 0.0;
    }
    
    public void viterbi(ArrayList<String> words){
        //two-dimensional Viterbi Matrix
        ArrayList<HashMap<String, Node>> list = new ArrayList<HashMap<String, Node>>();
        
        for(int i=0; i<words.size(); i++){
            String word = words.get(i);
            HashMap<String, Node> subMap = new HashMap<String,Node>();
            list.add(i, subMap);
            
            if(word.equals("<s>")){
                Node n = new Node(word, "<s>", null, 1.0);
                subMap.put(word, n);
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
                
                //QUIT!
                if(word.equals(".")){
                    backtrace(subMap.get("."));
                    //TODO for testing
                    //break;                    
                }
            }
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
    
    private void backtrace(Node n) {
        Stack<Node> stack = new Stack<Node>();
        while(n != null){
            stack.push(n);
            n = n.parent;
        }
        
        while(!stack.isEmpty()){
            n = stack.pop();
            System.out.println(n.tag + " " + n.word);
        }
    }
}
