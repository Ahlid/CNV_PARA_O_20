/**
 * Canvas Render for the Maze Runner at the Cloud
 */

// Highly coupled with the output file produced by the MazeRunner
const PASSAGE_CHAR = ' ';
const WALL_CHAR = '#';
const BEDROCK_CHAR = '@';
const INITIAL_CHAR = 'I';
const FINAL_CHAR = 'X';
const VISITED_CHAR = ':';
const NEWLINE_CHAR = '\n';

const POSITION_SQUARE_SIZE = 1

function renderSolvedMaze(positionSize) {
	var velocity = parseInt(document.getElementsByTagName("META")[2].content);
	var canvas = document.getElementById("solvedMaze");
	var ctx = canvas.getContext("2d");
	
	var mazeString = document.getElementById("maze").innerHTML;
	var x = 0;
	var y = 0;
	
	for(var i = 0; i < mazeString.length; i++){
		if(mazeString.charAt(i) == WALL_CHAR) {		
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = "brown";
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == VISITED_CHAR) {
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = "green";
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == PASSAGE_CHAR) {
			
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == BEDROCK_CHAR) {
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = "black";
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == FINAL_CHAR) {
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = "orange";
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == INITIAL_CHAR) {
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = "red";
			ctx.fill();
			ctx.closePath()
			/*
			if(velocity >= 75) {
				
			}
			else if(velocity >= 50 && velocity < 75) {
				
			}
			else if(velocity >= 25 && velocity < 50) {
				
			}
			else if(velocity >= 1 && velocity < 25) {
				
			}
			*/
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == NEWLINE_CHAR) {
			x = 0;
			y += positionSize;
			continue;
		}
	}
	
	document.body.removeChild(document.body.childNodes[0]);
}

function renderResolution(width, height){
	return POSITION_SQUARE_SIZE;
}

window.onload = function() {
	var width = parseInt(document.getElementsByTagName("META")[0].content);
	var height = parseInt(document.getElementsByTagName("META")[1].content);
	var positionSize = renderResolution(width,height);
	var canvasWidth = width * positionSize + (2 * positionSize);
	var canvasHeight = height * positionSize + (2 * positionSize);
	
	var svgCanvas = document.createElement("canvas");
	svgCanvas.setAttribute("id", "solvedMaze");
	svgCanvas.setAttribute("width",canvasWidth);
	svgCanvas.setAttribute("height",canvasHeight);
	
	document.body.appendChild(svgCanvas);
	
	renderSolvedMaze(positionSize)
	
};
