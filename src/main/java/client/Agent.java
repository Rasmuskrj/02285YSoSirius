package client;

import java.util.LinkedList;
import java.util.PriorityQueue;

public class Agent {
	// We don't actually use these for Randomly Walking Around
	private char id;
	private String color;
	private Coordinate coordinate;
	private Strategy strategy;
	private LinkedList<LinkedList<Node>> solutionList;
	
	//private LinkedList<Node> solution;
	private Goal currentSubGoal = null;
	private boolean lastPercept = true;

	public Agent(char id, String color, Coordinate coordinate) {
		this.id = id;
		this.color = color;
		this.coordinate = coordinate;
		//this.solution = new LinkedList<>();
		this.solutionList = new LinkedList<LinkedList<Node>>();
	}

	public Agent(char id, String color, Coordinate coordinate, Goal currentSubGoal){
		this.id = id;
		this.color = color;
		this.coordinate = coordinate;
		this.currentSubGoal = currentSubGoal;
		//this.solution = new LinkedList<>();
		this.solutionList = new LinkedList<LinkedList<Node>>();
	}

	
	public String act() {
		
		if (solutionList.size() > 0 && solutionList.getFirst().size() > 0) {
			Node next = solutionList.getFirst().getFirst();
			solutionList.getFirst().removeFirst();
			if (solutionList.getFirst().size() == 0) {
				solutionList.removeFirst();
			}
			return next.action.toActionString();
		} else {
			return "NoOp";
		}
		
		/*
		if (solutionList.peek().size() > 0) {
			Node next = solutionList.peek().getFirst();
			solutionList.peek().removeFirst();
		}
		
		if (solution.size() > i) {
			return solution.get(i).action.toActionString();
		} else {
			return "NoOp";
		}
		*/
	}

	public String multiAct(){
		if (solutionList.size() > 0 && solutionList.peek().size() > 0) {
			Node next = solutionList.peek().getFirst();
			solutionList.peek().removeFirst();
			return next.action.toString();
		} else {
			return "NoOp";
		}
		/*
		if (solution.size() > i) {
			return solution.get(i).action.toString();
		} else {
			return "NoOp";
		}
		*/
	}
	
	public char getId() {
		return id;
	}

	public void setId(char id) {
		this.id = id;
	}
	
	public boolean isLastPercept() {
		return lastPercept;
	}
	
	public void setLastPercept(boolean lastPercept) {
		this.lastPercept = lastPercept;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
	
	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public LinkedList<LinkedList<Node>> getSolutionList() {
		return solutionList;
	}
	
	public void setSolutionList(LinkedList<LinkedList<Node>> solutionList) {
		this.solutionList = solutionList;
	}
	
	public void appendSolution(LinkedList<Node> partialSolution) {
		this.solutionList.add(partialSolution); // equivalent to addLast()
	}
	
	public void appendPrioritySolution(LinkedList<Node> partialSolution) {
		this.solutionList.addFirst(partialSolution);
	}
	
	/*
	public LinkedList<Node> getSolution() {
		return solution;
	}

	public void setSolution(LinkedList<Node> solution) {
		this.solution = solution;
	}

	public void appendSolution(LinkedList<Node> partialSolution){
		this.solution.addAll(partialSolution);
	}
	*/
	public Goal getCurrentSubGoal() {
		return currentSubGoal;
	}

	public void setCurrentSubGoal(Goal currentSubGoal) {
		this.currentSubGoal = currentSubGoal;
	}

	@Override
	public Agent clone() {
		Agent newAgent = new Agent(this.id, this.color, 
					new Coordinate(this.coordinate.getRow(), this.coordinate.getColumn()), this.currentSubGoal);
		return newAgent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Agent agent = (Agent) o;

		if (id != agent.id) return false;
		if (color != null ? !color.equals(agent.color) : agent.color != null) return false;
		return coordinate.equals(agent.coordinate);

	}

	@Override
	public int hashCode() {
		int result = (int) id;
		result = 31 * result + (color != null ? color.hashCode() : 0);
		result = 31 * result + coordinate.hashCode();
		return result;
	}
}
