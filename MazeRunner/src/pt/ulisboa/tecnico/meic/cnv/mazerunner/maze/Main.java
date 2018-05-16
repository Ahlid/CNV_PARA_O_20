package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.CantGenerateOutputFileException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.CantReadMazeInputFileException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidCoordinatesException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidMazeRunningStrategyException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.render.RenderMaze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.render.RenderMazeHTMLClientCanvas;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies.FactoryMazeRunningStrategies;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies.MazeRunningStrategy;

public class Main {

	/**
	 * Must have arguments:
	 * 1 xStart: X position where we start
	 * 2 yStart: y position where we start
	 * 3 xFinal: x position where we finish
	 * 4 yFinal: y position where we finish
	 * 5 velocity: velocity that we can get running in the maze
	 * 6 strategy: the strategy to traverse the maze {dfs, bfs, astar}
	 * 7 mazeFile: The file path that represents the maze to solve
	 * 8 mazeSolvedFile: The file path to where the solved maze is printed
	 * @throws InvalidMazeRunningStrategyException: Invalid strategy (dfs, bfs, astar)
	 * @throws InvalidCoordinatesException: Probably trying to start/end the maze in a wall block
	 * @throws CantGenerateOutputFileException: Problems generating output file
	 * @throws CantReadMazeInputFileException: Problems reading the maze input file
	 */
	public static void main(String[] args) throws InvalidMazeRunningStrategyException,
			InvalidCoordinatesException, CantGenerateOutputFileException, CantReadMazeInputFileException {
		if(args.length < 8) {
			throw new IllegalArgumentException("InsuficientArguments - The maze runners do not have enough information to solve the maze");
		}
		
		int xStart, yStart, xFinal, yFinal, velocity;
		try {
			xStart = Integer.parseInt(args[0]);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Arg %d: xStart argument must be a number", 0));
		}
		try {
			yStart = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Arg %d: yStart argument must be a number", 1));
		}
		try {
			xFinal = Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Arg %d: xFinal argument must be a number", 2));
		}
		try {
			yFinal = Integer.parseInt(args[3]);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Arg %d: yFinal argument must be a number", 3));
		}
		try {
			velocity = Integer.parseInt(args[4]);
			if(velocity < 1 || velocity > 100) {
				throw new IllegalArgumentException(String.format("Arg %d: velocity argument must be between 1 and 100", 4));
			}
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Arg %d: velocity argument must be a number", 4));
		}
		MazeRunningStrategy strategy = FactoryMazeRunningStrategies.CreateMazeRunningStrategy(args[5]);
		String mazeFile = args[6];
		String mazeSolvedFile = args[7];
        Maze maze = null;
        
        // Read the maze from the file
        try {
           FileInputStream fileIn = new FileInputStream(mazeFile);
           ObjectInputStream in = new ObjectInputStream(fileIn);
           maze = (Maze) in.readObject();
           in.close();
           fileIn.close();
        } catch (IOException i) {
           i.printStackTrace();
           throw new CantReadMazeInputFileException("Problems reading " + mazeFile +" input file!");
        } catch (ClassNotFoundException c) {
           System.out.println("Maze class not found -> Dark stuff is happening...");
           c.printStackTrace();
           return;
        }
        
        // Solve the maze.
        strategy.solve(maze, xStart, yStart, xFinal, yFinal, velocity);
       
        // Choose the way to render the maze and rendered it
        RenderMaze renderMaze = new RenderMazeHTMLClientCanvas();
        String mazeRendered = renderMaze.render(maze, velocity);
        		
        // Write the maze solved to the output mazeOutputFile
        List<String> lines = Arrays.asList(mazeRendered);
        Path file = Paths.get(mazeSolvedFile);
        try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new CantGenerateOutputFileException("Problems writing to " + mazeSolvedFile + " output file!");
		}
	}
	
}
