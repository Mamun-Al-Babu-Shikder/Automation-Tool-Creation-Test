package com.mcubes;


import org.sikuli.script.Match;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;




/*
#Project Effectiveness
project.effectiveness.title=Project Effectiveness
project.effectiveness.workspace.name={0} name
project.effectiveness.population.size=Population Size
project.effectiveness.recall.target=Recall Target
project.effectiveness.probability.target.recall.calculations=Probability of Target Recall Calculations
project.effectiveness.max.total.true.positive=Max Total True Positive (if exceeded. recall target missed)
project.effectiveness.max.false.negative=Max False Negative (if exceeded. recall target missed)
project.effectiveness.false.and.true.negative=False Negative + True Negative (Not Reviewed @ Elusion Sample)
project.effectiveness.max.elusion.rate=Max Elusion Rate (% of False Negative in Not Reviewed @ Elusion Sample
project.effectiveness.binomial.distribution.probability=Probability Recall Target Achieved (Binomial Distribution Probability)


cal.project.effectiveness.review.results=Review Results
cal.project.effectiveness.probability.target=Probability Target Achieved @ Elusion Sample
cal.project.effectiveness.reviewed.elusion.sample.creation=Reviewed @ Elusion Sample Creation
cal.project.effectiveness.found.elusion.sample.creation=Found @ Elusion Sample Creation
cal.project.effectiveness.reviewed.stopping.point=Reviewed @ Stopping Point
cal.project.effectiveness.found.stopping.point=Found @ Stopping Point
cal.project.effectiveness.qc.elusion=Elusion
cal.project.effectiveness.not.reviewed.elusion.sample.creation=Not Reviewed @ Elusion Sample Creation
cal.project.effectiveness.positive.doc.sample={0} Docs in sample
cal.project.effectiveness.positive.documents.unreviewed=How many {0} documents in UNREVIEWED @ Elusion Sample Creation?*
cal.project.effectiveness.positive.documents.total.population=How many {0} documents are there in total population?
cal.project.effectiveness.estimated.recall.elusion.creation=Estimated Recall @ Elusion Creation
cal.project.effectiveness.estimated.recall.stopping.point=Estimated Recall @ Stopping Point
cal.project.effectiveness.lower.recall.estimate.formula=Lower Recall Estimate = Found / (Found @ Elusion Sample Creation + Upper Bound Elusion
cal.project.effectiveness.recall.estimate.formula=Recall Estimate = Found / (Found @ Elusion Sample Creation + Point Elusion
cal.project.effectiveness.upper.recall.estimate.formula=Upper Recall Estimate = Found / (Found @ Elusion Sample Creation + Lower Bound Elusion
cal.project.effectiveness.found.qc.point=Found @ QC Point

sal.project.effectiveness.probability.target=Probability Target Achieved
sal.project.effectiveness.prediction.precision=Prediction Precision
sal.project.effectiveness.predicted.positive=Predicted {0}
sal.project.effectiveness.precision.sample.size=Precision Sample Size
sal.project.effectiveness.positive.docs.sample={0} Docs in sample
sal.project.effectiveness.positive.documents.prediction=How many {0} documents are in our prediction?*
sal.project.effectiveness.prediction.elusion=Prediction Elusion
sal.project.effectiveness.elusion.sample.size=Elusion Sample Size
sal.project.effectiveness.positive.documents.not.prediction=How many {0} documents are NOT in our prediction?*
sal.project.effectiveness.estimated.recall=Estimated Recall
sal.project.effectiveness.lower.recall.estimate.formula=Lower Recall Estimate=Lower Bound Precision / (Point Precision + Point Elusion)
sal.project.effectiveness.recall.estimate.formula=Recall Estimate=Point Precision / (Point Precision + Point Elusion)
sal.project.effectiveness.upper.recall.estimate.formula=Upper Recall Estimate=Upper Bound Precision / (Point Precision + Point Elusion
sal.project.effectiveness.estimated.true.positives.prediction=Estimated True Positives in our prediction
 */





