/*******************************************************\
|  Mandatory Programming Assignment 02285, Spring 2015  |
|               README for students						|
\*******************************************************/

	* Please go through this README before asking questions about the workings of the server
	* The following describes the various options for starting the server using the provided example clients
	* Inspection of the source code for the example clients may yield useful information regarding the implementation of your own solver
	* The commands below must be executed from the directory containing this README file
	* It is required that the java runtime environment binaries are available in your system path for the commands below to work
	
	Get help about server options and arguments
		$ java -jar server.jar -?
	
	Basic usage (requires a path to a level and a client command):
		$ java -jar server.jar -l levels/MAsimple1.lvl -c "java client.RandomWalkClient"

	By default the server prints a string representation of the current state to the console. To minimize overhead (e.g. when optimizing your solver) this output may be redirected to the null device using:
		Windows: $ java -jar server.jar -l levels/MAsimple1.lvl -c "java client.RandomWalkClient" > NUL
		Linux: $ java -jar server.jar -l levels/MAsimple1.lvl -c "java client.RandomWalkClient" > /dev/null 
	Note that both messages from the client (i.e. your solver) and important server messages (including success) both use 'standard error' for printing to console, hence they bypass this redirection.
		
	The server may be started with a more graphical appeal using the -g option:
		Windows: $ java -jar server.jar -l levels/MAbispebjerg.lvl -c "java client.RandomWalkClient" -g
		Linux: $ java -Dsun.java2d.opengl=true -jar server.jar -l levels/MAbispebjerg.lvl -c "java client.RandomWalkClient" -g
	
	*Important* The -Dsun.java2d.opengl=true option enables OpenGL hardware acceleration (see http://docs.oracle.com/javase/6/docs/technotes/guides/2d/flags.html). This option may help if you experience low framerates or server instability when rendering. Windows defaults to Direct3D.
			
	Passing a value to the -g option specifies the initial settings of the 'Controls panel'.
		Windows: $ java -jar server.jar -l levels/SAanagram.lvl -c "java client.RandomWalkClient" -g 250
		Linux: $ java -Dsun.java2d.opengl=true -jar server.jar -l levels/SAanagram.lvl -c "java client.RandomWalkClient" -g 250
		
	When this value (-g) is below 30, the GUI is rendered as fast as possible. The -p option starts the server paused (actions sent to the server are processed once unpaused).
		Windows: $ java -jar server.jar -l levels/MAsimple2.lvl -c "java client.RandomWalkClient" -g 0 -p
		Linux: $ java -Dsun.java2d.opengl=true -jar server.jar -l levels/MAsimple2.lvl -c "java client.RandomWalkClient" -g 0 -p

	To test the effect of actions you can try the user controlled client: 
		Windows: $ java -jar server.jar -l levels/SAsokobanLevel96.lvl -c "java client.GuiClient" -g 200
		Linux: $ java -Dsun.java2d.opengl=true -jar server.jar -l levels/SAsokobanLevel96.lvl -c "java client.GuiClient" -g 200
	Which simply requires a change of client.
	
	GuiClient works by creating a joint action of identical individual actions for each agent on the level; e.g. clicking Move(W) on a level with 3 agents sends [Move(W),Move(W),Move(W)].
	For each argument passed to GuiClient, a custom text field is created with that joint action; e.g.:
		Windows: $ java -jar server.jar -l levels/MAsimple3.lvl -c "java client.GuiClient [NoOp,Push(E,E)] [Push(E,E),Push(E,N)] [Push(E,E),Pull(W,N)] [Pull(W,E),NoOp]" -g 100
		Linux: $ java -Dsun.java2d.opengl=true -jar server.jar -l levels/MAsimple3.lvl -c "java client.GuiClient [NoOp,Push(E,E)] [Push(E,E),Push(E,N)] [Push(E,E),Pull(W,N)] [Pull(W,E),NoOp]" -g 100
	fills the custom commands upon startup.
	
	Note that it is the parameter of the "-c"/"--client" option that you must change to execute your own implementation of a solver.
	To try out the included ruby random walk client (requires a ruby intepreter in your environment):
		Windows: $ java -jar server.jar -l levels/MApacman.lvl -c "ruby client/random_agent.rb 3" -g -p
		Linux: $ java -Dsun.java2d.opengl=true -jar server.jar -l levels/MApacman.lvl -c "ruby client/random_agent.rb 3" -g -p
	The argument passed to random_agent.rb is the number of agents on the level
	
	Finally the server may be started with the timeout option. The option may be useful when testing your planner with regards to the competetion.
		Windows: $ java -jar server.jar -l levels/MAsimple2.lvl -c "java client.RandomWalkClient" -t 300 > NUL
		Linux: $ java -jar server.jar -l levels/MAsimple1.lvl -c "java client.RandomWalkClient" -t 300 > /dev/null
	The argument of -t is the number of seconds before the server timeouts; i.e. 5 minutes in this case.
