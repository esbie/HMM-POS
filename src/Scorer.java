import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Scorer
{
    public static void main(String[] args)
    {
        int agreeCount = 0;
        int disagreeCount = 0;
        
        HashMap<String, HashMap<String, ArrayList<String>>> tagChosenForTag = new HashMap<String, HashMap<String, ArrayList<String>>>();
        
        File testFile = new File("data/test.pos");
        File outputFile = new File("data/output.pos");
        FileWriter outFile;
        try {
            outFile = new FileWriter(new File("scoring/score.html"));
            
            Scanner testScanner = new Scanner(testFile);
            Scanner outputScanner = new Scanner(outputFile);
            
            String testPOS;
            String outputPOS;
            int i = 0;
            while (testScanner.hasNext() && outputScanner.hasNext()) {
                i++;
                // Get the parts of speech
                testPOS = testScanner.next();
                outputPOS = outputScanner.next();
                
                // Get the associated words
                String testWord = testScanner.next();
                String outputWord = outputScanner.next();
                
                // Count agreement and disagreement
                if (testPOS.equals(outputPOS)) {
                    agreeCount++;
                } else {
                    disagreeCount++;
                }
                
                // Count which tag is mistaken for which
                if (!tagChosenForTag.containsKey(testPOS)) {
                    tagChosenForTag.put(testPOS, new HashMap<String, ArrayList<String>>());
                }
                if (!tagChosenForTag.get(testPOS).containsKey(outputPOS)) {
                    tagChosenForTag.get(testPOS).put(outputPOS, new ArrayList<String>());
                }
                tagChosenForTag.get(testPOS).get(outputPOS).add(testWord);
                //tagChosenForTag.get(testPOS).put(outputPOS, tagChosenForTag.get(testPOS).get(outputPOS)+1);
                
                if(!testWord.equals(outputWord)) {
                    System.out.println("line "+i+" ERROR: "+testWord+" does not match "+outputWord);
                }
            }
            
            // Start printing the scoring output
            outFile.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
            outFile.write("<html lang=\"en\">");
            outFile.write("<head>");
            outFile.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
            outFile.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
            outFile.write("<title>Part of Speech Scoring</title>");
            outFile.write("</head>");
            outFile.write("<body>");

            outFile.write("<div>Agree: " + agreeCount + "</div>");
            outFile.write("<div>Disagree: " + disagreeCount + "</div>");
            outFile.write("<div>Percentage Right: " + (100.0*(double)agreeCount/(double)(agreeCount+disagreeCount)) + "%</div>");

            outFile.write("<table rules='all' cellpadding='5'>");
            HMMParser p = new HMMParser("data/train.pos");
            p.parseTrainer();
            outFile.write("<tr><th scope='col'></th>");
            for (String tag : p.tagCounts.keySet()) {
                if (tag.equals("<s>")) {
                    outFile.write("<th scope='col'>&lt;s&gt;</th>");
                } else {
                    outFile.write("<th scope='col'>" + tag + "</th>");
                }
            }
            outFile.write("</tr>");
            for (String testTag : p.tagCounts.keySet()) {
                if (testTag.equals("<s>")) {
                    outFile.write("<tr>\n<th scope='row'>&lt;s&gt;</th>");
                } else {
                    outFile.write("<tr>\n<th scope='row'>" + testTag + "</th>");
                }
                for (String outputTag : p.tagCounts.keySet()) {
                    String htmlClass = " class='normal'";
                    if (tagChosenForTag.containsKey(testTag) && tagChosenForTag.get(testTag).containsKey(outputTag)) {
                        ArrayList<String> list = tagChosenForTag.get(testTag).get(outputTag);
                        int num = list.size();
                        if (testTag.equals(outputTag)) {
                            htmlClass = " class='self'";
                        } else if (num > 1000) {
                            htmlClass = " class='reallybad'";
                        } else if (num > 500) {
                            htmlClass = " class='bad'";
                        } else if (num > 100) {
                            htmlClass = " class='prettybad'";
                        }
                        outFile.write("<td"+htmlClass+" onclick='if(this.childNodes[1].style.display===\"none\"){this.childNodes[1].style.display=\"block\"}else{this.childNodes[1].style.display=\"none\"}'>"+num+"<div style='display:none'>");
                        for (String item : list) {
                            outFile.write(item + ", ");
                        }
                        outFile.write("</div></td>");
                    } else {
                        outFile.write("<td class='zero'>0</td>");
                    }
                }
                outFile.write("</tr>");
            }
            outFile.write("</table>");

            outFile.write("</body>");
            outFile.write("</html>");
            
            outFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}