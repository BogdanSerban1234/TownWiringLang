import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.*;

public class CodeRun {
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
            sc.nextLine();
            read = sc.nextInt();
        }
        Object[] chooseListArr = chooselist.toArray();
        File f = (File) chooseListArr[read];
        sc.close();
        Scanner code = new Scanner(f);
        File outf = new File(directory + "\\output.fwire");
        outf.createNewFile();
        PrintWriter pw = new PrintWriter(outf);
        int lineNr = 1;

        int x = 0;
        boolean expectParanthesis = false;
        boolean inGroup = false;
        while (code.hasNextLine()) {

            String line = code.nextLine();
            Token[] tokens = tokenize(line.split("[\\s{]+"));
            boolean first = true;
            int tokenNr = 0;
            for (Token token : tokens) {
                if (first) {
                    first = false;
                    if (token.function == 0) {
                        throw new Exception("Invalid command argument on line " + lineNr);
                    } else if (token.function == 1) {
                        pw.println(x + " 0 0 1 0.001");
                        x++;
                    } else if (token.function == 2) {
                        pw.println(x + " 0 0 1 0.001");
                        x++;
                    } else if (token.function == 3) {
                        x++;
                        pw.println(x + " 0 0 0 0.001");
                        x++;
                    } else if (token.function == 4) {
                    } else if (token.function == 5) {
                        if (tokens.length > tokenNr) {
                            if (tokens[tokenNr + 1].function == 901) {
                                expectParanthesis = true;
                                inGroup = true;
                                pw.println(x + " 0 0 1 0.001");
                                pw.println(x + " 1 0 2 0.001");
                                x += 2;
                                // continue here or whatever
                            } else {
                                throw new Exception("Unopened '{' on line " + lineNr);
                            }
                        } else {
                            throw new Exception("Invalid group structure on line " + lineNr);
                        }
                    } else if (token.function == 901) {
                    }
                } else {

                }
                tokenNr++;
            }
            pw.flush();
            lineNr++;
        }
        pw.close();
        code.close();
        pw.close();

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

class Token {
    String content = "";
    int function = 0;

    void match() {
        if (content.toLowerCase().equals("recieve")) {
            function = 1;
        } else if (content.toLowerCase().equals("transmit")) {
            function = 2;
        } else if (content.toLowerCase().equals("block")) {
            function = 3;
        } else if (content.toLowerCase().equals("colors")) {
            function = 4;
        } else if (content.toLowerCase().equals("group")) {
            function = 5;
        } else if (content.toLowerCase().equals("{")) {
            function = 900;
        } else if (content.toLowerCase().equals("}")) {
            function = 901;
        } else {
            function = 0;
        }
    }
}