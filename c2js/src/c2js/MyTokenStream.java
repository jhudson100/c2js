/*
 */
package c2js;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

/**
 *
 * @author jhudson
 */
public class MyTokenStream implements TokenStream {

    class Terminal {

        String lhs;
        Pattern rex;

        Terminal(String l, Pattern r) {
            lhs = l;
            rex = r;
        }
    }
    String input;
    ArrayList< Terminal> terminals = new ArrayList<>();
    int line = 1;
    int idx = 0;
    Token prev = null;

    MyTokenStream(String input) throws FileNotFoundException {
        this.input = input;
        var fs = new FileInputStream("terminals.txt");
        var scan = new Scanner(fs);
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            int idx = line.indexOf(':');
            if (idx != -1) {
                String lhs = line.substring(0, idx).trim();
                String rhs = line.substring(idx + 1).trim();
                var rex = Pattern.compile("\\A" + rhs);
                terminals.add(new Terminal(lhs, rex));
            }
        }
    }

    @Override
    public Token LT(int k) {
        if (k == -1) {
//            System.out.println("LT(" + k + ")=" + prev);
            return prev;
        } else if (k == 0) {
            throw new RuntimeException();
        } else {
            int j = this.idx;
            int line = this.line;
            MyToken T = null;
            for (int i = 0; i < k; ++i) {
                T = next(j, line);
                j = T.getStopIndex();
                line += T.numNewlines;
            }
//            System.out.println("LT(" + k + ")=" + T);
            return T;
        }
    }

//    private class NextResult{
//        Token token;
//        int newIndex;
//        int deltaLine;
//        NextResult( Token t, int newIndex, int deltaLine ){
//            this.token=t;
//            this.newIndex=newIndex;
//            this.deltaLine = deltaLine;
//        }
//    }
    
    public MyToken peek(){
        return next(this.idx, this.line);
    }
    
    private MyToken next(int idx, int startingLine) {

        while (idx < input.length() && input.charAt(idx) == '#' && (idx == 0 || input.charAt(idx - 1) == '\n')) {
            //preprocessor directive
            int end = input.indexOf('\n', idx);
            String directive = input.substring(idx, end);
            String[] options = directive.split("\\s+");
            if (options[0].equals("#")) {
                //# line file flags
                startingLine = Integer.parseInt(options[1]);
            } else {
                throw new RuntimeException("Unknown preprocessor directive: " + directive);
            }
            idx = end;
        }
        
        if (idx >= input.length()) {
            return new MyToken("$", EOF, this.line, "$", idx, startingLine);
        }

        for (int i = 0; i < terminals.size(); ++i) {
            var p = terminals.get(i);
            var m = p.rex.matcher(input);
            m.region(idx, input.length());
            if (m.lookingAt()) {
//                System.out.println("Got match with " + p.lhs + " /" + p.rex.pattern() + "/ for text " + 
//                        input.substring(idx,Math.min(idx+40,input.length())));
                String sym = p.lhs;
                String lexeme = m.group(0);
                var T = new MyToken(sym, i + 1, line, lexeme, idx, startingLine);
                if (sym.equals("WHITESPACE") || sym.equals("COMMENT")) {
                    return next(idx + lexeme.length(), startingLine + T.numNewlines);
                } else {
                    return T;
                }
            }
        }
        
        System.out.println("No match for token");
        System.out.println(input.substring(idx,Math.min(idx+40,input.length())));
        System.exit(1);
        throw new RuntimeException("No match for token");
    }

    @Override
    public Token get(int index) {
        System.out.println("get(" + index + ")");
        return next(index, 999999);
    }

    @Override
    public String getText(Interval interval) {
        return "????";
    }

    @Override
    public String getText() {
        return "???";
    }

    @Override
    public String getText(RuleContext ctx) {
        return "??";
    }

    @Override
    public String getText(Token start, Token stop) {
        return input.substring(start.getStartIndex(), stop.getStopIndex());
    }

    @Override
    public void consume() {
        var T = next(this.idx, this.line);
        prev = T;
        this.idx = T.getStopIndex();
        this.line = T.startLine+T.numNewlines;
        System.out.println("Consumed: " + T.sym);
    }

    @Override
    public int LA(int i) {
//        System.out.println("LA(" + i + ")");
        return LT(i).getType();
    }

    @Override
    public int index() {
        return this.idx;
    }

    @Override
    public void seek(int index) {
//        System.out.println("seek(" + index + ")");
        this.idx = index;
    }

    ////////////////////////////////////////////////////
    @Override
    public int size() {
        return input.length();
    }

    @Override
    public String getSourceName() {
        return UNKNOWN_SOURCE_NAME;
    }

    @Override
    public TokenSource getTokenSource() {
        return null;
    }

    @Override
    public int mark() {
        return 42;
    }

    @Override
    public void release(int marker) {
    }

}
