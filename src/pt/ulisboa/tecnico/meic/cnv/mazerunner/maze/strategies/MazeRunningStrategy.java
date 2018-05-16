package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidCoordinatesException;

public abstract class MazeRunningStrategy {

	public final void solve(Maze maze, int xStart, int yStart, int xFinal, int yFinal, int velocity) 
			throws InvalidCoordinatesException  {
		if(maze.isWall(xStart, yStart) || maze.isWall(xFinal, yFinal)) {
			throw new InvalidCoordinatesException("Trying starting inside a wall or outside the maze!");
		}
		
		run(maze, xStart, yStart, xFinal, yFinal, velocity);
		
		maze.setPos(xStart, yStart, Maze.INITIAL_CHAR);
		maze.setPos(xFinal, yFinal, Maze.FINAL_CHAR);
	}
	
	public abstract void run(Maze maze, int xStart, int yStart, int xFinal, int yFinal, int velocity) 
			throws InvalidCoordinatesException ;
	
}
