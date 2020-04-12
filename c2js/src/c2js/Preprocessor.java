/*
 */
package c2js;

import java.util.regex.Pattern;

/**
 *
 * @author jhudson
 */
public class Preprocessor {
    Pattern comment = Pattern.compile("(?s)/[*].*?[*]/|//[^\n]*");
    //Pattern inc = Pattern.compile("#\\s*include\\s+")
}
