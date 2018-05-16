package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.RobotController;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidCoordinatesException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies.datastructure.Coordinate;

public class AStarStrategy extends MazeRunningStrategy {
	
	private class Node {
		
		private Coordinate coordinate;
		private double totalCost, costToArrive, costToGoal;
		
		public Node(int x, int y, double totalCost, double costToArrive, double costToGoal) {
			this.coordinate = new Coordinate(x, y);
			this.totalCost = totalCost;
			this.costToArrive = costToArrive;
			this.costToGoal = costToGoal;
		}
		
		public int getX() {
			return this.coordinate.getX();
		}
		
		public int getY() {
			return this.coordinate.getY();
		}
		
		public double linearDistance(Node node) {
			double c1 = 0, c2 = 0;
			if(this.getX() >= node.getX()) {
				c1 = this.getX() - node.getX();
			} else {
				c1 = node.getX() - this.getX();
			}
			
			if(this.getY() >= node.getY()) {
				c2 = this.getY() - node.getY();
			} else {
				c2 = node.getY() - this.getY();
			}
				
			return Math.sqrt(Math.pow(c1, 2) + Math.pow(c2, 2));
		}
		
		List<Node> getNeighboors(Maze maze) {
			List<Coordinate> resCoordinate = this.coordinate.getAllNeighboors(maze);
			List<Node> resNode = new LinkedList<Node>();
			for(Coordinate c : resCoordinate) {
				resNode.add(new Node(c.getX(), c.getY(), 0, 0, 0));
			}
			return resNode;
		}
		
	}
	
	private class NodeComparator implements Comparator<Node> {
		
		@Override
		public int compare(Node node1, Node node2) {
			return Double.compare(node1.totalCost, node2.totalCost);
		}
		
	}
	
	@Override
	public void run(Maze maze, int xStart, int yStart, int xFinal, int yFinal, int velocity) throws InvalidCoordinatesException {
		final Node finalNode = new Node(xFinal, yFinal, 0, 0, 0);
		
		Queue<Node> openList = new PriorityQueue<Node>(1,new NodeComparator());
		HashMap<Coordinate,Node> openMap = new HashMap<Coordinate, Node>();		
		
		List<Node> closedList = new LinkedList<Node>();
		HashMap<Coordinate,Node> closedMap = new HashMap<Coordinate, Node>();	
		
		Node initialNode = new Node(xStart, yStart, 0, 0, 0);
		openList.add(initialNode);
		openMap.put(initialNode.coordinate, initialNode);
		
		while(!openList.isEmpty()) {
			Node examiningNode = openList.remove();
			openMap.remove(examiningNode.coordinate);
			
			RobotController.observe(5, maze.getPosPhoto(examiningNode.getX(), examiningNode.getY()));
			maze.setPos(examiningNode.getX(), examiningNode.getY(), Maze.VISITED_CHAR);
			
			List<Node> neighboors = examiningNode.getNeighboors(maze);
			for(Node neighboor: neighboors) {
				
				if(neighboor.getX() == xFinal && neighboor.getY() == yFinal) {
					maze.setPos(neighboor.getX(), neighboor.getY(), Maze.VISITED_CHAR);
					return;
				}
				
				neighboor.costToArrive = examiningNode.costToArrive + neighboor.linearDistance(examiningNode);
				neighboor.costToGoal = finalNode.linearDistance(neighboor);
				neighboor.totalCost = neighboor.costToArrive + neighboor.costToGoal;
				
				Node nOpen = openMap.get(new Coordinate(neighboor.getX(),neighboor.getY()));
				if(nOpen != null && nOpen.getX() == neighboor.getX() && nOpen.getY() == neighboor.getY() && nOpen.totalCost < neighboor.totalCost) {
					continue;
				}

				
				Node nClose = closedMap.get(new Coordinate(neighboor.getX(),neighboor.getY()));
				if(nClose != null && nClose.getX() == neighboor.getX() && nClose.getY() == neighboor.getY() && nClose.totalCost < neighboor.totalCost) {
					continue;
				}
				
				openMap.put(neighboor.coordinate, neighboor);
				openList.add(neighboor);
			}
			closedMap.put(examiningNode.coordinate, examiningNode);
			closedList.add(examiningNode);
			
			RobotController.run(velocity);
		}
	}

}
