package com.company;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.List;
/*
Extended from GUI class, our main class that does most of the task and store the collections
 */

public class ExtendGUI extends GUI {

    public static Map<Integer, Node> nodes = new HashMap<>(); //  a Collection of Nodes (graph)
    public static List<Segment> segments = new ArrayList<>();          // a Collection of Segments
    public static Map <Integer, Road> roads = new HashMap<>();  // a Collection of Roads
    private Location origin;
    private Location bottom_right_origin;
    private double scale = 50;
    private double ORIGIN_MOVEMENT_AMOUNT = 100;   // defines how much the map move when button pressed
    private double ZOOM_FACTOR = 1.2;           // defines the scale when zooming and panning
    private double MAX_DISTANCE = 0.5;    // defines how far away from a node you can click to choose.
    public List <Node> highlightedNodes = new ArrayList<>();
    public List <Node> highlightedNodes_search = new ArrayList<>();
    private Color highlight = Color.red;  // some colors
    private Color initial_color = Color.black;
    public List <Road> highlightRoads = new ArrayList<>();
    private TrieNode root; // root of Trie



    @Override
    protected void redraw(Graphics g) {

        for (Node n : nodes.values()){    //draw nodes
            g.setColor(initial_color);
            n.draw(g,origin,scale);
        }

        for(Segment s : segments){        //draw segment
            g.setColor(initial_color);
            s.draw(g, origin, scale);
        }

        if (highlightedNodes.size() !=0){     // draw highlighted node(intersection)
            int size = highlightedNodes.size();
            g.setColor(highlight);

            for(Node highlightedNode: highlightedNodes) {
                highlightedNode.draw(g, origin, scale);
            }

            if(size == 2){
                highlightedNodes = new ArrayList<>();
            }

        }

        if(!highlightedNodes_search.isEmpty()){   // draw highlighted nodes for search result
            g.setColor(highlight);
            for(Node n : highlightedNodes_search){
                n.draw(g,origin,scale);
            }
            highlightedNodes_search = new ArrayList<>();
        }


        if(!highlightRoads.isEmpty()) {     //draw highlighted roads

            g.setColor(highlight);
            for (Road r : highlightRoads) {
                for (Segment seg : r.segments) {
                    seg.draw(g, origin, scale);

                }

            }

        }
    }

    @Override
    protected void onClick(MouseEvent e) {
        Point mouse_point = e.getPoint();   //get mouse's point
        Location mouse_location = Location.newFromPoint(mouse_point, this.origin,this.scale); //convert to location
        double distance = Double.MAX_VALUE;//distance between target location and mouse location
        Node target = null;

        // find the closet node first
        for(Node n : nodes.values()){
            if(n.location.distance(mouse_location) < distance){
                distance = n.location.distance(mouse_location);   // get the closest distance
                target = n;
            }
        }

        if(target.location.isClose(mouse_location,MAX_DISTANCE)){ // compare the closest distance with max distance
            if(this.highlightedNodes.size()<=1)this.highlightedNodes.add(target);
            if(this.highlightedNodes.size()==2){     // start algorithm when we found a pair of nodes
                Node start = highlightedNodes.get(0);
                Node end = highlightedNodes.get(1);


                    applyAstar(start,end);

                    getTextOutputArea().setText("Intersection ID (Start): " + start.nodeID +
                            "\nRoads at this intersection:" + start.getRoads_name()+"\n"+
                            "\nIntersection ID (End): " + end.nodeID +
                            "\nRoads at this intersection:" + end.getRoads_name()+ "\n");  // remember only exist one output area

                }

                else {
                    getTextOutputArea().setText("Intersection ID: " + target.nodeID + "\nRoads at this intersection:" +
                            target.getRoads_name());
                }

        }
    }

    @Override
    protected void onSearch() {
        if (root == null){return;}
        String input = getSearchBox().getText();
        List <Road> trie_results = getALL(input.toCharArray());  //get results from the trie

        boolean exact_matched = false;

        for(Road roads:trie_results){       //see if the roads exactly match the user input
            if(roads.label.equals(input))
                exact_matched = true;
        }

        if(exact_matched){                 //if exactly matched, make a new set for exactly matched roads
            List<Road> exact_results = new ArrayList<>();
            for(Road r: trie_results){
                if(r.label.equals(input))
                    exact_results.add(r);
            }
            trie_results = exact_results;
        }

        this.highlightRoads = trie_results; // assign the roads to highlight collection

        if(highlightRoads.size()==0){   //// stop the method and show no roads when no results found
            getTextOutputArea().setText("No such road!");
            return;
        }

        // display highlighted road's information
        String info = "";
        Set<String> road_info = new HashSet<>();

        for (Road r: highlightRoads){ // add into set to remove duplicates
            road_info.add(r.label);
        }

        for(String str : road_info){
            info = info + str + "\n";
        }

        getTextOutputArea().setText(road_info.size() + " Road(s) in total highlighted\n" + info);
    }

