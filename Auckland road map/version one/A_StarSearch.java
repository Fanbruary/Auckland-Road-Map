package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class A_StarSearch {



    /*
    This method perform the A start search algorithm.
     */
    public void search (Node start, Node end){
        // make all the nodes unvisited and the fringe has a starter element
        for(Node n : ExtendGUI.nodes.values()) {
            n.isVisited = false;
            n.previousNode = null;
        }
        FringeComponent starter = new FringeComponent(start, null , 0, estimatedDistance(start,end));
        PriorityQueue<FringeComponent> fringe = new PriorityQueue<>(FringeComponent::compareTo);
        fringe.offer(starter);

        while(!fringe.isEmpty()){
            FringeComponent temp = fringe.poll();  // expand the minimal
            Node currentNode = temp.currentNode;
            Node previousNode = temp.previousNode;

            if(currentNode.isVisited != true){
                currentNode.setVisited();
                currentNode.setPrev(previousNode);

                if(currentNode.nodeID  ==  end.nodeID){break;}

                for(Node n : currentNode.findNeighbours()){// expand to neighbours
                    if(n.isVisited != true){
                        double costFromStart = temp.costFromStart + findEdgeWeight(currentNode,n);
                        double estimatedDistance = costFromStart + estimatedDistance(n,end);
                        fringe.add(new FringeComponent(n, currentNode, costFromStart, estimatedDistance));
                    }
                }

            }

        }

    }


    /*
    This is the heuristic function which return the estimated distance between a node and the goal.
     */
    public double estimatedDistance (Node n, Node end){
        return n.location.distance(end.location);
    }

    /*
    Find edge weight between two nodes
     */
    public double findEdgeWeight (Node start, Node end){

        double weight = 0.0;
        for (Segment s : start.outgoing_segment){
            if (s.from==start && s.to == end)
                weight = s.length;
                break;
        }
        return weight;
    }



}
