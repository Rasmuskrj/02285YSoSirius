package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import client.Command.dir;
import client.Command.type;

public class Node {

	public static HashMap<Coordinate, Boolean> walls;
    private static HashMap<Coordinate, Goal> goalsByCoordinate;
    private static HashMap<Character, Goal> goalsByID;
    private HashMap<Coordinate, Box> boxesByCoordinate;
    private HashMap<Character, Box> boxesByID;

	//public int agentRow;
	//public int agentCol;
    
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
			if (Character.toLowerCase(box.getLetter()) != goal.getLetter()) {
				return false;
			}
		}
		return true;
	}

	public ArrayList< Node > getExpandedNodes() {
		ArrayList< Node > expandedNodes = new ArrayList< Node >(Command.every.length);
		for (Command c : Command.every) {
			// Determine applicability of action
			int newAgentRow = this.agents.get(0).getCoordinate().getRow() + dirToRowChange(c.dir1);
			int newAgentCol = this.agents.get(0).getCoordinate().getColumn() + dirToColChange(c.dir1);

			if (c.actType == type.Move) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if (cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.ChildNode();
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
						Node n = this.ChildNode();
						n.action = c;
						n.agents.get(0).getCoordinate().setRow(newAgentRow);
						n.agents.get(0).getCoordinate().setColumn(newAgentCol);
						n.boxesByCoordinate.put(new Coordinate(newBoxRow, newBoxCol), 
									this.boxesByCoordinate.get(new Coordinate(newAgentRow, newAgentCol)));
						n.boxesByCoordinate.remove(new Coordinate(newAgentRow, newAgentCol));
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
						Node n = this.ChildNode();
						n.action = c;
						n.agents.get(0).getCoordinate().setRow(newAgentRow);
						n.agents.get(0).getCoordinate().setColumn(newAgentCol);
						n.boxesByCoordinate.put(
								new Coordinate(this.agents.get(0).getCoordinate().getRow(), 
											this.agents.get(0).getCoordinate().getRow()), 
										this.boxesByCoordinate.get(new Coordinate(boxRow, boxCol)));
						n.boxesByCoordinate.remove(new Coordinate(boxRow, boxCol));
						expandedNodes.add(n);
					}
				}
			}
		}
		//Collections.shuffle(expandedNodes, rnd);
		return expandedNodes;
	}

	private boolean cellIsFree(int row, int col) {
		return (!Node.walls.get(new Coordinate(row, col)) 
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

	private Node ChildNode() {
		Node copy = new Node(this);
		for (Coordinate key : this.boxesByCoordinate.keySet()) {
			copy.boxesByCoordinate.put(key, this.boxesByCoordinate.get(key));
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
