package com.company;

import java.util.ArrayList;
import java.util.List;
/*
Road is the object that contains Segments and represent roads in out map. Only useful at searching.
 */


public class Road {

    public  int id;
    public  int type;
    public  String label;
    public  String city;
    public  int oneway;
    public  int speed;
    public  int road_class;
    public  int not_for_car;
    public  int not_for_pede;
    public  int not_for_bicy;
    public List<Segment> segments;


    public Road(int roadID, int roadType, String roadLabel, String roadCity, int r_oneway, int r_speed, int r_class,
                int car, int pede, int bicycle){
        id = roadID;
        type = roadType;
        label = roadLabel;
        city = roadCity;
        oneway = r_oneway;
        speed = r_speed;
        road_class = r_class;
        not_for_car = car;
        not_for_pede = pede;
        not_for_bicy = bicycle;
        segments = new ArrayList<>();
    }
    /*
    Method for adding a segment to the road it belongs.
     */

    public void addSegment(Segment seg){
        this.segments.add(seg);

    }

}
