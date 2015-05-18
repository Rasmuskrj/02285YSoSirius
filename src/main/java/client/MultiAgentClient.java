package client;

/**
 * Created by RasmusKr�yer on 14-04-2015.
 */

import heuristics.AStarHeuristic;

import java.io.*;
import java.util.*;

public class MultiAgentClient {
    // the client can actually work as a blackboard, I'd think (at least for now)
    private Node currentState = new Node(null);

    public PriorityQueue<Goal> subGoals;
    private ArrayList<Agent> agents = new ArrayList<>();
    private String[] latestServerOutput = null;
    private Command[] latestActionArray = null;
    private Boolean[] agentErrorState = null;

    // uncomment two lines below if testing without server and comment the third line
    //FileReader fr = new FileReader("levels/MAsimple3.lvl");
    //private BufferedReader in = new BufferedReader(fr);
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public MultiAgentClient() throws IOException {
        readMap();
        Node.computeGoalDistance();
        findSubgoals();
        latestActionArray = new Command[currentState.agents.size()];
        agentErrorState = new Boolean[currentState.agents.size()];
        in.readLine();
        boolean finished = false;
        while(!finished) {
            for (Agent agent : currentState.agents) {
                agent.findNextGoal(currentState, subGoals);
                if(!agent.isQuarantined() && !agent.done) {
                    System.err.println("Agent " + agent.getId() + " finding solution for goal " + agent.getCurrentSubGoal().getLetter());
                    Node myinitalState = currentState.getCopy();
                    myinitalState.thisAgent = agent;
                    agent.setStrategy(new StrategyBestFirst(new AStarHeuristic(myinitalState)));
                    LinkedList<Node> plan = search(agent.getStrategy(), myinitalState);
                    if(plan != null) {
                        agent.appendSolution(plan);
                    } else {
                        System.err.println("Solution could not be found");
                    }
                }
            }
            //Execute solutions as long as possible
            boolean cont = true;
            while (cont) {
                cont = update();
                boolean status = currentState.changeState(latestActionArray, latestServerOutput, this);
                //currentState.printState();
                if(!status) {
                    currentState.printState();
                    System.exit(0);
                }
            }
            boolean error = false;
            for(int i = 0; i < latestServerOutput.length; i++){
                if(latestServerOutput[i] != null && latestServerOutput[i].equals("false")){
                    error = true;
                    agentErrorState[i] = true;
                    Agent failAgent = currentState.getAgentById(Integer.toString(i).charAt(0));
                    if(!currentState.agents.get(i).isQuarantined()) {
                        System.err.println("Agent number " + failAgent.getId() + " is requesting clear");
                        failAgent.requestClear(currentState);
                    }
                } else {
                    agentErrorState[i] = false;
                    System.err.println("Agent number " + currentState.agents.get(i).getId() + " is done with no error");
                    for(Agent a : currentState.getAgents()){
                        if(a.isQuarantined() && a.getQuarantinedBy().getId() == currentState.agents.get(i).getId()){
                            a.setQuarantined(false);
                        }
                    }
                }
            }
            if(error){
                while (update()) {
                    boolean status = currentState.changeState(latestActionArray, latestServerOutput, this);
                    if(!status) {
                        currentState.printState();
                        System.exit(0);
                    }
                }
            }
            boolean agentsDone = true;
            for(Agent a : currentState.agents){
                if(a.getCurrentSubGoal() != null){
                    agentsDone = false;
                }
            }
            if(agentsDone && subGoals.isEmpty()){
                finished = true;
            }
        }





        //Agent agent = currentState.getAgents().get(0);
        /*while (!subGoals.isEmpty()){
            Goal subgoal = subGoals.poll();
            System.err.println("Trying to solve sub-goal: " + subgoal.getLetter() + " at " + subgoal.getCoordinate().getRow() + "," + subgoal.getCoordinate().getColumn());
            subgoal.setCurrentMainGoal(true);
            agent.setStrategy(new StrategyBestFirst(new AStarHeuristic(currentState)));
            LinkedList<Node> plan = this.search(agent.getStrategy());
            System.err.println("Solution found for sub-goal: " + subgoal.getLetter() + " at " + subgoal.getCoordinate().getRow() + "," + subgoal.getCoordinate().getColumn());
            agent.appendSolution(plan);
            subgoal.setCurrentMainGoal(false);
            while(update());
            currentState = agent.getSolution().getLast();
            currentState.agents.get(0).setSolution(agent.getSolution());
            currentState.parent = null;
        }*/
        //TODO: Commented out for loop while working on single agent
		/*for (Agent agent : currentState.getAgents()) {
			agent.setStrategy(new StrategyBestFirst(new AStarHeuristic(currentState)));

			// TODO: change to threads
			agent.setSolution(this.search(agent.getStrategy()));
		}*/



    }

