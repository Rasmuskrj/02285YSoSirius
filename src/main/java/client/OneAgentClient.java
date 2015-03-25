package client;

import heuristics.AStarHeuristic;

import java.io.*;
import java.util.*;

public class OneAgentClient {
	
	// the client can actually work as a blackboard, I'd think (at least for now)
	private Node initialState = new Node(null);

	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	//private List<Agent> agents = new ArrayList<Agent>();
	
	public int actionCount = 0;

	public OneAgentClient() throws IOException {
		readMap();
		
		for (Agent agent : initialState.getAgents()) {
			agent.setStrategy(new StrategyBestFirst(new AStarHeuristic(initialState)));
		
			// TODO: change to threads
			agent.setSolution(this.search(agent.getStrategy()));
		}
	}
	
	public LinkedList<Node> search(Strategy strategy) {
		strategy.addToFrontier(this.initialState);
		while (true) {
			if (strategy.timeSpent() > 300) {
				return null;
			}
			if (strategy.frontierIsEmpty()) {
				return null;
			}
			
			Node leafNode = strategy.getAndRemoveLeaf();
			
			if ( leafNode.isGoalState() ) {
				return leafNode.extractPlan();
			}
			
			strategy.addToExplored(leafNode);
			for (Node n : leafNode.getExpandedNodes()) {
				if ( !strategy.isExplored(n) && !strategy.inFrontier(n) ) {
					strategy.addToFrontier(n);
				}
			}
		}
	}

	private void readMap() throws IOException {
		Map< Character, String > colors = new HashMap< Character, String >();
		String line, color;

		// Read lines specifying colors
		while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
			line = line.replaceAll("\\s", "");
			color = line.split(":")[0];

			for (String id : line.split(":")[1].split(","))
				colors.put(id.charAt(0), color);
		}

		// Read lines specifying level layout
		int lineCount = 0;
		while (!line.equals("")) {
			for (int i = 0; i < line.length(); i++) {
				char id = line.charAt(i);
				if ('0' <= id && id <= '9') {				// Agents
					initialState.agents.add(new Agent(id, colors.get(id), new Coordinate(lineCount, i)));
				} else if (id == '+') {						// Walls
					Node.walls.put(new Coordinate(lineCount, i), true);
				} else if ('A' <= id && id <= 'Z') {		// Boxes
					initialState.addBox(new Box(id, colors.get(id), new Coordinate(lineCount, i)));
				} else if ('a' <= id && id <= 'z') {		// Goals
					Node.addGoal(new Goal(id, new Coordinate(lineCount, i)));
				}
			}

			line = in.readLine();
			lineCount++;
		}
	}

	public boolean update() throws IOException {
		String jointAction = "[";

		for (int i = 0; i < initialState.agents.size() - 1; i++)
			jointAction += initialState.agents.get(i).act(actionCount) + ",";
		
		jointAction += initialState.agents.get(initialState.agents.size() - 1).act(actionCount) + "]";
		System.err.println(jointAction);
		
		actionCount++;
		
		// Place message in buffer
		System.out.println(jointAction);
		
		// Flush buffer
		System.out.flush();

		// Disregard these for now, but read or the server stalls when its output buffer gets filled!
		String percepts = in.readLine();
		System.err.println(percepts);
		if (percepts == null)
			return false;

		return true;
	}

	public static void main(String[] args) {

		// Use stderr to print to console
		System.err.println("Hello from OneAgentClient. I am sending this using the error outputstream");
		try {
			OneAgentClient client = new OneAgentClient();
			while (client.update());

		} catch (IOException e) {
			// Got nowhere to write to probably
		}
	}
}
