# Maze Runner

Maze solver with depth first search, breadth first search and A*.
Maze solver operates as a partially simulated compute intensive workload with some observable graphic output.
It serves as the workload to drive the project for the Cloud Computing and Virtualization course @ IST 2018

## Getting Started

The MazeRunner is a regular Java application.

How to run the application? (CLI example)

java pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Main <x_start> <y_start> <x_final> <y_final> <velocity> <strategy> <maze_file_input> <maze_file_output_html>

e.g. java pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Main 3 9 78 89 50 astar Maze100.maze Maze100.html

Notes: 
- You need to Update the CLASSPATH accordingly so that the JVM finds the classes (i.e., there should subdirectories corresponding to the namespaces pt/ulisboa/ etc.)
- The file Maze100.maze should be in the same directory where you launch the the Maze Runner application

Input parameter specifications and restrictions:
- (0,0) is in top-left corner. The outside wall (grey wall) does not count to determine the position.
- velocity: [1-100]
- strategy: {bfs, dfs, astar}
