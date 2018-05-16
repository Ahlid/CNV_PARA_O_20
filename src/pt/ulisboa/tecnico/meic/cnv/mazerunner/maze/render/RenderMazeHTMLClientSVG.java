package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.render;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;

public class RenderMazeHTMLClientSVG implements RenderMaze {

	private final String RENDER_JS_URL = "https://grupos.ist.utl.pt/~meic-cnv.daemon/project/renderSVG.js";
	private final String RENDER_GIF_URL = "https://grupos.ist.utl.pt/~meic-cnv.daemon/project/Rendering.gif";
	
	@Override
	public String render(Maze maze, int velocity) {
		final StringBuffer b = new StringBuffer();
		b.append("<!DOCTYPE html>"
				+ "<head>"
				+   "<meta name=\"width\" content=\"" + maze.getWidth() + "\">"
				+   "<meta name=\"height\" content=\"" + maze.getHeight() + "\">"
				+   "<meta name=\"velocity\" content=\"" + velocity + "\">"
				+	"<script src=\"" + RENDER_JS_URL + "\"></script>"
				+ "</head>"
				+ "<style>"
				+ "div {" 
				+   "width: 400px;"
				+   "height: 500px;"
				+   "position: absolute;" 
				+   "top:0;"
				+   "bottom: 0;"
				+   "left: 0;"
				+   "right: 0;"
				+   "margin: auto;"
				+ "}"
				+ "</style>"
				+ "<html>"
				+ 	"<body>"
				+ 		"<div id=\"loading\">" 
				+ 			"<h3>Runners have succeeded, rendering escape path...</h3>"
				+ 			"<iframe src=\"" + RENDER_GIF_URL + "\" width=\"400\" height=\"400\">" 
				+ 			"</iframe>"
				+ "		</div>"
				+ "		<pre id=\"maze\" hidden>");
		b.append(maze.toString());
		b.append(	"</pre> </body>"
				+ "</html>");
		return b.toString();
	}

}