    @Override
    protected void onMove(Move m) {
        if (m == GUI.Move.NORTH){
            origin = origin.moveBy(0, ORIGIN_MOVEMENT_AMOUNT / scale); // divide by scale to keep the move amount unchanged at every zoom condition
        }
        else if(m == Move.SOUTH){
            origin = origin.moveBy(0, -ORIGIN_MOVEMENT_AMOUNT / scale);
        }
        else if(m == Move.EAST){
            origin = origin.moveBy(ORIGIN_MOVEMENT_AMOUNT / scale,0);
        }
        else if(m == Move.WEST){
            origin = origin.moveBy(-ORIGIN_MOVEMENT_AMOUNT / scale,0);

        }
        else if(m == Move.ZOOM_IN){
            scale = scale*ZOOM_FACTOR;     //set scale
            double width = bottom_right_origin.x - origin.x; //getALL new width and height
            double height = origin.y - bottom_right_origin.y;
            double dx = (width*ZOOM_FACTOR - width)/2;       // getALL x gap and y gap
            double dy = (height * ZOOM_FACTOR - height)/2;
            origin.moveBy(-dx,-dy);        // move origin(top left)
            bottom_right_origin.moveBy(dx,dy); // move bottom right origin
        }

        else if(m == Move.ZOOM_OUT){
            scale = scale / ZOOM_FACTOR;
            double width = bottom_right_origin.x - origin.x;
            double height = origin.y - bottom_right_origin.y;
            double dx = (width - width/ZOOM_FACTOR)/2;
            double dy = (height - height/ZOOM_FACTOR)/2;
            origin.moveBy(dx,dy);
            bottom_right_origin.moveBy(-dx,-dy);
        }
    }

    @Override
    protected void onLoad(File nodes, File roads, File segments, File polygons) {
        readNode(nodes);
        readRoads(roads);
        readSegments(segments);
        makeOrigin();
        this.root = new TrieNode();     // initialize root trie node
        addRoads();                     // construct our trie
    }

    //-------------------------Methods below are implemented by myself----------------------------------



    /*
    This method apply the A star algorithm to the given two nodes and return a string of segments information
    which indicates the shortest path between start and end.
     */


    public void applyAstar(Node start, Node end){
        if(start == null || end == null){ return;}

        A_StarSearch A_search = new A_StarSearch();

        A_search.search(start,end);

        //highlight nodes of the route

        Node temp = end.previousNode;
        if (temp == null){return;}

        while(true){
            System.out.println(temp.nodeID);
            System.out.println(start.nodeID);
            if(temp.previousNode != start){

                highlightedNodes_search.add(temp);
                temp = temp.previousNode;
            }
            else{
                highlightedNodes_search.add(temp);
                break;
            }
        }

    }














    /*
    This method load all the Road object we have into the trieNode (root)
    */

    public void addRoads(){
        for (Road r: roads.values()){ ;
            char [] char_list = r.label.toCharArray();
            add(char_list,r);
        }
    }


    /*
    This method add Road and its name to the trie structure
     */

    public void add(char[] word, Road road){
        TrieNode node = this.root;
        for(int i = 0 ; i < word.length; i++){
            if(!node.children.containsKey(word[i])) {   // if node's children does not contain word[i]
                node.children.put(word[i], new TrieNode());     // create a new child of node, connecting to node via c
            }
            node = node.children.get(word[i]); // make node to be the new child or the new child of target Node

        }
        node.roads.add(road);  //add a reference to the road to the last node
    }

    /*
    This method gets a collection of all Roads correspond to the prefix
     */

