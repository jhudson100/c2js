/*
 */
package c2js;

import java.util.ArrayList;

/**
 *
 * @author jhudson
 */
public class TreeNode {
    String sym;
    MyToken token;
    ArrayList<TreeNode> children = new ArrayList<>();
    static int ctr=0;
    String name = "n"+(ctr++);
    
    TreeNode( String sym, MyToken t){
        this.sym=sym;
        this.token=token;
    }
}
