import java.io.File;
import java.io.PrintWriter;
import java.util.*;

class col {
    int r;
    int g;
    int b;

    public String toString() {
        return "(" + r + " " + g + " " + b + ")";
    }
}

class Token {
    String content = "";
    int function = 0;

    void match() {
        if (content.toLowerCase().equals("receive")) {
            function = 1;
        } else if (content.toLowerCase().equals("transmit")) {
            function = 2;
        } else if (content.toLowerCase().equals("logic")) {
            function = 3;
        } else if (content.toLowerCase().equals("force")) {
            function = 4;
        } else if (content.toLowerCase().equals("group")) {
            function = 5;
        } else if (content.toLowerCase().equals("nextline")) {
            function = 6;
        } else if (content.toLowerCase().equals("{")) {
            function = 900;
        } else if (content.toLowerCase().equals("}")) {
            function = 901;
        } else if (content.toLowerCase().equals("(")) {
            function = 905;
        } else if (content.toLowerCase().equals(")")) {
            function = 906;
        } else if (content.toLowerCase().equals("") || content.toLowerCase().equals(" ")) {
            function = 1000; // ignore
        } else {
            function = 0;
        }
    }
}

// This code is SO BADLY optimized but who cares since computers are fast - and
// who would care about compilation time on larger programs? Oh wait that's me..
// Still don't care!!
public class CodeRun {
    static boolean debug = true;
    static int lineNr = 1;
    static int colorNr = 0;

    static String[] splitString(String s) {
        return s.split("\\s+|(?=\\{)|(?=\\()|(?<=\\()|(?=\\))|(?<=\\))");

    }

