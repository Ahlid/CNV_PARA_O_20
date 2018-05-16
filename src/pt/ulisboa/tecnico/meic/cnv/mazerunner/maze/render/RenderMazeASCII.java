package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.render;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;

public class RenderMazeASCII implements RenderMaze {

	@Override
	public String render(Maze maze, int velocity) {
		return maze.toString();
	}

}
