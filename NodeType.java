/*
    This file represents an object that is used to store 
    type related information for each node of the tree
 */

import c1Absyn.Dec;

public class NodeType { //todo: maybe make this abstract, and make 3 children for simple, array, function types (remove Dec and use)
    
    public String name; 
    public Dec dec;
    public int level; 

    public NodeType(String name, Dec dec, int level) {
        this.name = name;
        this.dec = dec; 
        this.level = level;
    }  

 }

