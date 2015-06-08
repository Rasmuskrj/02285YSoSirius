package client;

import java.io.IOException;
import java.util.*;


import client.Command.dir;
import client.Command.type;

public class Node {

	public static HashMap<Coordinate, Boolean> walls = new HashMap<Coordinate, Boolean>();
    private static HashMap<Coordinate, Goal> goalsByCoordinate = new HashMap<Coordinate, Goal>();
    private static HashMap<Character, Goal> goalsByID = new HashMap<Character, Goal>();
    private HashMap<Coordinate, Box> boxesByCoordinate = new HashMap<Coordinate, Box>();
    private HashMap<Character, Box> boxesByID = new HashMap<Character, Box>();
	private PriorityQueue<Box> easiestBoxes;
	public static HashMap<Goal, HashMap<Coordinate, Integer>> goalDistance = 
				new HashMap<Goal, HashMap<Coordinate, Integer>>();

	public Agent thisAgent = null;

    
    public static int totalRows;
    public static int totalColumns;
    
    private int f;
    
    public List<Agent> agents = new ArrayList<Agent>();

	public Node parent;
	public Command action;

	private int g;

	public Node(Node parent) {
		this.parent = parent;
		this.boxesByCoordinate = new HashMap<Coordinate, Box>();
		if (parent == null) {
			g = 0;
		} else {
			g = parent.g() + 1;
		}
	}
	
	public static void computeGoalDistance() {
		for (Goal goal : Node.getGoalsByCoordinate().values()) {
			HashMap<Coordinate, Integer> distanceMap = new HashMap<Coordinate, Integer>();
			LinkedList<Coordinate> frontier = new LinkedList<Coordinate>();
			HashSet<Coordinate> frontierHash = new HashSet<Coordinate>();
			// initially put only the goal in the distance map
			distanceMap.put(goal.getCoordinate(), 0);
			// and the goal's neighbours (4-vicinity) in the frontier
			for (Coordinate coordinate : goal.getCoordinate().get4VicinityCoordinates()) {
				if (Node.walls.get(coordinate) == null && 
						coordinate.getRow() > -1 && coordinate.getRow() < Node.totalRows &&
						coordinate.getColumn() > -1 && coordinate.getColumn() < Node.totalColumns) {
					frontier.add(coordinate);
					frontierHash.add(coordinate);
				}
			}
			// then in each loop move elements in frontier to distanceMap (take min distance)
			// and add their neighbours to frontier
			while (!frontier.isEmpty()) {
				Coordinate coordinate = frontier.poll();
				frontierHash.remove(coordinate);
				Integer minDistance = Integer.MAX_VALUE;
				for (Coordinate neighbour : coordinate.get4VicinityCoordinates()) {
					if (distanceMap.containsKey(neighbour) && distanceMap.get(neighbour) < minDistance) {
						minDistance = distanceMap.get(neighbour);
					}
				}
				distanceMap.put(coordinate, minDistance+1);
				for (Coordinate neighbour : coordinate.get4VicinityCoordinates()) {
					if (Node.walls.get(neighbour) == null && 
							neighbour.getRow() > -1 && neighbour.getRow() < Node.totalRows &&
							neighbour.getColumn() > -1 && neighbour.getColumn() < Node.totalColumns &&
							!distanceMap.containsKey(neighbour) &&
							!frontierHash.contains(neighbour)) {
						frontier.add(neighbour);
						frontierHash.add(neighbour);
					}
				}
			}
			Node.goalDistance.put(goal, distanceMap);
		}
		
	}
	
	public HashMap<Coordinate, Box> getBoxesByCoordinate() {
        return boxesByCoordinate;
    }
    
    public HashMap<Character, Box> getBoxesByID() {
        return boxesByID;
    }


    public void addBox(Box box) {
        this.boxesByCoordinate.put(box.getCoordinate(), box);
        this.boxesByID.put(box.getLetter(), box);
    }
    
