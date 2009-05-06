
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class HMMParser {
    
    public static void main(String[] args){
        HMMParser p = new HMMParser("data/test.pos");
        p.parseTrainer();        
    }
    
    Scanner scanner;
    
    HashMap<String, Integer> tagCounts = new HashMap<String, Integer>();
    HashMap<String, HashMap<String, Integer>> wordCounts = new HashMap<String, HashMap<String,Integer>>();
    HashMap<String, HashMap<String, Integer>> tagBigramCounts = new HashMap<String, HashMap<String,Integer>>();
    HashMap<String, HashMap<String, Integer>> tagForWordCounts = new HashMap<String, HashMap<String, Integer>>();
    String mostFreqTag = "";
    int mostFreqTagCount = 0;
    
    
    public HMMParser(String filename){
        File file = new File(filename);
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void parseTrainer(){
        String prevTag = scanner.next();
        scanner.next();
       
        while(scanner.hasNext()){
            String currentTag = scanner.next();
            String currentWord = scanner.next();
            
            addOne(tagCounts, currentTag);
            addOne(wordCounts, currentTag, currentWord);
            addOne(tagBigramCounts, prevTag, currentTag);
            addOne(tagForWordCounts, currentWord, currentTag);
            
            if(tagCounts.get(currentTag) >= mostFreqTagCount){
                mostFreqTagCount = tagCounts.get(currentTag);
                mostFreqTag = currentTag;
            }
            
            prevTag = currentTag;
        }
    }
    
    public ArrayList<String> wordSequence(){
        ArrayList<String> list = new ArrayList<String>();
        while (scanner.hasNext()){
            scanner.next();
            list.add(scanner.next());
        }
        return list;
    }
    
    private void addOne(HashMap<String, Integer> map, String key1){
        if(map.containsKey(key1)){
            map.put(key1, map.get(key1)+1);
        } else{
            map.put(key1, 1);
        }       
    }   
    
    private void addOne(HashMap<String, HashMap<String, Integer>> map, String key1, String key2){
        if(map.containsKey(key1)){
            addOne(map.get(key1),key2);
        } else {
            HashMap<String, Integer> subMap = new HashMap<String, Integer>();
            subMap.put(key2, 1);
            map.put(key1, subMap);
        }
    }    
}
