import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;




	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			if (graph.highlightedNode != null){
				applyAstar(graph.highlightedNode,closest);
				graph.highlightedNode = null;
			}
			else{
				graph.setHighlight(closest);
				getTextOutputArea().setText(closest.toString());
			}

		}
	}
//------------------------------------------------------------------------------------------------------------//
    /*
    This method apply the A star algorithm to the given two nodes and return a string of segments information
    which indicates the shortest path between start and end.
     */


	public void applyAstar(Node start, Node end){
		if(start == null || end == null){ return;}

		//appy the algorithm
		search(start,end);

		// put the nodes into a list from the route we found
		List<Node> path_node = new ArrayList<>();
		Node temp = end;

		while(temp != start){
			path_node.add(temp);
			temp = temp.previousNode;
		}
		path_node.add(start);

		//put the nodes into highlight collection
		for(Node n : path_node){
			graph.setHighlightNodes(n);
		}

		//calculate the length of the route
		List <Segment> path_seg = new ArrayList<>();
		double length = 0.0; 	// route length

		for (int i = path_node.size() - 1; i > 0; i--){ //also put segments into a list
			Segment seg = findSegment(path_node.get(i),path_node.get(i-1));
			length = length + seg.length;
			path_seg.add(seg);
		}

		//put the segments into highlight collection
		for(Segment s : path_seg){
			graph.setHighlightSegments(s);
		}

		//construct the information sequence of the route
//		String segs = "";
//		for (Segment s : path_seg){
//			segs = segs + s.toString();
//		}

		//merge sequence of road segments
		Set <String> roadNames = new HashSet<>();
		for(Segment s: path_seg){
			roadNames.add(s.road.name);
		}
		//make room for each road
		Map<String, Double> mergedRoad = new HashMap<>();
		for(String s: roadNames){
			mergedRoad.put(s,0.0);
		}

		//calculate length of each road
		for(String name : roadNames){
			for(Segment s : path_seg){
				if(s.road.name.equals(name)){
					mergedRoad.put(name,mergedRoad.get(name) + s.length);
				}
			}
		}

		//construct merged sequence
		String output = "";
		for(String name: roadNames){
			output = output + "Road name: " + name + ".     Length: " + mergedRoad.get(name) + ". \n";
		}


		//print out the information about this route
		getTextOutputArea().setText("Route starts at:    \n" + start.toString() + "\n\n" + "Route ends at:    \n"
				+ end.toString()  + "\n\n" + "Sequence of road segments: \n" + output
				+ "\n" + "Route length:  " + length);


	}


	/*
This method perform the A start search algorithm.
 */
	public void search (Node start, Node end){
		// make all the nodes unvisited and the fringe has a starter element
		for(Node n : graph.nodes.values()) {
			n.isVisited = false;
			n.previousNode = null;
		}
		FringeComponent starter = new FringeComponent(start, null, 0, estimatedDistance(start,end));
		PriorityQueue <FringeComponent> fringe = new PriorityQueue<>(FringeComponent::compareTo);
		fringe.offer(starter);

		while(!fringe.isEmpty()){
			FringeComponent temp = fringe.poll();  // expand the minimal

			if(!temp.currentNode.isVisited){
				temp.currentNode.setVisited();
				temp.currentNode.setPrev(temp.previousNode);

				if(temp.currentNode.nodeID  ==  end.nodeID){break;}

				for(Node n : temp.currentNode.findNeighbours()){// expand to neighbours
					if(!n.isVisited){
						double costFromStart = temp.costFromStart + findEdgeWeight(temp.currentNode,n);
						double estimatedDistance = costFromStart + estimatedDistance(n,end);
						fringe.add(new FringeComponent(n, temp.currentNode, costFromStart, estimatedDistance));
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
		for (Segment s : start.segments){
			if (end.segments.contains(s)) {
				weight = s.length;
				break;
			}

		}
		return weight;
	}

	/*
	Find segment between two nodes
	 */
	public Segment findSegment (Node start, Node end){

		Segment temp = null;
		for (Segment s: start.segments){
			if (end.segments.contains(s)){
				temp = s;
				break;
			}
		}
		return temp;
	}



//----------------------------------------------------------------------------------------------------//


	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.roads.values());
		origin = new Location(-7, -1); // close enough
		scale = 100;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}
}

// code for COMP261 assignments