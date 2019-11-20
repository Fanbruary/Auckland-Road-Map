package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
TrieNode is the core object that builds up my Trie stucture
 */

public class TrieNode {
    public List<Road> roads = new ArrayList<>();
    public Map<Character, TrieNode> children = new HashMap<>();


}
