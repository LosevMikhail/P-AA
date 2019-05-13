package AhoCorasick;

import javafx.util.Pair;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

class Node {
    private int[] edges; // i'th edge edges[i] is a son with the edge i from the node (i is a character)
    private Node[] transits;
    private Node link; // suffix link
    private boolean isFinal;
    private Set<Integer> wordNum = new HashSet<>();
    private Node parent;
    private char incomingEdge;

    Node() { // root
        isFinal = false;
        edges = new int[Character.MAX_VALUE];
        transits = new Node[Character.MAX_VALUE];
        parent = this;
        incomingEdge = (char)0;
        link = this;
    }
    Node(Node parent, char edge, Node root) {
        isFinal = false;
        this.parent = parent;
        incomingEdge = edge;
        edges = new int[Character.MAX_VALUE];
        transits = new Node[Character.MAX_VALUE];
        link = root;
    }

    boolean isFinal() {
        return isFinal;
    }
    Node getParent() {
        return parent;
    }

    int getChildIndex(char letter) {
        return edges[letter];
    }
    void addEdge(char letter, int childIndex) {
        edges[letter] = childIndex;
    }
    Node getTransitIndex(char letter) {
        return transits[letter];
    }
    void setTransitIndex(char letter, Node node) {
        this.transits[letter] = node;
    }


    void setLink(Node link) {
        this.link = link;
    }
    Node getLink () {
        return link;
    }

    char getIncomingEdge() {
        return incomingEdge;
    }

    void makeFinal() {
        isFinal = true;
    }

    void setWordNum(int wordNum) {
        this.wordNum.add(wordNum);
    }

    Set<Integer> getWordNum() {
        return wordNum;
    }
}

class Trie {
    Vector<Node> nodes; // there can up to m = |words| of them

    Trie(String[] words) {
        nodes = new Vector<>();
        nodes.add(new Node()); // add root
        int i = 0;
        for (String word: words) {
            addWord(word);
            nodes.get(nodes.size() - 1).makeFinal();
            nodes.get(nodes.size() - 1).setWordNum(i);
            i++;
        }
    }

    private void addWord(String word) {
        Node currNode = nodes.get(Character.MIN_VALUE); // root
        Node nextNode;

        for (int i = 0; i < word.length(); i++) {
            char currEdge = word.charAt(i);
            //System.out.println("Current edge: " + currEdge);
            if (currNode.getChildIndex(currEdge) < nodes.size()) {
                nextNode = nodes.get(currNode.getChildIndex(currEdge));
            } else {
                nextNode = nodes.get(0);
            }

            if (nextNode == nodes.get(0)) {   // if next is root (that happens only when there is no edge currEdge)
                nextNode = new Node(currNode, currEdge, nodes.get(0));
                currNode.addEdge(currEdge, nodes.size());
                nodes.add(nextNode);
            }
            currNode = nextNode;
        }
    }

    Node getSuffixLink(Node node) {
        if (node.getLink() == nodes.get(0)) {  // not defined yet or node is the root or its son
            if (node == nodes.get(0) || node.getParent() == nodes.get(0)) { // node is the root or its son
                node.setLink(nodes.get(0)); // do nothing
            } else {    // node is not the root or its son
                node.setLink(transitFunction(node.getParent().getLink(), node.getIncomingEdge()));
            }
        }
        return node.getLink();
    }

    Node transitFunction(Node node, char letter) {
        //if (node == null) return nodes.get(0);
        if (node.getTransitIndex(letter) == null) node.setTransitIndex(letter, nodes.get(0));
        if (node.getTransitIndex(letter) == nodes.get(0)) {
            if (node.getChildIndex(letter) != 0) {
                node.setTransitIndex(letter, nodes.get(node.getChildIndex(letter))); // sigma(node, letter) = v, if v is a son of node by letter
            }
            else if (node == nodes.get(0)) { // if node == root
                node.setTransitIndex(letter, nodes.get(0));    // sigma(node, letter) = 0, if node is the root
            }
            else {
                node.setTransitIndex(letter, transitFunction(getSuffixLink(node), letter));
            }
        }
        return node.getTransitIndex(letter);
    }
}


public class AhoCorasick {
    private String[] needles;
    private Trie tire;
    private Vector<Pair<Integer, Integer>> solution;
    private int elementCounter;

    public AhoCorasick(String[] needles) { // no running
        this.needles = needles;
        tire = new Trie(needles);
        //tire.print(); // for debugging
    }
    public AhoCorasick(String[] needles, String haystack) {
        this.needles = needles;
        tire = new Trie(needles);
        run(haystack);
        //tire.print(); // for debugging
    }


    private void check(Node v, int i) {
        for (Node u = v; u != tire.nodes.get(0); u = tire.getSuffixLink(u)) {
            if (u.isFinal()) {
                for (int currWordNum: u.getWordNum()) {
                    solution.add(new Pair<>(i - needles[currWordNum].length(), currWordNum));
                }
            }
        }
    }

    public void run(String haystack){
        solution = new Vector<>();
        Node u = tire.nodes.get(0);
        for(int i = 0; i < haystack.length(); i++) {
            u = tire.transitFunction(u, haystack.charAt(i));
            check(u,i + 1);
        }
    }

    public int countOccurrences() {
        return solution == null ? 0 : solution.size();
    }

    public boolean hasMoreElements() {
        return solution != null && elementCounter < solution.size();
    }

    public Pair<Integer, Integer> nextOccurrence() {
        if (hasMoreElements())
            return solution.get(elementCounter++);
        return null;
    }
    public Object nextElement() {
        if (hasMoreElements())
            return solution.get(elementCounter++);
        return null;
    }





}

