package client;

import java.util.LinkedList;

public class Agent {
	// We don't actually use these for Randomly Walking Around
	private char id;
	private String color;
	private Coordinate coordinate;
	private Strategy strategy;
	private LinkedList<Node> solution;
	private Goal currentSubGoal = null;

	public Agent(char id, String color, Coordinate coordinate) {
		this.id = id;
		this.color = color;
		this.coordinate = coordinate;
		this.solution = new LinkedList<>();
	}

	public Agent(char id, String color, Coordinate coordinate, Goal currentSubGoal){
		this.id = id;
		this.color = color;
		this.coordinate = coordinate;
		this.currentSubGoal = currentSubGoal;
		this.solution = new LinkedList<>();
	}

	public String act(int i) {
		if (solution.size() > i) {
			return solution.get(i).action.toActionString();
		} else {
			return "NoOp";
		}
	}

	public String multiAct(int i){
		if (solution.size() > i) {
			return solution.get(i).action.toString();
		} else {
			return "NoOp";
		}
	}
	
	public char getId() {
		return id;
	}

	public void setId(char id) {
		this.id = id;
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

	public LinkedList<Node> getSolution() {
		return solution;
	}

	public void setSolution(LinkedList<Node> solution) {
		this.solution = solution;
	}

	public void appendSolution(LinkedList<Node> partialSolution){
		this.solution.addAll(partialSolution);
	}

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
