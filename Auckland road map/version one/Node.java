package com.company;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

import static com.company.ExtendGUI.roads;
/*
Nodes are intersections, or ends of roads, or points where two roads join. It represents the graph structure by storing two lists of
edges.
 */

public class Node {
    public  int nodeID;
    public  double lat,longi;
    public  List<Segment> outgoing_segment,incoming_segment;
    Location location;
    public Node previousNode;
    public Boolean isVisited;



    public Node (int id, double latitude, double longitude){
        this.nodeID = id;
        this.lat =latitude;
        this.longi =longitude;
        this.location = Location.newFromLatLon(latitude,longitude);
        outgoing_segment = new ArrayList<>();
        incoming_segment = new ArrayList<>();
    }

    /*
    method that draws every nodes
     */
    public void draw(Graphics g, Location origin, double scale){
        Point point = this.location.asPoint(origin,scale); // getALL point

        int size = 4;

        g.fillRect(point.x-size/2, point.y-size/2, size,size);
    }
    /*
    Helper methods to put a segment into the node it connects to
     */
    public void addSegment_in(Segment seg){
        incoming_segment.add(seg);
    }

    public void addSegment_out(Segment seg){
        outgoing_segment.add(seg);
    }

    /*
    this method returns a string which contains all the road that across this road.(in service of onclick method)
     */

    public String getRoads_name(){
        Set<String> names = new HashSet<>();  // create an empty set for roads via this node to aviod duplicates
        String road_names = " ";
        for(Segment seg: outgoing_segment){      // visit outgoing segment list
            int roadID = seg.roadID;            //getALL road id for each of the segment
            Road road = roads.get(roadID);      // obtain the road object from road id
            String road_name = road.label;      // getALL road's name
            if(!names.contains(road_name))
                names.add(road_name);               // add road'sname to the set
        }


        for(Segment seg: incoming_segment){    // easy way to getALL road name
            String road_name = seg.road.label;
            if (!names.contains(road_name))
                names.add(road_name);
        }

        for(String str : names){
            road_names = road_names + str + ", ";
        }

        return road_names;

    }


    public void setPrev(Node n){
        this.previousNode = n;

    }

    public void setVisited(){
        this.isVisited = true;
    }

    public List<Node> findNeighbours(){
        List<Node> neighbours = new ArrayList<Node>();

        for(Segment s: ExtendGUI.segments){

            if(s.from.nodeID == this.nodeID){
                neighbours.add(s.to);
            }
        }
        return neighbours;
    }



}
