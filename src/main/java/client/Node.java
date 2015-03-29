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

	//public int agentRow;
	//public int agentCol;
    
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
		this.easiestBoxes = new PriorityQueue<>(20, boxComparator);
		if (parent == null) {
			g = 0;
		} else {
			g = parent.g() + 1;
		}
	}

	public static Comparator<Box> boxComparator = new Comparator<Box>() {
		@Override
		public int compare(Box o1, Box o2) {
			int box1Dist = Integer.MAX_VALUE;
			int box2Dist = Integer.MAX_VALUE;
			for(Goal goal : Node.getGoalsByCoordinate().values()){
				if(goal.getLetter() == Character.toLowerCase(o1.getLetter()) && !Node.isBoxInTargetGoalCell(o1)){
					int box1CheckDist = Math.abs(goal.getCoordinate().getRow() - o1.getCoordinate().getRow()) +
							Math.abs(goal.getCoordinate().getColumn() - o1.getCoordinate().getColumn());
					if(box1CheckDist < box1Dist)
						box1Dist = box1CheckDist;
				}
				if(goal.getLetter() == Character.toLowerCase(o2.getLetter()) && !Node.isBoxInTargetGoalCell(o2)){
					int box2CheckDist = Math.abs(goal.getCoordinate().getRow() - o2.getCoordinate().getRow()) +
							Math.abs(goal.getCoordinate().getColumn() - o2.getCoordinate().getColumn());
					if(box2CheckDist < box2Dist)
						box2Dist = box2CheckDist;
				}
			}

			return (int) (box2Dist - box1Dist);
		}
	};
	
	public HashMap<Coordinate, Box> getBoxesByCoordinate() {
        return boxesByCoordinate;
    }
    
    public HashMap<Character, Box> getBoxesByID() {
        return boxesByID;
    }

	public PriorityQueue<Box> getEasiestBoxes(){
		return easiestBoxes;
	}

    public void addBox(Box box) {
        this.boxesByCoordinate.put(box.getCoordinate(), box);
        this.boxesByID.put(box.getLetter(), box);
		this.easiestBoxes.offer(box);
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
		for (Coordinate key : Node.goalsByCoordinate.keySet()) {
			Goal goal = Node.goalsByCoordinate.get(key);
			Box box = boxesByCoordinate.get(key);
			if (box == null || Character.toLowerCase(box.getLetter()) != goal.getLetter()) {
				return false;
			}
		}
		return true;
	}

	public static boolean isBoxInTargetGoalCell(Box box){
		for(Coordinate goalCord : Node.getGoalsByCoordinate().keySet()){
			if(box.getLetter() == Character.toUpperCase(Node.getGoalsByCoordinate().get(goalCord).getLetter()) && box.getCoordinate().equals(goalCord)){
				return true;
			}
		}
		return false;
	}

	public ArrayList<Node> getExpandedNodes() {
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.every.length);
		for (Command c : Command.every) {
			// Determine applicability of action
			int newAgentRow = this.agents.get(0).getCoordinate().getRow() + dirToRowChange(c.dir1);
			int newAgentCol = this.agents.get(0).getCoordinate().getColumn() + dirToColChange(c.dir1);

			if (c.actType == type.Move) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if (cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.childNode();
					n.action = c;
					n.agents.get(0).getCoordinate().setRow(newAgentRow);
					n.agents.get(0).getCoordinate().setColumn(newAgentCol);
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
						n.agents.get(0).getCoordinate().setRow(newAgentRow);
						n.agents.get(0).getCoordinate().setColumn(newAgentCol);
						// TODO: eventually refactor with clone()
						Box boxToMove = this.boxesByCoordinate.get(
											new Coordinate(newAgentRow, newAgentCol));
						Box boxToMoveCopy = new Box(boxToMove.getLetter(), boxToMove.getColor(),
													new Coordinate(newBoxRow, newBoxCol));
						//boxToMove.setCoordinate(new Coordinate(newBoxRow, newBoxCol));
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
					int boxRow = this.agents.get(0).getCoordinate().getRow() + dirToRowChange(c.dir2);
					int boxCol = this.agents.get(0).getCoordinate().getColumn() + dirToColChange(c.dir2);
					// .. and there's a box in "dir2" of the agent
					if (boxAt(boxRow, boxCol)) {
						Node n = this.childNode();
						n.action = c;
						// TODO: eventually refactor with clone()
						Box boxToMove = this.boxesByCoordinate.get(
											new Coordinate(boxRow, boxCol));
						Box boxToMoveCopy = new Box(boxToMove.getLetter(), boxToMove.getColor(),
								new Coordinate(this.agents.get(0).getCoordinate().getRow(), 
										this.agents.get(0).getCoordinate().getColumn()));
						//Coordinate newBoxCoordinate = new Coordinate(this.agents.get(0).getCoordinate().getRow(), 
						//					this.agents.get(0).getCoordinate().getColumn()); 
						//boxToMove.setCoordinate(newBoxCoordinate);
						n.boxesByCoordinate.put(new Coordinate(this.agents.get(0).getCoordinate().getRow(), 
								this.agents.get(0).getCoordinate().getColumn()), boxToMoveCopy);
						n.boxesByCoordinate.remove(new Coordinate(boxRow, boxCol));
						n.boxesByID.remove(boxToMoveCopy.getLetter());
						n.boxesByID.put(boxToMoveCopy.getLetter(), boxToMoveCopy);
						n.agents.get(0).getCoordinate().setRow(newAgentRow);
						n.agents.get(0).getCoordinate().setColumn(newAgentCol);
						expandedNodes.add(n);
					}
				}
			}
		}
		return expandedNodes;
	}

	private boolean cellIsFree(int row, int col) {
		return (Node.walls.get(new Coordinate(row, col)) == null
					&& this.boxesByCoordinate.get(new Coordinate(row, col)) == null);
	}

	private boolean boxAt(int row, int col) {
		return this.boxesByCoordinate.get(new Coordinate(row, col)) != null;
	}

	private int dirToRowChange(dir d) { 
		return (d == dir.S ? 1 : (d == dir.N ? -1 : 0)); // South is down one row (1), north is up one row (-1)
	}

	private int dirToColChange(dir d) {
		return (d == dir.E ? 1 : (d == dir.W ? -1 : 0)); // East is left one column (1), west is right one column (-1)
	}

	private Node childNode() {
		Node copy = new Node(this);
		for (Coordinate key : this.boxesByCoordinate.keySet()) {
			copy.boxesByCoordinate.put(key, this.boxesByCoordinate.get(key));
			copy.boxesByID.put(this.boxesByCoordinate.get(key).getLetter(), this.boxesByCoordinate.get(key));
			copy.easiestBoxes.offer(this.boxesByCoordinate.get(key));
		}
		for (Agent agent : this.agents) {
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
	
	@Override
    public boolean equals(Object obj) {
    	if (this == obj)
    		return true;
    	if (obj == null)
    		return false;
    	if (this.getClass() != obj.getClass())
    		return false;
    	Node other = (Node) obj;
    	if (this.parent != other.parent || this.action != other.action
    			|| !this.agents.equals(other.agents)
    			|| !this.boxesByCoordinate.equals(other.getBoxesByCoordinate())
    			|| !this.boxesByID.equals(other.getBoxesByID()))
    		return false;
    	return true;
    }

	public void printState() {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<Node.totalRows; i++) {
			for (int j=0; j<Node.totalColumns; j++) {
				if (Node.walls.get(new Coordinate(i, j)) != null) {
					builder.append('+');
				} else if (this.agents.get(0).getCoordinate().equals(new Coordinate(i, j))){
					builder.append(this.agents.get(0).getId());
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
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getF() {
		return f;
	}

	public void setF(int f) {
		this.f = f;
	}
	
	public String toString() {
		return "" + this.f;
	}

	/* TODO: refactor - if needed
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + agentCol;
		result = prime * result + agentRow;
		result = prime * result + Arrays.deepHashCode(boxes.toArray());
		return result;
	}
	*/

	/* TODO: refactor - if needed
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (agentCol != other.agentCol)
			return false;
		if (agentRow != other.agentRow)
			return false;
		if (!Arrays.deepEquals(boxes.toArray(), other.boxes.toArray())) {
			return false;
		}
		return true;
	}
	*/

	/* TODO: refactor - if needed
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < this.boxes.size(); row++) {
			if (!Node.walls.get(row).get(0)) {
				break;
			}
			for (int col = 0; col < this.boxes.get(row).size(); col++) {
				if (this.boxes.get(row).get(col) > 0) {
					s.append(this.boxes.get(row).get(col));
				} else if (row == this.agentRow && col == this.agentCol) {
					s.append("0");
				} else {
					s.append(" ");
				}
			}

			s.append("\n");
		}
		return s.toString();
	}
	*/

}
