/*
 */
package c2js;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.Tree;
import parser.gramParser;

/**
 *
 * @author jhudson
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException, Exception {
        String infile = "junk/test.c";
//        infile="test1";
                
        String inp = Files.readString(Path.of(infile));
        //inp = "int x=42;";
//        inp="void foo(){ bits = 1; }";
        inp = inp.replace("#include","//#include");
        ProcessBuilder pb = new ProcessBuilder("cpp","-P","-DEXPORT=");
        Process p = pb.start();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        var T = new Thread( () -> {
            var stm = p.getInputStream();
            byte[] b = new byte[4096];
            while(true){
                int nr;
                try {
                    nr = stm.read(b);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                if( nr <= 0 )
                    return;
                bos.write(b,0,nr);
            }
        });
        T.start();
        
        var stm = p.getOutputStream();
        stm.write(inp.getBytes());
        stm.close();
        T.join();
        
        inp = new String( bos.toByteArray() );
        
        try(var pw = new PrintWriter("junk/preprocessed")){
            pw.println(inp);
        }
        //System.out.println(inp);
        
        //var pp = new Preprocessor();
        //inp = pp.preprocess(inp);
        //var stream = new ANTLRInputStream(inp);
        //var lexer = new gramLexer(stream);
        //lexer.setTokenFactory(new MyTokenFactory());
        //var tokens = new CommonTokenStream(lexer);
        var TS = new MyTokenStream(inp);
        var parser = new gramParser(TS);
        parser.setBuildParseTree(true);
//        parser.setErrorHandler(new BailErrorStrategy());
        parser.setErrorHandler(new ErrorHandler(TS));
        try{
            Tree aroot = parser.start();
            TreeNode root = walk(aroot,parser.getRuleNames());
            writeDot("junk/test.dot",root);
            System.out.println("Wrote test.dot");
        } catch(ParseCancellationException ex){
            throw( (Exception) ex.getCause());
        }
    }
    
    static TreeNode walk(Tree node, String[] ruleNames){
        Object p = node.getPayload();
        if( p instanceof MyToken ){
            var T = (MyToken)p;
            return new TreeNode( T.sym, T );
        } else {
            var c = (RuleContext)p;
            int idx = c.getRuleIndex();
            String sym = ruleNames[idx];
            var N = new TreeNode(sym,null);
            for(int i=0;i<node.getChildCount();++i){
                var ch = node.getChild(i);
                N.children.add( walk( ch, ruleNames ));
            }
            return N;
        }
    }
    
    static void walk(TreeNode n, Function<TreeNode, Integer> f){
        f.apply(n);
        for(int i=0;i<n.children.size();++i){
            walk(n.children.get(i),f);
        }
    }
    static void writeDot(String fname, TreeNode n) throws FileNotFoundException{
        try( var pw = new PrintWriter(fname)){
            pw.println("digraph d {");
            walk( n, (TreeNode c) -> {
                pw.println(c.name+" [label=\""+c.sym+"\"];");
                return 0;
            });
            walk( n, (TreeNode c) -> {
                for(int i=0;i<c.children.size();++i){
                    pw.println(c.name+" -> "+c.children.get(i).name+";");
                }
                return 0;
            });
            pw.println("}");
        }
    }
}
