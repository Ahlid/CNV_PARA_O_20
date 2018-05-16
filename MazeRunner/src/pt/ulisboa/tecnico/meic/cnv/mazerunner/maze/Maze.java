package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidCoordinatesException;

public class Maze implements Serializable {
	
	private class Position implements Serializable {

		private static final long serialVersionUID = -8440798809543247456L;

		private static final int PHOTO_SIZE_BYTES = 256;
		
		private char content;
		private transient byte[] photo;
		
		public Position(char state) {
			this.content = state;
			photo = new byte[PHOTO_SIZE_BYTES];
		}
		
		public char getContent() {
			return this.content;
		}
		
		public void setContent(char content) {
			this.content = content;
		}
		
		public byte[] getPhoto() {
			return photo.clone();
		}
		
		private void writeObject(ObjectOutputStream stream)
	            throws IOException {
	        stream.writeChar(content);
	    }
		
		private void readObject(ObjectInputStream stream)
	            throws IOException, ClassNotFoundException {
	        this.content = stream.readChar();
	        this.photo = new byte[PHOTO_SIZE_BYTES];
	    }
		
	}
	
	private static final long serialVersionUID = -6912979841262795975L;
	
	public static final char PASSAGE_CHAR = ' ';
	public static final char WALL_CHAR = '#';
	public static final char BEDROCK_CHAR = '@';
	public static final char INITIAL_CHAR = 'I';
	public static final char FINAL_CHAR = 'X';
	public static final char VISITED_CHAR = ':';
	
	private final Position map[][];
	private int width;
	private int height;
	
	public Maze(final int width, final int height){
	    this.width = width;
	    this.height = height;
	    this.map = new Position[width][height];
	    
	    this.generateMaze();
	}
	
	private void generateMaze() {
		final LinkedList<int[]> frontiers = new LinkedList<>();
	    final Random random = new Random(System.currentTimeMillis());
	    int x = random.nextInt(width);
	    int y = random.nextInt(height);
	    frontiers.add(new int[]{x,y,x,y});
	
	    //Initialize all the map with walls before generate the maze.
	    for(int xi = 0; xi < width; xi++) {
			for(int yi = 0; yi < height; yi++) {
				map[xi][yi] = new Position(WALL_CHAR);
			}
		}
	    
	    //Maze RANDOMLY generated
	    while (!frontiers.isEmpty()) {
	        final int[] f = frontiers.remove(random.nextInt(frontiers.size()));
	        x = f[2];
	        y = f[3];
	        if (map[x][y].getContent() == WALL_CHAR) {
	        	map[x][y].setContent(PASSAGE_CHAR);
	        	map[f[0]][f[1]].setContent(PASSAGE_CHAR);
	            
	            if (x >= 2 && map[x-2][y].getContent() == WALL_CHAR)
	                frontiers.add( new int[]{x-1,y,x-2,y});
	            if (y >= 2 && map[x][y-2].getContent() == WALL_CHAR)
	                frontiers.add( new int[]{x,y-1,x,y-2});
	            if (x < width-2 && map[x+2][y].getContent() == WALL_CHAR)
	                frontiers.add( new int[]{x+1,y,x+2,y});
	            if (y < height-2 && map[x][y+2].getContent() == WALL_CHAR)
	                frontiers.add( new int[]{x,y+1,x,y+2});
	        }
	    }
	}
	
	public void setPos(int x, int y, char symbol) throws InvalidCoordinatesException {
		if(x < 0 || y < 0 || x >= width || y >= height) {
			throw new InvalidCoordinatesException("Position (" + x + "," + y +") out of bounds");
		}
		map[x][y].setContent(symbol);
	}
	
	public char getPos(int x, int y) throws InvalidCoordinatesException {
		if(x < 0 || y < 0 || x >= width || y >= height) {
			throw new InvalidCoordinatesException("Position (" + x + "," + y +") out of bounds");
		}
		return map[x][y].getContent();
	}

	public byte[] getPosPhoto(int x, int y) throws InvalidCoordinatesException {
		if(x < 0 || y < 0 || x >= width || y >= height) {
			throw new InvalidCoordinatesException("Position (" + x + "," + y +") out of bounds");
		}
		return map[x][y].getPhoto();
	}
	
	public boolean isWall(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height) {
			return true;
		}
		return map[x][y].getContent() == WALL_CHAR;
	}
	
	public boolean isUnvisitedPassage(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height) {
			return false;
		}
		return map[x][y].getContent() == PASSAGE_CHAR;
	}
	
	public boolean isVisitedPassage(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height) {
			return false;
		}
		return map[x][y].getContent() == VISITED_CHAR;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	/**
	 * Returns a string form of the current state of the maze.
	 * ASCIII
	 */
	@Override
	public String toString() {
	    final StringBuffer b = new StringBuffer();
	    for (int x = 0; x < width + 2; x++) {
	        b.append(BEDROCK_CHAR);
	    }
	    b.append('\n');
	    for (int y = 0; y < height; y++) {
	        b.append(BEDROCK_CHAR);
	        for (int x = 0; x < width; x++) {
	            b.append(map[x][y].getContent());
	        }
	        b.append(BEDROCK_CHAR);
	        b.append('\n');
	    }
	    for (int x = 0; x < width + 2; x++) {
	        b.append(BEDROCK_CHAR);
	    }
	    b.append('\n');
	    return b.toString();
	}
	
}