    public static HashMap<Coordinate, Goal> getGoalsByCoordinate() {
        return Node.goalsByCoordinate;
    }
    
    public static HashMap<Character, Goal> getGoalsByID() {
        return Node.goalsByID;
    }
    
    public static void addGoal(Goal goal) {
    	Node.goalsByCoordinate.put(goal.getCoordinate(), goal);
        Node.goalsByID.put(goal.getLetter(), goal);
    }

	public List<Agent> getAgents() {
		return agents;
	}

	public Agent getAgentById(char id){
		for(Agent agent : agents){
			if(agent.getId() == id){
				return agent;
			}
		}
		return null;
	}

	public void setAgents(ArrayList<Agent> agents) {
		this.agents = agents;
	}

	public int g() {
		return g;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}

	public boolean isGoalState() {
		if(!thisAgent.isClearMode()) {
			Goal goal = thisAgent.getCurrentSubGoal();
			Box box = boxesByCoordinate.get(goal.getCoordinate());
			if (box != null && box.getLetter() == Character.toUpperCase(goal.getLetter())) {
				box.setInFinalPosition(true);
				return true;
			}
		} else {
			for(Coordinate cord : thisAgent.getClearCords()){
				Box box = boxesByCoordinate.get(cord);
				if(thisAgent.getCoordinate().equals(cord)){
					return false;
				} else if(box != null && box.getColor() != null && box.getColor().equals(thisAgent.getColor())){
					return false;
				}
			}
			return true;
		}

		return false;
	}

	public static boolean isBoxInTargetGoalCell(Box box){
		for(Coordinate goalCord : Node.getGoalsByCoordinate().keySet()){
			if(box.getLetter() == Character.toUpperCase(Node.getGoalsByCoordinate().get(goalCord).getLetter()) && box.getCoordinate().equals(goalCord)){
				return true;
			}
		}
		return false;
	}

	public static void setGoalsPriority(){
		for(Goal goal : Node.getGoalsByCoordinate().values()){
			//Goal priority is initialized as 0 so if it is higher we can assume that the priority has already been set.
			if(goal.getPriority() < 1){
				goal.setPriority(setSingleGoalPriority(goal));
			}
		}
	}

	//Function used to recursively set goal priority
	public static int setSingleGoalPriority(Goal goal){
		goal.setIsBeingPrioritized(true);
		int bufferZoneOffset = 10;
		//Get coordinates for all adjacent cells, and put them in list
		Coordinate nCord = new Coordinate(goal.getCoordinate().getRow() - 1,goal.getCoordinate().getColumn());
		Coordinate wCord = new Coordinate(goal.getCoordinate().getRow(), goal.getCoordinate().getColumn() -1 );
		Coordinate sCord = new Coordinate(goal.getCoordinate().getRow() +1, goal.getCoordinate().getColumn());
		Coordinate eCord = new Coordinate(goal.getCoordinate().getRow(), goal.getCoordinate().getColumn() +1);
		ArrayList<Coordinate> newCords = new ArrayList<>();
		newCords.add(nCord);
		newCords.add(wCord);
		newCords.add(sCord);
		newCords.add(eCord);
		int returnVal = Integer.MAX_VALUE;
		for( Coordinate cord : newCords){
			//The goal is next to a "free" cell meaning that it has neither a wall or another goal cell. Base case for recursive function
			if(Node.walls.get(cord) == null && Node.getGoalsByCoordinate().get(cord) == null){
				//Need to also call the setter here in case this is a recursive call of the function.
				goal.setPriority(1 + bufferZoneOffset);
				return 1 + bufferZoneOffset;
			}
			//The goal cell is next another goal cell in this direction
			else if(Node.getGoalsByCoordinate().get(cord) != null){
				Goal target = Node.getGoalsByCoordinate().get(cord);
				//Skip this goal if it is already in the stack
				if(target.isBeingPrioritized()){
					continue;
				}
				//Goal priority is initialized as 0 so if it is higher we can assume that the priority has already been set.
				if(target.getPriority() < 1) {
					//If priority has not been set, call this function recursively for the adjacent goal cell and add 1.
					int targetPrio = setSingleGoalPriority(target);
					if(targetPrio < goal.getPriority() || goal.getPriority() < 1) {
						goal.setPriority(targetPrio + 1 + bufferZoneOffset);
					}
				} else {
					//else just get the priority and add 1
					int targetPrio = target.getPriority();
					if(targetPrio < goal.getPriority() || goal.getPriority() < 1) {
						goal.setPriority(targetPrio + 1 + bufferZoneOffset);
					}
				}
				//Compare the priority to the priorities found in other directions. Note that a wall will not be able to set the returnVal.
				returnVal = goal.getPriority() < returnVal ? goal.getPriority() : returnVal;
			}
		}
		return returnVal;
	}


