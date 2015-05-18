package client;

import heuristics.AStarHeuristic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Agent {
	// We don't actually use these for Randomly Walking Around
	private char id;
	private String color;
	private Coordinate coordinate;
	private Strategy strategy;
	private LinkedList<Node> solution;
	private Goal currentSubGoal = null;
	private Node latestAction = null;
	private boolean clearMode = false;
	private ArrayList<Coordinate> clearCords;
	private boolean quarantined = false;
	private Agent quarantinedBy;
	public boolean done = false;

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

	public Command act() {
		if(solution.size() > 0){
			Node next = solution.getFirst();
			latestAction = next;
			solution.removeFirst();
			return next.action;
		} else {
			return null;
		}

		/*if (solution.size() > i) {
			return solution.get(i).action.toActionString();
		} else {
			return "NoOp";
		}*/
	}

	public void findNextGoal(Node currentState, PriorityQueue<Goal> subGoals){
		if(currentSubGoal != null){
			Box goalBox = currentState.getBoxesByCoordinate().get(currentSubGoal.getCoordinate());
			if(goalBox != null && goalBox.getLetter() == Character.toUpperCase(currentSubGoal.getLetter())){
				currentSubGoal = null;
			} else {
				return;
			}
		}
		if(color == null){
			currentSubGoal = subGoals.poll();
			done = false;
		} else {
			for(Goal goal : subGoals){
				for(Box box : currentState.getBoxesByCoordinate().values()){
					if(box.getColor().equals(color) && box.getLetter() == Character.toUpperCase(goal.getLetter())){
						currentSubGoal = goal;
						subGoals.remove(goal);
						done = false;
						return;
					}
				}
			}
		}
		if(currentSubGoal == null){
			done = true;
			System.err.println("Agent " + id + " is done");
		}
	}

	public void requestClear(Node currentState){
		ArrayList<Coordinate> clearCoordinates = new ArrayList<>();
		Coordinate pos = coordinate;
		Command cmd;
		ArrayList<Coordinate> cmdEffectsCoordinates;
		System.err.println(pos);
		if(latestAction != null) {
			cmd = latestAction.action;
			cmdEffectsCoordinates = currentState.commandToCoordinates(pos, cmd);
			pos = new Coordinate(cmdEffectsCoordinates.get(0).getRow(), cmdEffectsCoordinates.get(0).getColumn());
			clearCoordinates.addAll(cmdEffectsCoordinates);
			//System.err.println(cmd.toString());
		}
		cmd = act();
		while(cmd != null){
			//System.err.println(cmd.toString());
			cmdEffectsCoordinates = currentState.commandToCoordinates(pos, cmd);
			pos = new Coordinate(cmdEffectsCoordinates.get(0).getRow(), cmdEffectsCoordinates.get(0).getColumn());
			clearCoordinates.addAll(cmdEffectsCoordinates);
			cmd = act();
		}
		for(Agent agent : currentState.agents){
			if(agent.getId() != id){
				ArrayList<Coordinate> agentClearCords = new ArrayList<>();
				for(Coordinate cord : clearCoordinates){
					Box box = currentState.getBoxesByCoordinate().get(cord);
					if(agent.getCoordinate().equals(cord) || (box != null && box.getColor() != null && box.getColor().equals(agent.getColor()) && !box.getColor().equals(color))){
						agentClearCords.add(cord);
					}
				}
				agent.clearCells(agentClearCords, currentState, this);
			}
		}
	}

	public void clearCells(ArrayList<Coordinate> coordinates, Node currentState, Agent sender){
		System.err.println("Agent number " + id + " attempting to clear");
		for(Coordinate cord : coordinates){
			System.err.println(cord.toString());
		}
		clearMode = true;
		solution.clear();
		clearCords = coordinates;
		Node myCurrentState = currentState.getCopy();
		myCurrentState.thisAgent = this;
		this.setStrategy(new StrategyBestFirst(new AStarHeuristic(myCurrentState)));
		LinkedList<Node> plan = MultiAgentClient.search(this.getStrategy(), myCurrentState);
		if(plan != null) {
			this.appendSolution(plan);
		} else {
			System.err.println("Solution could not be found");
		}
		//System.err.println("Clear solution:");
		//for(Node n : plan){
		//	System.err.println(n.action.toString());
		//}
		clearMode = false;
		clearCords.clear();
		quarantined = true;
		quarantinedBy = sender;
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

	public boolean isClearMode() {
		return clearMode;
	}

	public void setClearMode(boolean clearMode) {
		this.clearMode = clearMode;
	}

	public ArrayList<Coordinate> getClearCords() {
		return clearCords;
	}

	public void setClearCords(ArrayList<Coordinate> clearCords) {
		this.clearCords = clearCords;
	}

	public boolean isQuarantined() {
		return quarantined;
	}

	public void setQuarantined(boolean quarantined) {
		this.quarantined = quarantined;
	}

	public Agent getQuarantinedBy() {
		return quarantinedBy;
	}

	public void setQuarantinedBy(Agent quarantinedBy) {
		this.quarantinedBy = quarantinedBy;
	}

	@Override
	public Agent clone() {
		Agent newAgent = new Agent(this.id, this.color, 
					new Coordinate(this.coordinate.getRow(), this.coordinate.getColumn()), this.currentSubGoal);
		newAgent.setClearMode(clearMode);
		newAgent.setClearCords(clearCords);
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

	public static ArrayList<Agent> sortById(ArrayList<Agent> agents){
		ArrayList<Agent> returnArr = new ArrayList<>(agents.size());
		for( int i = 0; i < agents.size(); i++){
			returnArr.add(null);
		}
		for (Agent agent : agents){
			returnArr.remove(Character.getNumericValue(agent.getId()));
			returnArr.add(Character.getNumericValue(agent.getId()), agent);
		}
		return returnArr;
	}
}
