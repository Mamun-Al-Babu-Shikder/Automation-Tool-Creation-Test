package com.mcubes;

import com.mcubes.model.Instruction;
import com.mcubes.model.LineShiftThreshold;
import org.sikuli.script.Key;
import org.sikuli.script.Screen;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutomateMachine {

    private static Logger log = Logger.getLogger(AutomateMachine.class.getName());

    private String commentRegex = "(\\s*//.*\\s*|\\s*#\\s*.*\\s*)?";
    private String variableDefRegex = "[a-zA-Z_]\\w*";
    private String findVariableRegex = "\\$\\{([a-zA-Z0-9_]+)\\}";
    private String numRegex = "[+-]?\\d*\\.?\\d+";

    private boolean buildSuccess;
    private String script;
    private Screen screen;
    private Pattern pattern;
    private Matcher matcher;
    private Map<String, String> tokens;
    private Map<String, String> keys;

    private List<Instruction> instructions;
    private Map<String, String> variables;
    private Stack<Integer> conditionalStatementThreshold;
    private Stack<Integer> loopStatementThreshold;
    private Stack<LineShiftThreshold> lineShiftThresholdStack;

    // for shifting line
    private boolean needToShift;
    private int shiftWhenLine;
    private int shiftToLine;


    // (\s*#.+)? for comment inside line
    private AutomateMachine() {

        this.screen = new Screen();
        this.variables = new HashMap<>();
        this.tokens = new HashMap<>();
        this.conditionalStatementThreshold = new Stack<>();
        this.loopStatementThreshold = new Stack<>();
        this.lineShiftThresholdStack = new Stack<>();

        /* value defining function */
        tokens.put("def_bool", "def_bool\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*(true|false)\\s*\\)" + commentRegex);
        tokens.put("def_num", "def_num\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*(" + numRegex + ")\\s*\\)" +  commentRegex);
        tokens.put("def_str", "def_str\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*(\".*\")\\s*\\)(\\s*#.+)?");

        /* mathematical function */
        tokens.put("sum", "sum\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*\\)" + commentRegex);
        tokens.put("sub", "sub\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*\\)" + commentRegex);
        tokens.put("mul", "mul\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*\\)" + commentRegex);
        tokens.put("div", "div\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*\\)" + commentRegex);
        tokens.put("mod", "mod\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*,\\s*([a-zA-Z_]\\w*|" + numRegex + ")\\s*\\)" + commentRegex);

        /* printing function */
        tokens.put("mgs", "mgs\\s*\\(\\s*\"(.*)\"\\s*\\)" + commentRegex);
        tokens.put("print", "print\\s*\\(\\s*\"(.*)\"\\s*\\)" + commentRegex);
        tokens.put("log_info", "log_info\\s*\\(\\s*\"(.*)\"\\s*\\)" + commentRegex);
        tokens.put("log_success", "log_success\\s*\\(\\s*\"(.*)\"\\s*\\)" + commentRegex);
        tokens.put("log_error", "log_error\\s*\\(\\s*\"(.*)\"\\s*\\)" + commentRegex);
        tokens.put("log_warn", "log_warn\\s*\\(\\s*\"(.*)\"\\s*\\)" + commentRegex);

        /* conditional function */
        tokens.put("if", "if\\s*\\(\\s*(.*)\\s*\\)\\s*:\\s*" + commentRegex);
        tokens.put("else_if", "else_if\\s*\\(\\s*(.*)\\s*\\)\\s*:\\s*" + commentRegex);
        tokens.put("else", "else\\s*:\\s*" + commentRegex);
        tokens.put("end_if", "end_if\\s*" + commentRegex);

        /* loop function */
        tokens.put("loop", "loop\\s*\\(\\s*(.*)\\s*\\)\\s*:\\s*" + commentRegex);
        tokens.put("end_loop", "end_loop\\s*" + commentRegex);

        /* event function */
        tokens.put("click", "click\\s*\\(\\s*\"(.*)\"\\s*\\)|click\\s*\\(\\s*\\)" + commentRegex);
        tokens.put("type", "type\\s*\\(\\s*\"(.*)\"\\s*\\)" + commentRegex);
        tokens.put("press", "press\\s*\\(\\s*(.*)\\s*\\)" + commentRegex);
        tokens.put("delay", "delay\\s*\\(\\s*(\\d{1,15})\\s*\\)" + commentRegex);
        keys = new HashMap<>();
        keys.put("ENTER", Key.ENTER);
    }

    public AutomateMachine(String script) {
        this();
        this.script = script;
    }

    public void build() {
        buildSuccess = true;
        instructions = new ArrayList<>();
        FileReader reader = null;
        try {
            reader = new FileReader(this.script);
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            int lineNum = 0;
            boolean skip = false;
            while ((line=br.readLine())!=null) {
                lineNum++;
                line = line.trim();
                if (line.matches("//|#.*") || (line.startsWith("/*") && line.endsWith("*/")) || line.length() == 0) {
                    continue;
                } else if (line.startsWith("/*")) {
                    skip = true;
                } else if (line.endsWith("*/") && skip) {
                    skip = false;
                    continue;
                } else if(line.endsWith("*/") && !skip) {
                    throw new Exception("[ERROR] invalid comment block at line " + lineNum);
                }

                if (skip) {
                    continue;
                }

                try {
                    String keyword = findKeyword(line);
                    if (keyword == null) {
                        throw new Exception();
                    } else {
                        matcher = Pattern.compile(tokens.get(keyword)).matcher(line);
                        if (matcher.matches()) {
                            createInstruction(lineNum, keyword, matcher);
                        } else {
                            throw new Exception();
                        }
                    }
                } catch (Exception e) {
                    buildSuccess = false;
                    System.err.println("[ERROR] Syntax error at line: " + lineNum);
                    e.printStackTrace();
                    break;
                }
            }

            // System.out.println(conditionalStatementThreshold);

            if (skip) {
                throw new Exception("[ERROR] invalid comment block at line " + lineNum);
            }

            if (!conditionalStatementThreshold.isEmpty()) {
                throw new Exception("[ERROR] Syntax error, at line: " + (lineNum + 1)
                        + " can't find the end of the conditional statement");
            }

            if (!loopStatementThreshold.isEmpty()) {
                throw new Exception("[ERROR] Syntax error, at line: " + (lineNum + 1)
                        + " can't find the end of the loop statement");
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            buildSuccess = false;
            //e.printStackTrace();
        }
    }


    private String findKeyword(String line) {
        Set<String> keywords = tokens.keySet();
        for (String keyword : keywords) {
            if (line.startsWith(keyword))
                return keyword;
        }
        return null;
    }

    private void createInstruction(int lineNum, String keyword, Matcher matcher) throws Exception {
        //System.out.println("Keyword: " + keyword);
        if (keyword.equals("def_bool") || keyword.equals("def_num") || keyword.equals("def_str")) {
            instructions.add(new Instruction(lineNum, keyword, matcher.group(1), matcher.group(2)));
            //System.out.println(matcher.group(1)+":="+matcher.group(2));
        } else if (keyword.equals("sum") || keyword.equals("sub") || keyword.equals("mul") | keyword.equals("div")
                || keyword.equals("mod")) {
            instructions.add(new Instruction(lineNum, keyword, matcher.group(1), matcher.group(2), matcher.group(3)));
            //System.out.println(matcher.group(1)+":="+matcher.group(2)+" op "+matcher.group(3));
        } else if (keyword.equals("mgs") || keyword.equals("print") || keyword.equals("log_info")
                || keyword.equals("log_success") || keyword.equals("log_error") || keyword.equals("log_warn")) {
            //System.out.println(matcher.group(1));
            instructions.add(new Instruction(lineNum, keyword, matcher.group(1)));
        } else if (keyword.equals("if")) {
            pushConditionalStatement(lineNum, keyword);
        } else if (keyword.equals("else_if")) {
            if (conditionalStatementThreshold.isEmpty() || instructions.get(conditionalStatementThreshold.peek()).getKeyword().equals("else")) {
                throw new Exception("[ERROR] Syntax error at line " + lineNum + ", 'if' statement not found");
            }
            pushConditionalStatement(lineNum, keyword);
        } else if (keyword.equals("else")) {
            if (conditionalStatementThreshold.isEmpty() || instructions.get(conditionalStatementThreshold.peek()).getKeyword().equals("else")) {
                throw new Exception("[ERROR] Syntax error at line " + lineNum + ", 'if' statement not found");
            }
            pushConditionalStatement(lineNum, keyword);
        } else if (keyword.equals("end_if")) {
            if (conditionalStatementThreshold.isEmpty()) {
                throw new Exception("[ERROR] Syntax error at line " + lineNum + ", 'if' statement not found");
            }
            int blockEnd = instructions.size();
            instructions.add(new Instruction(lineNum, keyword));
            int nextCheckPoint = blockEnd;
            while (!conditionalStatementThreshold.isEmpty()) {
                int index = conditionalStatementThreshold.pop();
                Instruction instruction = instructions.get(index);
                instruction.setConditionalBlockEnd(blockEnd);
                instruction.setNextCheckPoint(nextCheckPoint);
                nextCheckPoint = instruction.getConditionalBlockStart();
                if (instruction.getKeyword().equals("if")) {
                    break;
                }
            }
        } else if (keyword.equals("loop")) {
            pushLoopStatement(lineNum, keyword);
        } else if (keyword.equals("end_loop")) {
            if (loopStatementThreshold.isEmpty()) {
                throw new Exception("[ERROR] Syntax error at line " + lineNum + ", 'loop' statement not found");
            }
            int blockEnd = instructions.size();
            int index = loopStatementThreshold.pop();
            Instruction instruction = instructions.get(index);
            instruction.setConditionalBlockEnd(blockEnd);
            instructions.add(new Instruction(lineNum, keyword, null, instruction.getConditionalBlockStart(),
                    0, 0));
        }



        else if (keyword.equals("click") || keyword.equals("type") || keyword.equals("press")
                || keyword.equals("delay")) {
            instructions.add(new Instruction(lineNum, keyword, matcher.group(1)));
        }

        /*
        else if (keyword.equals("else_if")) {
            System.out.println("GRP: " + matcher.group());
        } else if (keyword.equals("else")) {
            System.out.println("GRP: " + matcher.group());
        } else if (keyword.equals("end_if")) {
            System.out.println("GRP: " + matcher.group());
        }
         */

        /*
        else if (keyword.equals("end_if")) {
            int blockStart = conditionalStatementThreshold.pop();
            Instruction instruction = instructions.get(blockStart);
            int blockEnd = instructions.size();
            instruction.setConditionalBlockEnd(blockEnd);
            instructions.add(new Instruction(lineNum, keyword, instruction.getCondition(), instruction.getConditionalBlockStart(), instruction.getConditionalBlockEnd()));
            System.out.println("STR: " + instruction);
        }

         */
    }

    private void pushConditionalStatement(int lineNum, String keyword) {
        int blockStart = instructions.size();
        conditionalStatementThreshold.push(blockStart);
        instructions.add(new Instruction(lineNum, keyword, matcher.group(1), blockStart, 0, 0));
    }

    private void pushLoopStatement(int lineNum, String keyword) {
        //System.out.println("GRP: " + matcher.group(1));
        int blockStart = instructions.size();
        loopStatementThreshold.push(blockStart);
        instructions.add(new Instruction(lineNum, keyword, matcher.group(1), blockStart, 0, 0));
    }

    public void run() {

        try {
            for (int i = 0; i < instructions.size(); i++) {

                if (!lineShiftThresholdStack.isEmpty() && i == lineShiftThresholdStack.peek().getShiftWhenLine()) {
                    i = lineShiftThresholdStack.pop().getShiftToLine();
                }

                Instruction ins = instructions.get(i);
                int lineNum = ins.getLine();
                String keyword = ins.getKeyword();

                if (keyword.equals("def_bool") || keyword.equals("def_num") || keyword.equals("def_str")) {
                    variables.put(ins.getParam()[0], ins.getParam()[1]);
                } else if (keyword.equals("sum") || keyword.equals("sub") || keyword.equals("mul") | keyword.equals("div")
                        || keyword.equals("mod")) {
                    double var1 = getDoubleValue(lineNum, ins.getParam()[1]);
                    double var2 = getDoubleValue(lineNum, ins.getParam()[2]);
                    double ans = 0;
                    switch (keyword) {
                        case "sum":
                            ans = var1 + var2;
                            break;
                        case "sub":
                            ans = var1 - var2;
                            break;
                        case "mul":
                            ans = var1 * var2;
                            break;
                        case "div":
                            if (var2 == 0){
                                throw new Exception("[MATH_ERROR] '/' by zero at line " + lineNum);
                            } else {
                                ans = var1 / var2;
                            }
                            break;
                        case "mod":
                            ans = var1 % var2;
                            break;
                    }
                    String dist = ins.getParam()[0];
                    if (!ins.getParam()[1].contains(".") && !ins.getParam()[2].contains(".") && !keyword.equals("div")){
                        variables.put(dist, String.valueOf((long)ans));
                    } else {
                        variables.put(dist, String.valueOf(ans));
                    }
                } else if (keyword.equals("mgs") || keyword.equals("print") || keyword.equals("log_info")
                        || keyword.equals("log_success") || keyword.equals("log_error") || keyword.equals("log_warn"))
                {
                    String message = ins.getParam()[0];
                    message = variableReplaceWithActualValue(lineNum, message);
                    if (message.startsWith("\"") && message.endsWith("\"")) {
                        message = message.substring(1, message.length() - 1);
                    }
                    if (keyword.equals("mgs") || keyword.equals("print")) {
                        System.out.println(message);
                    } else {
                        System.out.println("[" + keyword.toUpperCase() + "] " + message);
                    }
                } else if (keyword.equals("if") || keyword.equals("else_if")) {
                    String condition = ins.getCondition();
                    condition = variableReplaceWithActualValue(lineNum, condition);
                    try {
                        boolean value = evaluateCondition(condition);
                        if (value) {
                            lineShiftThresholdStack.push(new LineShiftThreshold(ins.getNextCheckPoint(),
                                    ins.getConditionalBlockEnd()));
                        } else {
                            i = ins.getNextCheckPoint() - 1;
                        }
                    } catch (ScriptException e) {
                        throw new Exception("[ERROR] Invalid conditional statement at line " + lineNum);
                    } catch (Exception e) {
                        throw new Exception("[ERROR] Condition must be true or false value at line " + lineNum);
                    }
                } else if (keyword.equals("else")) {

                } else if (keyword.equals("loop")) {
                    String condition = ins.getCondition();
                    condition = variableReplaceWithActualValue(lineNum, condition);
                    try {
                        boolean value = evaluateCondition(condition);
                        if (value) {
                            continue;
                        } else {
                            i = ins.getConditionalBlockEnd();
                        }
                    } catch (ScriptException e) {
                        throw new Exception("[ERROR] Invalid loop statement at line " + lineNum);
                    } catch (Exception e) {
                        throw new Exception("[ERROR] Condition must be true or false value at line " + lineNum);
                    }
                } else if (keyword.equals("end_loop")) {
                    i = ins.getConditionalBlockStart() - 1;
                }

                else if (keyword.equals("click")) {
                    click(lineNum, ins.getParam()[0]);
                } else if (keyword.equals("type")) {
                    type(lineNum, ins.getParam()[0]);
                } else if (keyword.equals("press")) {
                    press(lineNum, ins.getParam()[0]);
                } else if (keyword.equals("delay")) {
                    try {
                        Thread.sleep(Long.parseLong(ins.getParam()[0]));
                    }catch (Exception e) {
                        throw new Exception("[ERROR] Can't delay at line " + lineNum);
                    }
                }


            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    private String variableReplaceWithActualValue(int lineNum, String line) throws Exception {
        Matcher matcher = Pattern.compile(findVariableRegex).matcher(line);
        while (matcher.find()) {
            String variable = matcher.group(1);
            if (variables.containsKey(variable)) {
                String value = variables.get(variable);
                line = line.replace("${" + variable + "}", value);
            } else {
                throw new Exception("[ERROR] Variable '" + variable + "' not defined at line " + lineNum);
            }
        }
        return line;
    }

    private String removeDoubleQuotation (String value) {
        if (value != null && (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private boolean evaluateCondition(String condition) throws ScriptException {
        //System.out.println("# con: " + condition);
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        return (boolean) engine.eval(condition);
    }

    private double getDoubleValue(int lineNum, String variable) throws Exception {
        double num = 0;
        if (variable.matches(variableDefRegex)) {
            String value = variables.get(variable);
            if (value == null){
                throw new Exception("[ERROR] Variable '" + variable + "' not defined at line " + lineNum);
            } else {
                num = Double.parseDouble(value);
            }
        } else {
            num = Double.parseDouble(variable);
        }
        return num;
    }

    public void buildAndRun() {
        build();
        if (buildSuccess) {
            run();
        }
    }

    /*
    if (line.startsWith("def")) {
                    def(line);
                }else if (line.startsWith("click")) {
                    click(line);
                } else if (line.startsWith("double_click")) {
                    doubleClick(line);
                } else if (line.startsWith("type")) {
                    type(line);
                } else if (line.startsWith("press")) {
                    press(line);
                } else if (line.startsWith("delay")) {
                    delay(line);
                } else if (line.startsWith("start_loop")) {

                }
     */

    //public void start


    private boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    private void def(String line) {
        try {
            matcher = Pattern.compile(tokens.get("def")).matcher(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void click(int lineNum, String value) throws Exception {
        if (value == null) {
            screen.click();
            return;
        }
        value = variableReplaceWithActualValue(lineNum, value);
        value = removeDoubleQuotation(value);
        if (isFileExist(value)){
            screen.click(value);
        } else {
            throw new Exception("[ERROR] Image file '" + value + "' not found at line " + lineNum);
        }
    }

    private void type(int lineNum, String value) throws Exception {
        try {
            value = variableReplaceWithActualValue(lineNum, value);
            value = removeDoubleQuotation(value);
            screen.type(value);
        }catch (Exception e) {
            throw new Exception("[ERROR] Can't type value '"+value+"' at line " + lineNum);
        }
    }



    private void doubleClick(String line) {

    }

    private void press(int lineNum, String key) throws Exception {
        if (keys.containsKey(key)) {
            screen.type(keys.get(key));
        } else {
            throw new Exception("[ERROR] Invalid key or can't press key '' at line " + lineNum);
        }
    }

    private void delay(String line) {
        try {
            matcher = Pattern.compile(tokens.get("delay")).matcher(line);
            if (matcher.matches()) {
                String value = matcher.group(1);
                Thread.sleep(Long.parseLong(value));
            } else {
                System.out.println("Syntax error...");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
