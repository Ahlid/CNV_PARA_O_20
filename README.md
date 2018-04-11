# CNV_PARA_O_20
#

Compile everything: Make All

Run web server: java -cp bin/:lib/log4j-1.2.17.jar pt.ulisboa.tecnico.meic.cnv.httpserver.WebServer

Run inst tool: make run_inst inputClass=bin/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Maze.class

Run maze runner: java -cp bin/:lib/log4j-1.2.17.jar pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Main 3 9 28 39 50 astar Maze50.maze Maze50.html
