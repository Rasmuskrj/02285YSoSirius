package heuristics;

import java.util.Comparator;
import java.util.PriorityQueue;

import client.Box;
import client.Coordinate;
import client.Node;
import client.Goal;

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

	private int sumManhattanExcludeSolvedHeuristic(Node n) {

		System.err.print((n.action != null ? n.action.toActionString() : "") + " :: ");
		int sumH = 0;
		for (Coordinate goalCoordinate : Node.getGoalsByCoordinate().keySet()) {
			Character itemName = Node.getGoalsByCoordinate().get(goalCoordinate).getLetter();
			Coordinate boxCoordinate = goalCoordinate;// = n.getBoxesByID().get(Character.toUpperCase(itemName)).getCoordinate();
			for(Box box : n.getBoxesByCoordinate().values()){
				if(box.getLetter() == Character.toUpperCase(itemName) && !Node.isBoxInTargetGoalCell(box)){
					boxCoordinate = box.getCoordinate();
					break;
				}
			}
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

	private int sumManhattenPrioritizeClosestHeuristic(Node n){
		int sumH = 0;

		PriorityQueue<Box> targetBoxes = n.getEasiestBoxes();
		while (!targetBoxes.isEmpty()){
			Box box = targetBoxes.poll();
			int goalDist = Integer.MAX_VALUE;
			Goal targetGoal = Node.getGoalsByID().get(Character.toLowerCase(box.getLetter()));
			for(Goal goal : Node.getGoalsByCoordinate().values()){
				Character itemName = goal.getLetter();
				Coordinate goalCoordinate = goal.getCoordinate();
				int distance = Math.abs(box.getCoordinate().getColumn() - goalCoordinate.getColumn()) + Math.abs(box.getCoordinate().getRow() - goalCoordinate.getRow());
				if(itemName == Character.toLowerCase(box.getLetter()) && distance < goalDist){
					targetGoal = goal;
					goalDist = distance;
				}
			}
			if (!targetGoal.getCoordinate().equals(box.getCoordinate())) {
				int newH = Math.abs(n.agents.get(0).getCoordinate().getRow() - box.getCoordinate().getRow())
						+ Math.abs(n.agents.get(0).getCoordinate().getColumn()
						- box.getCoordinate().getColumn()) - 1
						+ Math.abs(box.getCoordinate().getRow() - targetGoal.getCoordinate().getRow())
						+ Math.abs(box.getCoordinate().getColumn() - targetGoal.getCoordinate().getColumn());
				sumH += newH + 1000;
			}
		}
		System.err.println("  " + sumH);
		return sumH;
	}

	public int h(Node n) {
		//return maxManhattanHeuristic(n);
		//return sumManhattanHeuristic(n);
		//return sumManhattanExcludeSolvedHeuristic(n);
		return sumManhattenPrioritizeClosestHeuristic(n);
	}

	public abstract int f(Node n);

}
