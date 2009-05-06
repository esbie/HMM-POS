
public class Node {
    public Node parent;
    public double prob;
    public String tag;
    public String word;
    
    public Node(String word, String tag, Node parent, double prob){
        this.word = word;
        this.parent = parent;
        this.tag = tag;
        this.prob = prob;
    }

    public Node(String word, String tag) {
        this(word, tag, null, 0.0);
    }
    
    
}
