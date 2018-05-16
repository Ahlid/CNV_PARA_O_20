package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidMazeRunningStrategyException;

public abstract class FactoryMazeRunningStrategies {

	public static MazeRunningStrategy CreateMazeRunningStrategy(String strategy) throws InvalidMazeRunningStrategyException {
		switch (strategy.toLowerCase()) {
		case "dfs":
			return new DepthFirstSearchStrategy();
		case "bfs":
			return new BreadthFirstSearchStrategy();
		case "astar":
			return new AStarStrategy();
		default:
			throw new InvalidMazeRunningStrategyException("The runners don not know how to run using the strategy: " + strategy);
		}
	}
	
}
