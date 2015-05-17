package heuristics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import client.*;

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


	private int manhattanSubgoalHeuristic(Node n){
		int ret = 0;
		Goal currentSubGoal = null;
		//Find current main subgoal. CurrentMainGoal is set in the while-loop in OneAgentClient
		currentSubGoal = n.thisAgent.getCurrentSubGoal();

		/*for (Goal goal : Node.getGoalsByCoordinate().values()){
			if(goal.isCurrentMainGoal()){
				currentSubGoal = goal;
				break;
			}
		}*/
		if(currentSubGoal != null){
			//find closest box that can be used to solve
			int boxDistance = Integer.MAX_VALUE;
			Box targetBox = null;
			for(Box box : n.getBoxesByCoordinate().values()){
				if(!box.isInFinalPosition() && box.getLetter() == Character.toUpperCase(currentSubGoal.getLetter())){
					HashMap<Coordinate, Integer> goalMap = Node.goalDistance.get(currentSubGoal);
					int dist = goalMap.get(box.getCoordinate());
					//int dist = Math.abs(box.getCoordinate().getColumn() - currentSubGoal.getCoordinate().getColumn()) + Math.abs(box.getCoordinate().getRow() - currentSubGoal.getCoordinate().getRow());
					if(dist < boxDistance){
						targetBox = box;
						boxDistance = dist;
					}
				}
			}
			if(targetBox != null){
				//calculate heuristic. There is extra weight in moving the box closer to its goal.
				ret = (10 * boxDistance) + Math.abs(n.thisAgent.getCoordinate().getRow() - targetBox.getCoordinate().getRow()) +
						Math.abs(n.thisAgent.getCoordinate().getColumn() - targetBox.getCoordinate().getColumn());
				Command com = n.action;
				if(com != null && !(com.actType == Command.type.Move)){
					//if the agent is moving boxes that is not the target box, the heuristic is worsened.
					int boxRow = n.thisAgent.getCoordinate().getRow() + n.dirToRowChange(com.dir2);
					int boxCol = n.thisAgent.getCoordinate().getColumn() + n.dirToColChange(com.dir2);
					if(!(boxRow == targetBox.getCoordinate().getRow() && boxCol == targetBox.getCoordinate().getColumn() )){
						ret += 10;
					}
				}
				Box possibleBox = n.getBoxesByCoordinate().get(n.thisAgent.getCoordinate());
				if(possibleBox != null){
					ret += 200;
				}
				for(Agent a : n.agents){
					if(a.getId() != n.thisAgent.getId() && a.getCoordinate().equals(n.thisAgent.getCoordinate())){
						ret += 200;
					}
				}

			} else {
				if(!n.isGoalState()) {
					System.err.println("Error calculating heuristics: No free box to solve current main sub-goal");
				}
			}
		} else {
			System.err.println("Error calculating heuristics: No goal is current main sub-goal");
		}
		return ret;
	}

	public int clearModeHeuristic(Node n){
		int ret = 1;
		boolean wrongBox = true;
		for(Coordinate cord : n.thisAgent.getClearCords()){
			Box box = n.getBoxesByCoordinate().get(cord);
			if(box != null && box.getColor() != null && !box.getColor().equals(n.thisAgent.getColor())){
				ret += 20;
				ret += Math.abs(n.thisAgent.getCoordinate().getRow() - box.getCoordinate().getRow()) +
						Math.abs(n.thisAgent.getCoordinate().getColumn() - box.getCoordinate().getColumn());
				Command com = n.action;
				if(com != null && !(com.actType == Command.type.Move)){
					//if the agent is moving boxes that is not the target box, the heuristic is worsened.
					int boxRow = n.thisAgent.getCoordinate().getRow() + n.dirToRowChange(com.dir2);
					int boxCol = n.thisAgent.getCoordinate().getColumn() + n.dirToColChange(com.dir2);
					if((boxRow == box.getCoordinate().getRow() && boxCol == box.getCoordinate().getColumn() )){
						wrongBox = false;
					}
				}
			}
			if(cord.equals(n.thisAgent.getCoordinate())){
				ret += 100;
			}
			if(wrongBox){
				ret += 200;
			}
		}
		Box possibleBox = n.getBoxesByCoordinate().get(n.thisAgent.getCoordinate());
		if(possibleBox != null){
			ret += 20;
		}
		for(Agent a : n.agents){
			if(a.getId() != n.thisAgent.getId() && a.getCoordinate().equals(n.thisAgent.getCoordinate())){
				ret += 20;
			}
		}
		Command com2 = n.action;
		if(com2 != null && com2.actType == Command.type.Push){
			int boxRow = n.thisAgent.getCoordinate().getRow() + n.dirToRowChange(com2.dir2);
			int boxCol = n.thisAgent.getCoordinate().getColumn() + n.dirToColChange(com2.dir2);
			possibleBox = n.getBoxesByCoordinate().get(new Coordinate(boxRow, boxCol));
			if(possibleBox != null){
				ret += 500;
			}
		}
		return ret;
	}


	public int h(Node n) {
		//return maxManhattanHeuristic(n);
		//return sumManhattanHeuristic(n);
		//return sumManhattanExcludeSolvedHeuristic(n);
		//return sumManhattenPrioritizeClosestHeuristic(n);
		if(!n.thisAgent.isClearMode()) {
			return manhattanSubgoalHeuristic(n);
		} else {
			return clearModeHeuristic(n);
		}
	}

	public abstract int f(Node n);

}
