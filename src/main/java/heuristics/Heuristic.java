package heuristics;

import java.util.Comparator;

import client.Coordinate;
import client.Node;

public abstract class Heuristic implements Comparator< Node > {

	public Node initialState;
	public Heuristic(Node initialState) {
		this.initialState = initialState;
	}

	public int compare(Node n1, Node n2) {
		return f(n1) - f(n2);
	}
	
	private int maxManhattanHeuristic(Node n) {
		
		int maxH = 0;
		for (Character itemName : Node.getGoalsByID().keySet()) {
			Coordinate goalCoordinate = Node.getGoalsByID().get(itemName).getCoordinate();
			Coordinate boxCoordinate = n.getBoxesByID().get(Character.toUpperCase(itemName)).getCoordinate();
			int newH = Math.abs(n.agents.get(0).getCoordinate().getRow() - boxCoordinate.getRow()) 
						+ Math.abs(n.agents.get(0).getCoordinate().getColumn() 
											- boxCoordinate.getColumn()) - 1
						+ Math.abs(boxCoordinate.getRow() - goalCoordinate.getRow())
						+ Math.abs(boxCoordinate.getColumn() - goalCoordinate.getColumn());
			if (newH > maxH) {
				maxH = newH;
			}
		}
		
		return maxH;
	}
	
	@SuppressWarnings("unused")
	private int sumManhattanHeuristic(Node n) {
		
		int sumH = 0;
		for (Character itemName : Node.getGoalsByID().keySet()) {
			Coordinate goalCoordinate = Node.getGoalsByID().get(itemName).getCoordinate();
			Coordinate boxCoordinate = n.getBoxesByID().get(Character.toUpperCase(itemName)).getCoordinate();
			int newH = Math.abs(n.agents.get(0).getCoordinate().getRow() - boxCoordinate.getRow()) 
						+ Math.abs(n.agents.get(0).getCoordinate().getColumn() 
											- boxCoordinate.getColumn()) - 1
						+ Math.abs(boxCoordinate.getRow() - goalCoordinate.getRow())
						+ Math.abs(boxCoordinate.getColumn() - goalCoordinate.getColumn());
			sumH += newH;
		}
		
		return sumH;
	}
	
	@SuppressWarnings("unused")
	private int sumManhattanExcludeSolvedHeuristic(Node n) {
		
		System.err.print((n.action != null ? n.action.toActionString() : "") + " :: ");
		int sumH = 0;
		for (Character itemName : Node.getGoalsByID().keySet()) {
			// this currently assumes only one box for each ID (character) - and one goal for the box
			Coordinate goalCoordinate = Node.getGoalsByID().get(itemName).getCoordinate();
			Coordinate boxCoordinate = n.getBoxesByID().get(Character.toUpperCase(itemName)).getCoordinate();
			System.err.print(goalCoordinate.toString() + " / " + boxCoordinate.toString() + " ;; ");
			// this currently assumes only one agent
			if (!goalCoordinate.equals(boxCoordinate)) {
				int newH = Math.abs(n.agents.get(0).getCoordinate().getRow() - boxCoordinate.getRow()) 
							+ Math.abs(n.agents.get(0).getCoordinate().getColumn() 
												- boxCoordinate.getColumn()) - 1
							+ Math.abs(boxCoordinate.getRow() - goalCoordinate.getRow())
							+ Math.abs(boxCoordinate.getColumn() - goalCoordinate.getColumn());
				sumH += newH + 1000;
			}
		}
		System.err.println("  " + sumH);
		return sumH;
	}

	public int h(Node n) {
		//return maxManhattanHeuristic(n);
		//return sumManhattanHeuristic(n);
		return sumManhattanExcludeSolvedHeuristic(n);
	}

	public abstract int f(Node n);

}
