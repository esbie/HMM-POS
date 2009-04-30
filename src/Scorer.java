import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;

public class Scorer
{
    public static void main(String[] args)
    {
        int agreeCount = 0;
        int disagreeCount = 0;
        
        HashMap<String, HashMap<String, Integer>> tagChosenForTag = new HashMap<String, HashMap<String, Integer>>();
        
        File testFile = new File("data/test.pos");
        File outputFile = new File("data/output.pos");
        FileWriter outFile;
        try {
            outFile = new FileWriter(new File("scoring/score.html"));
            
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
                }
                
                // Count which tag is mistaken for which
                if (!tagChosenForTag.containsKey(testPOS)) {
                    tagChosenForTag.put(testPOS, new HashMap<String, Integer>());
                }
                if (!tagChosenForTag.get(testPOS).containsKey(outputPOS)) {
                    tagChosenForTag.get(testPOS).put(outputPOS, 0);
                }
                tagChosenForTag.get(testPOS).put(outputPOS, tagChosenForTag.get(testPOS).get(outputPOS)+1);
                
                // Get through the associated words
                if(!testScanner.next().equals(outputScanner.next())) {
                    System.out.println("ERROR: The words don't match!!");
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
                        int num = tagChosenForTag.get(testTag).get(outputTag);
                        if (testTag.equals(outputTag)) {
                            htmlClass = " class='self'";
                        } else if (num > 1000) {
                            htmlClass = " class='reallybad'";
                        } else if (num > 500) {
                            htmlClass = " class='bad'";
                        } else if (num > 100) {
                            htmlClass = " class='prettybad'";
                        }
                        outFile.write("<td"+htmlClass+">"+num+"</td>");
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