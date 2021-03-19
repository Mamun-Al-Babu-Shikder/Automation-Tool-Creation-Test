package com.mcubes;


import com.mcubes.model.Instruction;
import org.sikuli.script.Key;
import org.sikuli.script.Screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutomateMachine {

    private static Logger log = Logger.getLogger(AutomateMachine.class.getName());

    private String commentRegex = "(\\s*#.+)?";
    private String variableRegex = "[a-zA-Z_]\\w*";
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

    // (\s*#.+)? for comment inside line
    private AutomateMachine() {

        screen = new Screen();
        variables = new HashMap<>();
        tokens = new HashMap<>();

        /* value defining function */
        tokens.put("def_bool", "def_bool\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*(true|false)\\s*\\)" + commentRegex);
        tokens.put("def_num", "def_num\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*(" + numRegex + ")\\s*\\)" +  commentRegex);
        tokens.put("def_str", "def_str\\s*\\(\\s*([a-zA-Z_]\\w*)\\s*,\\s*\"(.*)\"\\s*\\)(\\s*#.+)?");

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
        tokens.put("if", "");
        tokens.put("end_if", "");

        tokens.put("click", "click\\s*\\(\\s*\"(.*)\"\\s*\\)|click\\s*\\(\\s*\\)" + commentRegex);
        tokens.put("type", "type\\s*\\(\\s*\"(.*)\"\\s*\\)" + commentRegex);
        tokens.put("press", "press\\s*\\(\\s*(.*)\\s*\\)" + commentRegex);
        tokens.put("delay", "delay\\s*\\(\\s*(\\d+)\\s*\\)" + commentRegex);
        tokens.put("start_loop", "start_loop\\s*\\(\\s*\\d{1,8}\\s*,\\s*\\d{1,8}\\s*,\\s*\\d{1,6}\\s*\\):" + commentRegex);

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
            while ((line=br.readLine())!=null) {
                lineNum++;
                line = line.trim();
                if (line.matches("#.*") || line.length() == 0) {
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
                    System.err.println("[ERROR] Syntax error at line: " + lineNum);
                    buildSuccess = false;
                    break;
                }
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

    private void createInstruction(int lineNum, String keyword, Matcher matcher) {
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
        }
    }

    public void run() {


        try {
            for (Instruction ins : instructions) {
                //System.out.println(ins);
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
                    /*
                    String var3 = String.valueOf(ans);
                    if (String.valueOf(ans).endsWith(".0")) {}
                     */
                    variables.put(dist, String.valueOf(ans));
                    System.out.println("dist: " + dist +", var1: "+var1+", var2: "+var2 + ",  ANS: " + ans);

                } else if (keyword.equals("mgs") || keyword.equals("print") || keyword.equals("log_info")
                        || keyword.equals("log_success") || keyword.equals("log_error") || keyword.equals("log_warn")) {
                    String message = ins.getParam()[0];
                    matcher = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)\\}").matcher(message);
                    while (matcher.find()) {
                        String variable = matcher.group(1);
                        if (variables.containsKey(variable)) {
                            message = message.replace("${" + variable + "}", variables.get(variable));
                        } else {
                            throw new Exception("[ERROR] Variable '" + variable + "' not defined at line " + lineNum);
                        }
                    }
                    if (keyword.equals("mgs") || keyword.equals("print")) {
                        System.out.println(message);
                    } else {
                        System.out.println("[" + keyword.toUpperCase() + "] " + message);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }


    }


    private double getDoubleValue(int lineNum, String variable) throws Exception {
        double num = 0;
        if (variable.matches(variableRegex)) {
            String value = variables.get(variable);
            if (value == null){
                throw new Exception("[ERROR] Variable '" + variable + "' not defined at line " + lineNum);
            } else {
                num = Double.parseDouble(value);
                System.out.println(num);
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
        System.out.println(file.exists());
        return file.exists();
    }

    private void def(String line) {
        try {
            matcher = Pattern.compile(tokens.get("def")).matcher(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void click(String line) {
        try {
            matcher = Pattern.compile(tokens.get("click")).matcher(line);
            if (matcher.matches()) {
                String value = matcher.group(1);
                if (value == null)
                    screen.click();
                else if (value != null && isFileExist(value)) {
                    screen.click(value);
                }else {
                    System.err.printf("Image file not found at '%s'\n", value);
                }
            } else {
                System.out.println("Syntax error...");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void doubleClick(String line) {

    }

    private void type(String line) {
        try {
            matcher = Pattern.compile(tokens.get("type")).matcher(line);
            if (matcher.matches()) {
               String value = matcher.group(1);
               screen.type(value);
            } else {
                System.out.println("Syntax error...");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void press(String line) {
        try {
            matcher = Pattern.compile(tokens.get("press")).matcher(line);
            if (matcher.matches()) {
                String value = matcher.group(1);
                if (keys.containsKey(value))
                    screen.type(keys.get(value));
                else
                    System.out.println("Press option not found!");
            } else {
                System.out.println("Syntax error...");
            }
        }catch (Exception e) {
            e.printStackTrace();
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
