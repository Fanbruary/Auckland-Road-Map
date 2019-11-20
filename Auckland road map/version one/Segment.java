package com.company;

import java.awt.*;

/*
Segments make up a Road and it played an important role when manipulating the data structure
 */

public class Segment {
    public  int roadID;
    public  double length;
    public Node from, to;
    public Road road;
    public Location [] locations;



    public Segment(int roadID, double length, int nodeID1, int notdeID2, double[] coords){
        this.roadID = roadID;
        this.length = length;
        this.road = ExtendGUI.roads.get(roadID);
        this.from = ExtendGUI.nodes.get(nodeID1);
        this.to = ExtendGUI.nodes.get(notdeID2);

        this.locations = new Location [coords.length/2]; // create segments' Location array
        for (int i = 0; i < locations.length; i++){
            this.locations[i] = Location.newFromLatLon(coords[i*2], coords[i*2+1]);  // load location from lat and long
        }

        this.from.addSegment_out(this);  // add this segment to the nodes it connected with
        this.to.addSegment_in(this);
        this.road.addSegment(this);      // add this segment to the road it belongs to
    }

    /*
    Method that draws every Segments
     */

    public void draw(Graphics g, Location origin, double scale){
        for(int i = 1; i <locations.length;i++){  // getALL pairs of locations and then draw
            Point p1 = locations[i].asPoint(origin,scale);
            Point p2 = locations[i-1].asPoint(origin,scale);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }




    }



}