    public static void main(String[] args) throws Exception {
        String directoryPath = System.getProperty("user.dir");
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        ArrayList<File> chooselist = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".wire")) {
                    // System.out.println(file.getName());
                    chooselist.add(file);
                }
            }
        }
        int i = 0;
        System.out.println("Choose one of these files: ");
        for (File f : chooselist) {
            System.out.println(i + ". " + f.getName());
            i++;
        }
        Scanner sc = new Scanner(System.in);
        int read = -1;
        while (read < 0 || read >= chooselist.size()) {
            // sc.nextLine();
            read = sc.nextInt();
        }
        Object[] chooseListArr = chooselist.toArray();
        File f = (File) chooseListArr[read];
        sc.close();
        Scanner code = new Scanner(f);
        File outf = new File(directory + "\\output.fwire");
        outf.createNewFile();
        PrintWriter pw = new PrintWriter(outf);

        int x = 0;
        int z = 0;
        boolean inFunction = false;
        boolean inGroup = false;
        boolean runningFunction = false;
        ArrayList<String> currentFunction = new ArrayList<>();
        ArrayList<String> functionContent = new ArrayList<>();
        ArrayList<String> parameters = new ArrayList<>();
        String currentFunctionName = "";
        HashMap<String, String> functionParameters = new HashMap<>();
        while (code.hasNextLine() || functionContent.size() != 0) {
            String line = "";
            if (functionContent.size() == 0) {
                // if (runningFunction) {
                runningFunction = false;
                functionParameters.clear();
                // inGroup = false;
                // }
                line = code.nextLine();
            } else {
                // inGroup = true;
                runningFunction = true;
                line = functionContent.removeFirst();
                if (debug) {
                    System.out.println("Function provided line:");
                }
            }
            Token[] tokens = tokenize(splitString(line));
            for (Token token : tokens) {
                System.out.print("'" + token.content + "'(" + token.function + ") ");

            }

            System.out.println();
            boolean first = true;
            int tokenNr = 0;
            for (Token token : tokens) {
                if (token.content.startsWith("//")) {
                    break;
                }
                if (inFunction) {
                    if (token.function == 901) {
                        // inGroup = false;
                        inFunction = false;
                        if (debug) {
                            System.out.println("Added function " + currentFunctionName);

                        }
                        addFunction(currentFunctionName, currentFunction, new ArrayList<String>(parameters));
                    }

                    currentFunction.add(line);
                    // tokenNr++;
                    break;
                }
                if (first) {
                    first = false;
                    if (token.function == 0) {
                        // Guess what: Functions
                        // throw new Exception("Invalid command argument, got '" + token.content + "' on
                        // line" + lineNr);
                        if (inGroup == true) {
                            if (debug) {
                                System.out.println("Called function " + token.content);
                            }
                            HashMap<ArrayList<String>, ArrayList<String>> h = getFunction(token.content);
                            Collection<ArrayList<String>> s = h.values();
                            ArrayList<String> origarr = null;
                            for (ArrayList<String> arrs : s) {
                                origarr = arrs;
                            }
                            boolean insideParanthesis = false;
                            boolean dont = false;
                            ArrayList<String> calledParameters = new ArrayList<>();
                            for (Token tok : tokens) {
                                dont = false;
                                if (tok.content.equals("(")) {
                                    insideParanthesis = true;
                                    dont = true;
                                } else if (tok.content.equals(")")) {
                                    insideParanthesis = false;
                                }
                                if (insideParanthesis && !dont) {
                                    calledParameters.add(tok.content);
                                }
                            }
                            Set<ArrayList<String>> parametersSet = h.keySet();
                            ArrayList<String> parameters2 = (ArrayList<String>) parametersSet.toArray()[0];
                            if (parameters2.size() != calledParameters.size()) {
                                throw new Exception(
                                        "Function parameter count does not match on line "
                                                + lineNr);
                            }
                            for (int param = 0; param < calledParameters.size(); param++) {
                                functionParameters.put(parameters2.get(param), calledParameters.get(param));
                            }
                            if (origarr == null) {
                                throw new Exception(
                                        "Seems like you managed to find a bug in my code, also this function  might not exist on line "
                                                + lineNr);
                            }
                            ArrayList<String> newfunc = new ArrayList<>(origarr);
                            newfunc.addAll(functionContent);
                            functionContent = new ArrayList<>(newfunc);
                            break;
                        } else {
                            if (tokens.length > tokenNr) {

                                if (line.trim().endsWith("{")) {
                                    currentFunctionName = token.content;
                                    currentFunction.clear();
                                    parameters.clear();
                                    boolean insideParanthesis = false;
                                    boolean dont = false;
                                    for (Token tok : tokens) {
                                        dont = false;
                                        if (tok.content.equals("(")) {
                                            insideParanthesis = true;
                                            dont = true;
                                        } else if (tok.content.equals(")")) {
                                            insideParanthesis = false;
                                        }
                                        if (insideParanthesis && !dont) {
                                            parameters.add(tok.content);
                                        }
                                    }
                                    // inGroup = true;
                                    inFunction = true;
                                    break;
                                } else {
                                    // throw new Exception("Unopened '{', insted got " + tokens[tokenNr + 1].content
                                    // + " on line " + lineNr);
                                    inGroup = true;
                                    if (debug) {
                                        System.out.println("Called function " + token.content);
                                    }
                                    HashMap<ArrayList<String>, ArrayList<String>> h = getFunction(token.content);
                                    Collection<ArrayList<String>> s = h.values();
                                    ArrayList<String> origarr = null;
                                    for (ArrayList<String> arrs : s) {
                                        origarr = arrs;
                                    }
                                    boolean insideParanthesis = false;
                                    boolean dont = false;
                                    ArrayList<String> calledParameters = new ArrayList<>();
                                    for (Token tok : tokens) {
                                        dont = false;
                                        if (tok.content.equals("(")) {
                                            insideParanthesis = true;
                                            dont = true;
                                        } else if (tok.content.equals(")")) {
                                            insideParanthesis = false;
                                        }
                                        if (insideParanthesis && !dont) {
                                            calledParameters.add(tok.content);
                                        }
                                    }
                                    Set<ArrayList<String>> parametersSet = h.keySet();
                                    ArrayList<String> parameters2 = (ArrayList<String>) parametersSet.toArray()[0];
                                    if (parameters2.size() != calledParameters.size()) {
                                        throw new Exception(
                                                "Function parameter count does not match on line "
                                                        + lineNr);
                                    }
                                    for (int param = 0; param < calledParameters.size(); param++) {
                                        functionParameters.put(parameters2.get(param), calledParameters.get(param));
                                    }
                                    if (origarr == null) {
                                        throw new Exception(
                                                "Seems like you managed to find a bug in my code, also this function  might not exist on line "
                                                        + lineNr);
                                    }
                                    ArrayList<String> newfunc = new ArrayList<>(origarr);
                                    newfunc.addAll(functionContent);
                                    functionContent = new ArrayList<>(newfunc);
                                    functionContent.add("}");
                                    pw.println(x + " 0 " + z + " 1 0.001");
                                    pw.println(x + " 1 " + z + " 2 0.001");
                                    x++;
                                    pw.println(x + " 0 " + z + " 0 0.001 ");
                                    x++;
                                    break;
                                }
                            } else {
                                throw new Exception("Invalid function structure on line " + lineNr);
                            }
                        }
                    } else if (token.function == 1) { // recv
                        if (inGroup) {
                            int c = getColor(tokens, tokenNr, false, functionParameters);
                            pw.println(x + " 0 " + z + " 4 0.001 " + c);
                            x++;
                        } else {
                            throw new Exception("Recieve not in group on " + lineNr);
                        }
                    } else if (token.function == 2) { // transmit
                        if (inGroup) {
                            int c = getColor(tokens, tokenNr, false, functionParameters);
                            pw.println(x + " 0 " + z + " 3 0.001 " + c);
                            x++;
                        } else {
                            throw new Exception("Transmit not in group on " + lineNr);
                        }
                    } else if (token.function == 3) { // logic
                        if (inGroup) {
                            int c = getColor(tokens, tokenNr, true, functionParameters);
                            pw.println(x + " 0 " + z + " 10 " + c);
                            x++;
                        } else {
                            throw new Exception("Transmit not in group on " + lineNr);
                        }
                    } else if (token.function == 4) { // force
                        if (inGroup) {
                            int c = getColor(tokens, tokenNr, true, functionParameters);
                            pw.println(x + " 0 " + z + " 5 " + c);
                            x++;
                        } else {
                            throw new Exception("Transmit not in group on " + lineNr);
                        }
                    } else if (token.function == 5) { // group
                        if (tokens.length > tokenNr + 1) {
                            if (tokens[tokenNr + 1].function == 900) {
                                inGroup = true;
                                pw.println(x + " 0 " + z + " 1 0.001");
                                pw.println(x + " 1 " + z + " 2 0.001");
                                x++;
                                pw.println(x + " 0 " + z + " 0 0.001 ");
                                x++;

                            } else {
                                throw new Exception("Unopened '{', insted got " + tokens[tokenNr + 1].content
                                        + " on line " + lineNr);
                            }
                        } else {
                            throw new Exception("Invalid group structure on line " + lineNr);
                        }
                    } else if (token.function == 6) {
                        x = 0;
                        z += 3;
                    } else if (token.function == 901) {
                        if (inGroup) {
                            inGroup = false;
                            x++;
                        } else {
                            throw new Exception("Unnecesarry '}' on line " + lineNr);
                        }
                    } else if (token.function == 1000) {
                        first = true;
                    }
                } else {

                }
                tokenNr++;
            }
            pw.flush();
            if (!runningFunction) {
                lineNr++;
            }
        }
        pw.close();
        code.close();
        // pw.close();
        return;
    }

    static int safeInt(String s) throws Exception {
        try {
            int i = Integer.parseInt(s);
            return i;
        } catch (Exception e) {
            throw new Exception("Invalid number '" + s + "' on line " + lineNr);
        }
    }

    // I just realised that I should not be doing this function; Keeping it for
    // possible eventual use.
    /*
     * static col getColor(Token[] tokens, int tokenpos) throws Exception {
     * boolean opened = false;
     * int nr = 0;
     * col c = new col();
     * if (tokens.length > tokenpos) {
     * for (int i = tokenpos; i <= tokens.length; i++) {
     * if (opened) {
     * if (nr == 0) {
     * c.r = safeInt(tokens[i].content);
     * } else if (nr == 1) {
     * c.g = safeInt(tokens[i].content);
     * } else if (nr == 2) {
     * c.b = safeInt(tokens[i].content);
     * }
     * nr++;
     * }
     * if (tokens[i].function == 905) {
     * opened = true;
     * } else if (tokens[i].function == 906) {
     * break;
     * }
     * 
     * }
     * } else {
     * throw new Exception("Command requires a color '(r g b)' on line " + lineNr);
     * }
     * return c;
     * }
     */
    static HashMap<HashMap<String, ArrayList<String>>, ArrayList<String>> functions = new HashMap<>();

    static void addFunction(String name, ArrayList<String> content, ArrayList<String> parameters) {
        if (debug) {
            System.out.println("--FUNCTION CONTENTS--");
            for (String s : content) {
                System.out.println(s);
            }
            System.out.println("--END--");
        }
        HashMap<String, ArrayList<String>> params = new HashMap<>();

        params.put(name, parameters);
        functions.put(params, new ArrayList<>(content));
    }

    static void printArray(ArrayList<String> content) {
        for (String s : content) {
            System.out.println(s);
        }
    }

    static HashMap<ArrayList<String>, ArrayList<String>> getFunction(String name)
            throws Exception {
        HashMap<String, ArrayList<String>> orighash = new HashMap<>();
        boolean found = false;
        for (HashMap<String, ArrayList<String>> hash : functions.keySet()) {
            if (hash.containsKey(name)) {
                orighash.put(name, hash.get(name));
                found = true;
            }
        }
        if (!found) {
            throw new Exception("No function with this name or mispelled core function on line " + lineNr);
        }
        ArrayList<String> orig = functions.get(orighash);
        ArrayList<String> arr = new ArrayList<String>(orig);
        HashMap<ArrayList<String>, ArrayList<String>> result = new HashMap<>();
        result.put(orighash.get(name), arr);
        if (orig == null) {
            throw new Exception("No function with this name or mispelled core function on line " + lineNr);
        }
        return result;

    }

    static Random r = new Random(0);
    static HashMap<String, Integer> colors = new HashMap<>();

    static int getColor(Token[] tokens, int tokenpos, boolean getinteger, HashMap<String, String> functionParamaters)
            throws Exception {
        boolean opened = false;
        boolean shouldClose = false;
        // int nr = 0;
        int c = -1;
        // If this is a confusing name - it means "make a new variable from function
        // parameters"
        boolean makeNewFunc = false;
        String name = "";
        if (tokens.length > tokenpos) {
            for (int i = tokenpos; i < tokens.length; i++) {
                if (!getinteger) {
                    if (opened && !shouldClose) {
                        if (colors.containsKey(tokens[i].content)
                                || functionParamaters.containsKey(tokens[i].content)) {
                            if (functionParamaters.containsKey(tokens[i].content)) {
                                String s = functionParamaters.get(tokens[i].content);
                                // if (s == null) {
                                // throw new Exception("Not a valid color on line " + lineNr);
                                // }
                                // for (String str : colors.keySet()) {
                                // System.out.println(str);
                                // }
                                if (colors.containsKey(s)) {
                                    c = colors.get(s);
                                } else {
                                    makeNewFunc = true;
                                    name = s;
                                }
                            } else
                                c = colors.get(tokens[i].content);
                        } else if (!colors.containsKey(tokens[i].content)) {
                            c = colorNr;
                            colors.put(tokens[i].content, colorNr);
                            colorNr++;
                        }
                        if (makeNewFunc) {
                            c = colorNr;
                            colors.put(name, colorNr);
                            colorNr++;
                        }
                        if (debug) {
                            System.out.print("color: '" + tokens[i].content + "' ");
                        }
                        shouldClose = true;
                        continue;
                    }
                } else {
                    if (opened && !shouldClose) {
                        if (debug) {
                            System.out.print("color'" + tokens[i].content + "' ");
                        }
                        // For logic gates
                        if (tokens[i].content.toUpperCase().equals("OR")) {
                            c = 1;
                        } else if (tokens[i].content.toUpperCase().equals("AND")) {
                            c = 2;
                        } else if (tokens[i].content.toUpperCase().equals("NOT")) {
                            c = 3;
                        } else if (tokens[i].content.toUpperCase().equals("NAND")) {
                            c = 4;
                        } else if (tokens[i].content.toUpperCase().equals("NOR")) {
                            c = 5;
                        } else if (tokens[i].content.toUpperCase().equals("XOR")) {
                            c = 6;
                        } else if (tokens[i].content.toUpperCase().equals("XNOR")) {
                            c = 7;
                        } // For force gates
                        else if (tokens[i].content.toUpperCase().equals("F_IF_T")) {
                            c = 1;
                        } else if (tokens[i].content.toUpperCase().equals("T_IF_T")) {
                            c = 2;
                        } else if (tokens[i].content.toUpperCase().equals("F_IF_F")) {
                            c = 3;
                        } else if (tokens[i].content.toUpperCase().equals("T_IF_F")) {
                            c = 4;
                        } else if (tokens[i].content.toUpperCase().equals("NEVER")) {
                            c = 5;
                        } else if (tokens[i].content.toUpperCase().equals("ALWAYS")) {
                            c = 6;
                        } else {
                            c = safeInt(tokens[i].content);
                        }
                        shouldClose = true;
                        continue;
                    }
                }
                if (tokens[i].function == 905) {
                    opened = true;
                } else if (tokens[i].function == 906 && shouldClose) {
                    break;
                }
                if (shouldClose && tokens[i].function != 906) {
                    throw new Exception("')' expected after color variable on line " + lineNr);
                }
                if (i == tokens.length - 1) {
                    if (!opened || !shouldClose) {
                        throw new Exception("expected valid color structure '(var)' on line " + lineNr);
                    }
                }
            }
        } else {
            throw new Exception("Command requires a color '(var)' on line " + lineNr);
        }
        if (debug) {
            System.out.print(c);
            System.out.println();
        }
        return c;
    }

    static Token[] tokenize(String[] in) {
        String[] ss = in;
        Token[] tks = new Token[ss.length];
        int i = 0;
        for (String string : ss) {
            Token t = new Token();
            t.content = string;
            t.match();
            tks[i] = t;
            i++;
        }
        return tks;
    }
}