    public List<Road> getALL(char[] prefix){
        List<Road> results = new ArrayList<>();
        TrieNode node = this.root;

        //find the node that contains the very first prefix result
        for(char c : prefix){
            if(!node.children.containsKey(c)){
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }

        //find the nodes that contains Road with prefix included  in name
        getALLFrom(node, results);
        return results;
    }
    /*
    Recursive method to retrieve the trie
     */

    public void getALLFrom(TrieNode node, List<Road> results){
        for(Road r: node.roads){
            results.add(r);
        }

        for (TrieNode child: node.children.values()){
            getALLFrom(child,results);
        }

    }
    /*This method generates the origin Location(and a bottrom right Location) by fining the minimum latitude of Nodes and the maximum longitude of Nodes.
    x is longitude, y is latitude (IMPORTANT ! order matters)
    */

    public void makeOrigin(){
        double min_long = Double.MAX_VALUE; // for finding top left origin location
        double max_lat = -Double.MAX_VALUE;
        double max_long = -Double.MAX_VALUE;   //for finding bottom right location
        double min_lat = Double.MAX_VALUE;

        for(Node n: nodes.values()){
            if(n.longi < min_long){
                min_long = n.longi;
            }
        }

        for(Node n: nodes.values()){
            if(n.longi < min_lat){
                min_lat = n.longi;
            }
        }

        for (Node n: nodes.values()){
            if(n.lat > max_lat){
                max_lat = n.lat;
            }
        }

        for (Node n: nodes.values()){
            if(n.lat > max_long){
                max_long = n.lat;
            }
        }

        this.bottom_right_origin = Location.newFromLatLon(min_lat,max_long);

        this.origin = Location.newFromLatLon(max_lat,min_long);

    }
    /*
    Method that reads the Node data and create a collection of Node
     */

    public void readNode(File n){

        try {
            FileReader fr = new FileReader(n);
            BufferedReader reader = new BufferedReader(fr);
            String the_line;
            String[] node_info;

            while (true) {
                the_line = reader.readLine();
                if (the_line == null) {
                    break;
                } else {
                    node_info = the_line.split("\t");
                    int id = toInt(node_info[0]);
                    double x = toDouble(node_info[1]);
                    double y = toDouble(node_info[2]);
                    Node node = new Node (id, x, y);
                    this.nodes.put(id,node);
                }
            }
            reader.close();

        } catch (Exception e) {
            throw new RuntimeException("file reading failed");
        }

    }


    /*
    Method that reads the Road data and create a collection of Road
     */
    public void readRoads(File n){
        try {
            FileReader fr = new FileReader(n);
            BufferedReader reader = new BufferedReader(fr);
            String the_line;
            String [] node_info;
            reader.readLine();// pass the first line

            while (true) {
                the_line = reader.readLine();
                if (the_line != null){
                    node_info = the_line.split("\t");
                    int id = toInt(node_info[0]);
                    int type = toInt(node_info[1]);
                    String lable = node_info[2];
                    String city = node_info [3];
                    int oneway = toInt(node_info[4]);
                    int speed = toInt(node_info[5]);
                    int road_class = toInt(node_info[6]);
                    int notforcar = toInt(node_info[7]);
                    int notforpede = toInt(node_info[8]);
                    int notforbicy = toInt(node_info[9]);
                    Road road = new Road(id, type, lable, city, oneway, speed, road_class,notforcar,notforpede,notforbicy);
                    this.roads.put(id, road);
                }
                else{
                    break;
                }
            }
            reader.close();
        }
        catch (Exception e) {
            throw new RuntimeException("file reading failed");
        }
    }

    /*
    Method that reads the Segment data and create a collection of Segments
     */
    public void readSegments(File n){

        try {
            BufferedReader reader = new BufferedReader(new FileReader(n));
            reader.readLine();
            String the_line;
            String[] seg_info;

            while ((the_line = reader.readLine()) != null) {
                seg_info = the_line.split("\t");
                int roadID = toInt(seg_info[0]);
                double length = toDouble(seg_info[1]);
                int nodeID1 = toInt(seg_info[2]);
                int notdeID2 = toInt(seg_info[3]);

                double[] coords = new double[seg_info.length - 4]; //create the coordinate array
                for (int i = 4; i < seg_info.length; i++) {
                    coords[i - 4] = toDouble(seg_info[i]);
                }
                Segment segment = new Segment(roadID, length, nodeID1, notdeID2, coords);
                this.segments.add(segment);
            }
            reader.close();
        }

        catch(Exception e){
            throw new RuntimeException("file reading failed");
        }
    }
    /*
    Helper method to parse raw data
     */
    public int toInt(String str){
        return Integer.parseInt(str);
    }

    /*
    Helper method to parse raw data
     */
    public double toDouble (String str){
        return Double.parseDouble(str);
    }


    public static void main(String[] args){
        new ExtendGUI();
    }
}