    public static LinkedList<Node> search(Strategy strategy, Node initialState) {
        strategy.addToFrontier(initialState);
        while (true) {
            //if (strategy.timeSpent() > 300) {
            //	return null;
            //}
            if (strategy.frontierIsEmpty()) {
                System.err.println("Agent " + initialState.thisAgent.getId() + " says: Frontier is empty");
                return null;
            }

            Node leafNode = strategy.getAndRemoveLeaf();
            // uncomment below for more debugging:
            //System.err.println("Chosen: " + (leafNode.action != null ? leafNode.action.toActionString() : ""));
            //leafNode.printState();

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
        int maxColumns = 0;
        while (line != null && !line.equals("")) {
            if (line.length() > maxColumns) {
                maxColumns = line.length();
            }
            for (int i = 0; i < line.length(); i++) {
                char id = line.charAt(i);
                if ('0' <= id && id <= '9') {				// Agents
                    Agent newAgent = new Agent(id, colors.get(id), new Coordinate(lineCount, i));
                    currentState.agents.add(newAgent);
                    agents.add(newAgent);
                } else if (id == '+') {						// Walls
                    Node.walls.put(new Coordinate(lineCount, i), true);
                } else if ('A' <= id && id <= 'Z') {		// Boxes
                    currentState.addBox(new Box(id, colors.get(id), new Coordinate(lineCount, i)));
                } else if ('a' <= id && id <= 'z') {		// Goals
                    Node.addGoal(new Goal(id, new Coordinate(lineCount, i)));
                }
            }

            line = in.readLine();
            lineCount++;
        }
        Node.totalRows = lineCount;
        Node.totalColumns = maxColumns;
    }

    public boolean update() throws IOException {

        String jointAction = "[";
        int noActions = 0;
        ArrayList<Agent> actAgent = Agent.sortById(agents);

        for (int i = 0; i < actAgent.size(); i++) {
            Command action = actAgent.get(i).act();
            String actionStr = "";
            latestActionArray[i] = action;
            if(action == null){
                noActions++;
                actionStr = "NoOp";
            } else {
                actionStr = action.toString();
            }
            jointAction += actionStr;
            if(i < actAgent.size() - 1){
                jointAction += ",";
            }
        }
        jointAction += "]";
        if(noActions == actAgent.size()){
            return false;
        }
        System.err.println("Sending command: " + jointAction + "\n");
        /*if(currentState.agents.get(currentState.agents.size() - 1).act(actionCount).equals("NoOp")){
            return false;
        }*/

        // Place message in buffer
        System.out.println(jointAction);

        // Flush buffer
        System.out.flush();

        // Disregard these for now, but read or the server stalls when its output buffer gets filled!
        String percepts = in.readLine();
        System.err.println(percepts);

        if (percepts == null)
            return false;

        String strip = percepts.replaceAll("\\[", "").replaceAll("\\]","").replaceAll("\\s", "");
        String[] returnVals = strip.split(",");
        this.latestServerOutput = returnVals;
        for(String returnVal : returnVals){
            if(returnVal.equals("false")){
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {

        // Use stderr to print to console
        System.err.println("Hello from MultiAgentClient. I am sending this using the error outputstream");
        try {
            MultiAgentClient client = new MultiAgentClient();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            // Got nowhere to write to probably
        }
    }

    public void findSubgoals(){
        Node.setGoalsPriority();
        subGoals = new PriorityQueue<>(20, subGoalComparator);
        for (Goal goal : Node.getGoalsByCoordinate().values()){
            subGoals.offer(goal);
        }
    }

    public static Comparator<Goal> subGoalComparator = new Comparator<Goal>() {
        @Override
        public int compare(Goal o1, Goal o2) {
            return (int) o2.getPriority() - o1.getPriority();
        }
    };

}
