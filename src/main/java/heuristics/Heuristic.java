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
			int newH = Math.abs(n.agents.get(0).getCoordinate().getRow() - goalCoordinate.getRow()) 
						+ Math.abs(n.agents.get(0).getCoordinate().getColumn() 
											- goalCoordinate.getColumn()) - 1
						+ Math.abs(goalCoordinate.getRow() - boxCoordinate.getRow())
						+ Math.abs(goalCoordinate.getColumn() - boxCoordinate.getColumn());
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
			int newH = Math.abs(n.agents.get(0).getCoordinate().getRow() - goalCoordinate.getRow()) 
						+ Math.abs(n.agents.get(0).getCoordinate().getColumn() 
											- goalCoordinate.getColumn()) - 1
						+ Math.abs(goalCoordinate.getRow() - boxCoordinate.getRow())
						+ Math.abs(goalCoordinate.getColumn() - boxCoordinate.getColumn());
			sumH += newH;
		}
		
		return sumH;
	}

	public int h(Node n) {
		return maxManhattanHeuristic(n);
		//return sumManhattanHeuristic(n);
	}

	public abstract int f(Node n);

}