public class Main {

    static List<String> sb = new  ArrayList();

    public static void main(String[] args) throws Exception {

        AutomateMachine machine = new AutomateMachine("files/script/test.txt");
        machine.buildAndRun();
        //machine.build();


        /*
        FileReader reader = new FileReader("files/script/loop_test.mas");
        BufferedReader br = new BufferedReader(reader);
        String l = "";

        boolean isLoop = false;

        while ((l= br.readLine())!=null) {
            l = l.trim();
            if (l.startsWith("def") || l.startsWith("mgs")) {

            } else if (l.startsWith("start_loop") && isLoop == false) {
                isLoop = true;
            } else {
                System.out.println("Nested loop not possible...");
            }

            if (isLoop) {
                sb.add(l);
                if (l.startsWith("end_loop")) {
                    isLoop = false;
                    analysis(sb);
                }
            }

        }

         */

    }

    private static void analysis(List<String> list) {
        System.out.println(sb);
        sb.clear();
    }



}


 class Test2 {
    public static void main(String[] args) throws ScriptException {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        String foo = "\"tr\"==\"tr\"";
        System.out.println(engine.eval(foo));
    }
}






 class LexicalAnalysis {

    /**
     * @param args
     */
    private String[] keywords = { "abstract", "boolean", "byte", "case",
            "catch", "char", "class", "continue", "default", "do", "double",
            "else", "extends", "final", "finally", "float", "for", "if",
            "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "super", "switch", "synchronized",
            "this", "throw", "throws", "transient", "try", "void", "volatile",
            "while", "false", "true", "null" };
    HashMap<String, ArrayList<Integer>> keywordsTable;

    HashMap<String, ArrayList<Integer>> otherWords = new HashMap<String, ArrayList<Integer>>();

    public LexicalAnalysis(String fileName){

        Scanner kb = null;
        int lineNumber = 0;

        try {
            kb = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        keywordsTable = new HashMap<String, ArrayList<Integer>>();
        for(int i = 0; i < 47; i++){
            keywordsTable.put(keywords[i], new ArrayList<Integer>());
        }

        while(kb.hasNextLine()){

            lineNumber++;

            String line = kb.nextLine();

            String[] lineparts = line.split("\\s+|\\.+|\\;+|\\(+|\\)+|\\\"+|\\:+|\\[+|\\]+");

            for(String x: lineparts){

                ArrayList<Integer> list = keywordsTable.get(x);
                if(list == null){
                    list = otherWords.get(x);
                    if(list == null){
                        ArrayList<Integer> temp = new ArrayList<Integer>();
                        temp.add(lineNumber);
                        otherWords.put(x,temp);
                    }else{
                        otherWords.remove(x);
                        ArrayList<Integer> temp = new ArrayList<Integer>();
                        temp.add(lineNumber);
                        otherWords.put(x, temp);
                    }
                }else{
                    keywordsTable.remove(x);
                    ArrayList<Integer> temp = new ArrayList<Integer>();
                    temp.add(lineNumber);
                    keywordsTable.put(x, temp);
                }
            }
        }
        System.out.println("Keywords:");
        printMap(keywordsTable);
        System.out.println();
        System.out.println("Other Words:");
        printMap(otherWords);

    }
    public static void printMap(Map<String, ArrayList<Integer>> mp) {
        Iterator<Map.Entry<String, ArrayList<Integer>>> it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ArrayList<Integer>> pairs = (Map.Entry<String, ArrayList<Integer>>)it.next();
            System.out.print(pairs.getKey() + " = ");
            printList(pairs.getValue());
            System.out.println();
            it.remove();
        }
    }
    public static void printList(List x){

        for(Object m : x){
            System.out.print(m + ", ");
        }

    }
    public static void main(String[] args) {
// TODO Auto-generated method stub
        new LexicalAnalysis("files/script/test.txt");
    }

}






