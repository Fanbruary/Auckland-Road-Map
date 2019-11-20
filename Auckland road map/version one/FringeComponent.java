package com.company;

public class FringeComponent implements Comparable<FringeComponent>{

    public Node currentNode, previousNode;
    double costFromStart,estimatedTotal;



    public FringeComponent(Node n, Node prev, double costFromStart, double estimatedTotal){
        this.currentNode = n;
        this.previousNode = prev;
        this.costFromStart = costFromStart;
        this.estimatedTotal = estimatedTotal;
    }


    public int compareTo(FringeComponent f) {
        if(this.estimatedTotal < f.estimatedTotal){return -1;}

        else if(this.estimatedTotal > f.estimatedTotal){return 1;}

        else{return 0;}
    }



}
