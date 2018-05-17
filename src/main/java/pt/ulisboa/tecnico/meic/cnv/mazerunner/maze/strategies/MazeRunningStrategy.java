package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidCoordinatesException;

public abstract class MazeRunningStrategy {

	public final void solve(Maze maze, int xStart, int yStart, int xFinal, int yFinal, int velocity) 
			throws InvalidCoordinatesException  {
		boolean isStartWall = maze.isWall(xStart, yStart);
		boolean isFinalWall = maze.isWall(xFinal, yFinal);
		if(isStartWall || isFinalWall) {
			String exceptionMessage = "";
			if(isStartWall) {
				exceptionMessage += "Start Point Invalid, suggestions: ";
				for(int x = xStart - 1; x <= xStart + 1; x++) {
					for(int y = yStart - 1; y <= yStart + 1; y++) {
						if(x == xStart && y == yStart) {continue;}
						if(!maze.isWall(x, y)) { exceptionMessage += "(" + x +"," + y + ") | "; }
					}
				}
				exceptionMessage += "\n";
			}
			if(isFinalWall) {
				exceptionMessage += "Final Point Invalid, suggestions: ";
				for(int x = xFinal - 1; x <= xFinal + 1; x++) {
					for(int y = yFinal - 1; y <= yFinal + 1; y++) {
						if(x == xFinal && y == yFinal) {continue;}
						if(!maze.isWall(x, y)) { exceptionMessage += "(" + x +"," + y + ") | "; }
					}
				}
			}
			throw new InvalidCoordinatesException(exceptionMessage);
		}
		
		run(maze, xStart, yStart, xFinal, yFinal, velocity);
		
		maze.setPos(xStart, yStart, Maze.INITIAL_CHAR);
		maze.setPos(xFinal, yFinal, Maze.FINAL_CHAR);
	}
	
	public abstract void run(Maze maze, int xStart, int yStart, int xFinal, int yFinal, int velocity) 
			throws InvalidCoordinatesException ;
	
}