	public ArrayList<Node> getExpandedNodes() {
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.every.length);
		for (Command c : Command.every) {
			// Determine applicability of action
			int newAgentRow = thisAgent.getCoordinate().getRow() + dirToRowChange(c.dir1);
			int newAgentCol = thisAgent.getCoordinate().getColumn() + dirToColChange(c.dir1);

			if (c.actType == type.Move) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if (cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.childNode();
					n.action = c;
					n.thisAgent.getCoordinate().setRow(newAgentRow);
					n.thisAgent.getCoordinate().setColumn(newAgentCol);
					expandedNodes.add(n);
				}
			} else if (c.actType == type.Push) {
				// Make sure that there's actually a box to move
				if (boxAt(newAgentRow, newAgentCol)) {
					int newBoxRow = newAgentRow + dirToRowChange(c.dir2);
					int newBoxCol = newAgentCol + dirToColChange(c.dir2);
					// .. and that new cell of box is free
					if (cellIsFree(newBoxRow, newBoxCol)) {
						Node n = this.childNode();
						n.action = c;
						n.thisAgent.getCoordinate().setRow(newAgentRow);
						n.thisAgent.getCoordinate().setColumn(newAgentCol);
						// TODO: eventually refactor with clone()
						Box boxToMove = this.boxesByCoordinate.get(
											new Coordinate(newAgentRow, newAgentCol));
						Box boxToMoveCopy = new Box(boxToMove.getLetter(), boxToMove.getColor(),
													new Coordinate(newBoxRow, newBoxCol), boxToMove.isInFinalPosition());
						n.boxesByCoordinate.put(new Coordinate(newBoxRow, newBoxCol), boxToMoveCopy);
						n.boxesByCoordinate.remove(new Coordinate(newAgentRow, newAgentCol));
						n.boxesByID.remove(boxToMoveCopy.getLetter());
						n.boxesByID.put(boxToMoveCopy.getLetter(), boxToMoveCopy);
						expandedNodes.add(n);
					}
				}
			} else if (c.actType == type.Pull) {
				// Cell is free where agent is going
				if (cellIsFree(newAgentRow, newAgentCol)) {
					int boxRow = this.thisAgent.getCoordinate().getRow() + dirToRowChange(c.dir2);
					int boxCol = this.thisAgent.getCoordinate().getColumn() + dirToColChange(c.dir2);
					// .. and there's a box in "dir2" of the agent
					if (boxAt(boxRow, boxCol)) {
						Node n = this.childNode();
						n.action = c;
						// TODO: eventually refactor with clone()
						Box boxToMove = this.boxesByCoordinate.get(
											new Coordinate(boxRow, boxCol));
						Box boxToMoveCopy = new Box(boxToMove.getLetter(), boxToMove.getColor(),
								new Coordinate(this.thisAgent.getCoordinate().getRow(),
										this.thisAgent.getCoordinate().getColumn()), boxToMove.isInFinalPosition());

						n.boxesByCoordinate.put(new Coordinate(this.thisAgent.getCoordinate().getRow(),
								this.thisAgent.getCoordinate().getColumn()), boxToMoveCopy);
						n.boxesByCoordinate.remove(new Coordinate(boxRow, boxCol));
						n.boxesByID.remove(boxToMoveCopy.getLetter());
						n.boxesByID.put(boxToMoveCopy.getLetter(), boxToMoveCopy);
						n.thisAgent.getCoordinate().setRow(newAgentRow);
						n.thisAgent.getCoordinate().setColumn(newAgentCol);
						expandedNodes.add(n);
					}
				}
			}
		}
		return expandedNodes;
	}

	private boolean cellIsFree(int row, int col) {
		if(thisAgent.isClearMode()) {
			for (Agent agent : agents) {
				if (agent.getId() != thisAgent.getId() && agent.getCoordinate().equals(new Coordinate(row, col))) {
					return false;
				}
			}
		}
		Box box = boxesByCoordinate.get(new Coordinate(row, col));
		boolean noBox;
		if(box == null || box.getColor() != null && !box.getColor().equals(thisAgent.getColor())){
			noBox = true;
		} else {
			noBox = false;
		}

		return (Node.walls.get(new Coordinate(row, col)) == null
					&& noBox);
	}

	private boolean boxAt(int row, int col) {
		Box box = this.boxesByCoordinate.get(new Coordinate(row, col));
		if(box == null){
			return false;
		} else {
			if(thisAgent == null || thisAgent.getColor() == null){
				return true;
			} else {
				return box.getColor().equals(thisAgent.getColor());
			}
		}
	}

	public int dirToRowChange(dir d) {
		return (d == dir.S ? 1 : (d == dir.N ? -1 : 0)); // South is down one row (1), north is up one row (-1)
	}

	public int dirToColChange(dir d) {
		return (d == dir.E ? 1 : (d == dir.W ? -1 : 0)); // East is left one column (1), west is right one column (-1)
	}

	public ArrayList<Coordinate> commandToCoordinates(Coordinate startPos, Command command){
		ArrayList<Coordinate> retArr = new ArrayList<>();

		Coordinate newAgentPos = new Coordinate(startPos.getRow() + dirToRowChange(command.dir1), startPos.getColumn() + dirToColChange(command.dir1));
		retArr.add(newAgentPos);

		if(command.actType == type.Push){
			Coordinate newBoxPos = new Coordinate(newAgentPos.getRow() + dirToRowChange(command.dir2), newAgentPos.getColumn() + dirToColChange(command.dir2));
			retArr.add(newBoxPos);
		} else if(command.actType == type.Pull){
			Coordinate newBoxPos = new Coordinate(startPos.getRow() + dirToRowChange(command.dir2), startPos.getColumn() + dirToColChange(command.dir2));
			retArr.add(newBoxPos);
		}
		return retArr;
	}

	public void setParent(Node parent){
		if(parent == null){
			this.parent = null;
			g = 0;
		} else {
			this.parent = parent;
			this.g = parent.g;
		}
	}

	private Node childNode() {
		Node copy = new Node(this);
		for (Coordinate key : this.boxesByCoordinate.keySet()) {
			copy.boxesByCoordinate.put(key, this.boxesByCoordinate.get(key));
			copy.boxesByID.put(this.boxesByCoordinate.get(key).getLetter(), this.boxesByCoordinate.get(key));
		}
		for (Agent agent : this.agents) {
			copy.agents.add(agent.clone());
		}
		copy.thisAgent = this.thisAgent.clone();
		return copy;
	}

	public Node getCopy(){
		Node copy = new Node(this);
		copy.setParent(parent);
		for(Coordinate key : this.getBoxesByCoordinate().keySet()) {
			copy.boxesByCoordinate.put(key, this.boxesByCoordinate.get(key));
			copy.boxesByID.put(this.boxesByCoordinate.get(key).getLetter(), this.boxesByCoordinate.get(key));
		}
		for (Agent agent : this.agents){
			copy.agents.add(agent.clone());
		}
		return copy;
	}

	public LinkedList<Node> extractPlan() {
		LinkedList<Node> plan = new LinkedList<Node>();
		Node n = this;
		while(!n.isInitialState()) {
			plan.addFirst(n);
			n = n.parent;
		}
		return plan;
	}

	public void printState() {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<Node.totalRows; i++) {
			for (int j=0; j<Node.totalColumns; j++) {
				Agent cellAgent = null;
				for(Agent agent : agents){
					if(agent.getCoordinate().equals(new Coordinate(i,j))){
						cellAgent = agent;
					}
				}
				if (Node.walls.get(new Coordinate(i, j)) != null) {
					builder.append('+');
				} else if (cellAgent != null){
					builder.append(cellAgent.getId());
				} else if (this.boxesByCoordinate.get(new Coordinate(i, j)) != null) {
					builder.append(this.boxesByCoordinate.get(new Coordinate(i, j)).getLetter());
				} else if (Node.goalsByCoordinate.get(new Coordinate(i, j)) != null) {
					builder.append(Node.goalsByCoordinate.get(new Coordinate(i, j)).getLetter());
				} else {
					builder.append(' ');
				}
			}
			builder.append('\n');
		}
		System.err.print(builder.toString());
		/*try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	public int getF() {
		return f;
	}

	public void setF(int f) {
		this.f = f;
	}

	public boolean changeState(Command[] commands, String[] serverOutput, MultiAgentClient client){
		for(int i = 0; i < commands.length; i++){
			Agent activeAgent = this.getAgentById(Integer.toString(i).charAt(0));
			if(commands[i] != null && !serverOutput[i].equals("false")) {
				int newAgentRow = activeAgent.getCoordinate().getRow() + dirToRowChange(commands[i].dir1);
				int newAgentColumn = activeAgent.getCoordinate().getColumn() + dirToColChange(commands[i].dir1);
				Coordinate newPos = new Coordinate(newAgentRow, newAgentColumn);
				if (commands[i].actType == type.Pull) {
					int boxRow = activeAgent.getCoordinate().getRow() + dirToRowChange(commands[i].dir2);
					int boxCol = activeAgent.getCoordinate().getColumn() + dirToColChange(commands[i].dir2);
					if (boxAt(boxRow, boxCol) && boxesByCoordinate.get(new Coordinate(boxRow, boxCol)).getColor() == null || boxAt(boxRow, boxCol) && boxesByCoordinate.get(new Coordinate(boxRow, boxCol)).getColor().equals(activeAgent.getColor())) {
						Box pullBox = this.boxesByCoordinate.get(new Coordinate(boxRow, boxCol));
						Box pullBoxNew = new Box(pullBox.getLetter(), pullBox.getColor(), activeAgent.getCoordinate());
						boxesByCoordinate.remove(pullBox.getCoordinate());

						if(activeAgent.getCurrentSubGoal() != null && activeAgent.getCurrentSubGoal().getCoordinate().equals(pullBoxNew.getCoordinate()) && !activeAgent.isClearMode()) {
							pullBoxNew.setInFinalPosition(true);
							System.err.println("Set in final position");
						}
						//System.err.println("Agent " + activeAgent.getId() + " moved box from " + boxRow + ", " + boxCol + " to " + activeAgent.getCoordinate().getRow() + ", " + activeAgent.getCoordinate().getColumn());
						activeAgent.setCoordinate(newPos);
						boxesByCoordinate.put(pullBoxNew.getCoordinate(), pullBoxNew);
						for(Goal goal : goalsByCoordinate.values()){
							if(goal.getCoordinate().equals(pullBox.getCoordinate()) && goal.getLetter() == Character.toLowerCase(pullBox.getLetter())){
								if(!client.subGoals.contains(goal)){
									if(goal.getPriority() > 1 ){
										System.err.println("Priority was: " + goal.getPriority());
										goal.setPriority(goal.getPriority() - 1);
									}
									client.subGoals.offer(goal);
									System.err.println("subgoal " + goal.getLetter() + " at " + goal.getCoordinate().toString() + " reinserted in list. Priority: " + goal.getPriority());
								}
							}
						}
					} else if(boxesByCoordinate.get(new Coordinate(boxRow, boxCol)).getColor() == null || boxesByCoordinate.get(new Coordinate(boxRow, boxCol)).getColor().equals(activeAgent.getColor())){
						System.err.println("No box at: " + newPos.toString());
						System.err.println("Client State corrupted");
						return false;
					}

				} else if (commands[i].actType == type.Push) {
					int newBoxRow = newAgentRow + dirToRowChange(commands[i].dir2);
					int newBoxCol = newAgentColumn + dirToColChange(commands[i].dir2);
					if (boxAt(newPos.getRow(), newPos.getColumn()) && boxesByCoordinate.get(newPos).getColor() == null || boxAt(newPos.getRow(), newPos.getColumn()) && boxesByCoordinate.get(newPos).getColor() != null && boxesByCoordinate.get(newPos).getColor().equals(activeAgent.getColor())) {
						Box pushBox = this.boxesByCoordinate.get(newPos);
						Box pushBoxNew = new Box(pushBox.getLetter(), pushBox.getColor(), new Coordinate(newBoxRow, newBoxCol));
						boxesByCoordinate.remove(pushBox.getCoordinate());
						activeAgent.setCoordinate(newPos);
						if(activeAgent.getCurrentSubGoal() != null && activeAgent.getCurrentSubGoal().getCoordinate().equals(pushBoxNew.getCoordinate()) && !activeAgent.isClearMode()) {
							pushBoxNew.setInFinalPosition(true);
							System.err.println("Set in final position");
						}
						boxesByCoordinate.put(pushBoxNew.getCoordinate(), pushBoxNew);
						for(Goal goal : goalsByCoordinate.values()){
							if(goal.getCoordinate().equals(pushBox.getCoordinate()) && goal.getLetter() == Character.toLowerCase(pushBox.getLetter())){
								if(!client.subGoals.contains(goal)){
									if(goal.getPriority() > 1 ){
										System.err.println("Priority was: " + goal.getPriority());
										goal.setPriority(goal.getPriority() - 1);
									}
									client.subGoals.offer(goal);
									System.err.println("subgoal " + goal.getLetter() + " at " + goal.getCoordinate().toString() + " reinserted in list. Priority: " + goal.getPriority());
								}
							}
						}
					} else if(boxesByCoordinate.get(newPos).getColor() == null ||  boxesByCoordinate.get(newPos).getColor().equals(activeAgent.getColor())){
						System.err.println("No box at: " + newPos.toString());
						System.err.println("Client state corrupted");
						return false;
					}
				} else {
					boolean movePossible = true;
					for(Agent a : agents){
						if(a.getId() != activeAgent.getId() && a.getCoordinate().equals(newPos)){
							movePossible = false;
						}
					}
					if (movePossible){
						activeAgent.setCoordinate(newPos);
					}
					//System.err.println("Agent " + activeAgent.getId() + "moved to " + newPos.getRow() + ", " + newPos.getColumn());
				}
			}
		}
		return true;
	}
	
	public String toString() {
		return "" + this.f;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + boxesByCoordinate.hashCode();
		result = prime * result + thisAgent.hashCode();
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;

		if(!boxesByCoordinate.equals(other.getBoxesByCoordinate())){
			return false;
		}

		if(!thisAgent.equals(other.thisAgent)){
			return false;
		}
		return true;
	}



}
