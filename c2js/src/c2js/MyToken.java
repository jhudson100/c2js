/*
 */
package c2js;

import org.antlr.v4.runtime.CommonToken;

/**
 *
 * @author jhudson
 */
public class MyToken  extends CommonToken {
    String sym;
    int startLine;
    int numNewlines;
    
    MyToken(String sym, int num, int line, String lexeme, int startIndex, int startLine){
        super(num);
        this.sym=sym;
        super.setLine(line);
        super.setText(lexeme);
        super.setStartIndex(startIndex);
        super.setStopIndex(startIndex+lexeme.length());
        this.startLine = startLine;
        numNewlines=0;
        for(int i=0;i<lexeme.length();++i){
            if( lexeme.charAt(i) == '\n' )
                ++numNewlines;
        }
    }
    
    public String getSymbol(){
        return sym;
    }
 
    public String toString(){
        return "["+this.sym+" line="+this.getLine()+" lexeme='"+this.getText()+"' ]";
    }
}
