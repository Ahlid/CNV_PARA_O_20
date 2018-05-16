package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.RobotController;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidCoordinatesException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies.datastructure.Coordinate;

public class BreadthFirstSearchStrategy extends MazeRunningStrategy {
	
	@Override
	public void run(Maze maze, int xStart, int yStart, int xFinal, int yFinal, int velocity) 
			throws InvalidCoordinatesException  {
		
		Queue<Coordinate> queue = new LinkedBlockingQueue<Coordinate>();
		queue.add(new Coordinate(xStart, yStart));
		
		while(!queue.isEmpty()) {
			Coordinate c = queue.remove();
			maze.setPos(c.getX(), c.getY(), Maze.VISITED_CHAR);
			
			RobotController.observe(3, maze.getPosPhoto(c.getX(), c.getY()));
			
			if(c.getX() == xFinal && c.getY() == yFinal) {
				return;
			} else {
				List<Coordinate> neighboors = c.getUnvisitedNeighboors(maze);
				for (Coordinate local : neighboors) {
					queue.add(local);
				}
			}
			
			RobotController.run(velocity);
		}
	}
	
}
