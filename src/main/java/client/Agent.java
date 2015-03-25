package client;

import java.util.LinkedList;

public class Agent {
	// We don't actually use these for Randomly Walking Around
	private char id;
	private String color;
	private Coordinate coordinate;
	private Strategy strategy;
	private LinkedList<Node> solution;

	public Agent(char id, String color, Coordinate coordinate) {
		this.id = id;
		this.color = color;
		this.coordinate = coordinate;
	}

	public String act(int i) {
		if (solution.size() > i) {
			return solution.get(i).action.toActionString();
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
	
}
