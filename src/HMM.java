import java.util.HashMap;

public class HMM {
    
    public static void main(String[] args){
        HMMParser p = new HMMParser("data/test.pos");
        p.parseTrainer();
        HMM hmm = new HMM(p.tagCounts, p.wordCounts, p.tagBigramCounts);
        System.out.println("likelihood of 'NN' corresponding to 'agreement': "+ hmm.calcLikelihood("NN", "agreement"));
        System.out.println("prior probability of NN -> VBG: "+ hmm.calcPriorProb("NN", "VBG"));
    }

    HashMap<String, Integer> tagCounts;
    HashMap<String, HashMap<String, Integer>> wordCounts;
    HashMap<String, HashMap<String, Integer>> tagBigramCounts; 
    
    public HMM(HashMap<String, Integer> tagCounts,
            HashMap<String, HashMap<String, Integer>> wordCounts,
            HashMap<String, HashMap<String, Integer>> tagBigramCounts){
        this.tagCounts = tagCounts;
        this.wordCounts = wordCounts;
        this.tagBigramCounts = tagBigramCounts;
    }
    
    public double calcLikelihood(String tag, String word){
        HashMap<String, Integer> subMap = wordCounts.get(tag);
        return  (double) subMap.get(word) / (double) tagCounts.get(tag);
    }
    
    public double calcPriorProb(String tag1, String tag2){
        HashMap<String, Integer> subMap = tagBigramCounts.get(tag1);
        return (double) subMap.get(tag2) / (double) tagCounts.get(tag1);        
    }
    
}
