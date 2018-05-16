package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies.datastructure;

import java.util.LinkedList;
import java.util.List;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;

public class Coordinate {

	private final int x, y;
	
	public Coordinate(int x, int y) { this.x = x; this.y = y;};
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public List<Coordinate> getAllNeighboors(Maze maze){
		List<Coordinate> neighboors = new LinkedList<Coordinate>();
		if(maze.isUnvisitedPassage(x-1,y) || maze.isVisitedPassage(x-1,y)) {
			neighboors.add(new Coordinate(x-1, y));
		} 
		if(maze.isUnvisitedPassage(x+1,y) || maze.isVisitedPassage(x+1,y)){
			neighboors.add(new Coordinate(x+1, y));
		} 
		if(maze.isUnvisitedPassage(x,y-1) || maze.isVisitedPassage(x,y-1)){
			neighboors.add(new Coordinate(x, y-1));
		} 
		if(maze.isUnvisitedPassage(x,y+1) || maze.isVisitedPassage(x,y+1)){
			neighboors.add(new Coordinate(x, y+1));
		}
		return neighboors;
	}
	
	public List<Coordinate> getUnvisitedNeighboors(Maze maze){
		List<Coordinate> neighboors = new LinkedList<Coordinate>();
		if(maze.isUnvisitedPassage(x-1,y)) {
			neighboors.add(new Coordinate(x-1, y));
		} 
		if(maze.isUnvisitedPassage(x+1,y)){
			neighboors.add(new Coordinate(x+1, y));
		} 
		if(maze.isUnvisitedPassage(x,y-1)){
			neighboors.add(new Coordinate(x, y-1));
		} 
		if(maze.isUnvisitedPassage(x,y+1)){
			neighboors.add(new Coordinate(x, y+1));
		}
		return neighboors;
	}
	
	@Override
	public boolean equals(Object obj) {
		Coordinate c = (Coordinate) obj;
		if(c == null) {
			return false;
		}
		return c.x == this.x && c.y == this.y;
	}
	
	@Override
	public int hashCode() {
		return x-y;
	}
	
}
