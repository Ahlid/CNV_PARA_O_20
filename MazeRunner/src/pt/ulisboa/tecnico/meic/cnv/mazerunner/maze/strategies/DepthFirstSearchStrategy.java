package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.RobotController;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidCoordinatesException;

public class DepthFirstSearchStrategy extends MazeRunningStrategy {

	private boolean solved = false;
	
	@Override
	public void run(Maze maze, int xStart, int yStart, int xFinal, int yFinal, int velocity) throws InvalidCoordinatesException  {
		solveAux(maze, xStart, yStart, xFinal, yFinal, velocity);
	}
	
	private void solveAux(Maze maze, int x, int y, int xFinal, int yFinal, int velocity) throws InvalidCoordinatesException {
		if (x == -1 || y == -1 || x == maze.getWidth() || y == maze.getHeight()) return;
        if (solved || (maze.getPos(x, y) == Maze.VISITED_CHAR)) return;
        
        RobotController.observe(1, maze.getPosPhoto(x, y));
        
        maze.setPos(x, y, Maze.VISITED_CHAR);
        
        RobotController.run(velocity);
        
        if (x == xFinal && y == yFinal) {
        	solved = true;
        }
        
        if ((y+1 < maze.getHeight()) && !maze.isWall(x,y+1)) solveAux(maze, x, y + 1,xFinal,yFinal, velocity);
        if ((x+1 < maze.getWidth()) && !maze.isWall(x+1,y)) solveAux(maze, x + 1, y,xFinal,yFinal, velocity);
        if ((y-1 >= 0) && !maze.isWall(x,y-1)) solveAux(maze, x, y - 1,xFinal,yFinal, velocity);
        if ((x-1 >= 0) && !maze.isWall(x-1,y)) solveAux(maze, x - 1, y,xFinal,yFinal, velocity);

        if (solved) return;
        
        RobotController.run(velocity);
	}

}
