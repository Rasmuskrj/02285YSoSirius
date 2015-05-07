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

    private PriorityQueue<Goal> subGoals;
    private ArrayList<Agent> agents = new ArrayList<>();
    private String[] latestServerOutput = null;
    private Command[] latestActionArray = null;
    private Boolean[] agentErrorState = null;

    // uncomment two lines below if testing without server and comment the third line
    //FileReader fr = new FileReader("levels/MAsimple1.lvl");
    //private BufferedReader in = new BufferedReader(fr);
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public MultiAgentClient() throws IOException {
        readMap();
        Node.computeGoalDistance();
        findSubgoals();
        currentState.printState();
        latestActionArray = new Command[agents.size()];
        agentErrorState = new Boolean[agents.size()];
        assignSubgoals();
        in.readLine();
        while(!subGoals.isEmpty()) {
            for (Agent agent : agents) {
                //TODO: Only replan if needed
                Node myinitalState = currentState.getCopy();
                myinitalState.thisAgent = agent;
                agent.setStrategy(new StrategyBestFirst(new AStarHeuristic(myinitalState)));
                LinkedList<Node> plan = this.search(agent.getStrategy(), myinitalState);
                agent.appendSolution(plan);
            }

            //Execute solutions as long as possible
            while (update()) {
                currentState.changeState(latestActionArray);
            }
            boolean error = false;
            for(int i = 0; i < latestActionArray.length; i++){
                if(latestActionArray[i].equals("false")){
                    error = true;
                    agentErrorState[i] = true;
                } else {
                    agentErrorState[i] = false;
                }
            }
            if(!error){
                for(Agent a : agents){
                    subGoals.poll();
                }
            } else {
                //TODO: Resolve conflicts
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

    private void assignSubgoals() {
        Iterator iter = subGoals.iterator();
        while (iter.hasNext()){
            Goal subGoal = (Goal)iter.next();
            for(Agent agent : agents){
                boolean goalAssigned = false;
                for(Box box : currentState.getBoxesByCoordinate().values()){
                    if(box.getColor().equals(agent.getColor()) && box.getLetter() == Character.toUpperCase(subGoal.getLetter())){
                        agent.setCurrentSubGoal(subGoal);
                        goalAssigned = true;
                        break;
                    }
                }
                if(goalAssigned){
                    break;
                }
            }
        }
    }

    public LinkedList<Node> search(Strategy strategy, Node initialState) {
        strategy.addToFrontier(initialState);
        while (true) {
            //if (strategy.timeSpent() > 300) {
            //	return null;
            //}
            if (strategy.frontierIsEmpty()) {
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
                    System.err.println(newAgent.getId());
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

        for (int i = 0; i < agents.size(); i++) {
            Command action = agents.get(i).act();
            String actionStr = "";
            latestActionArray[i] = action;
            if(action == null){
                noActions++;
                actionStr = "NoOp";
            } else {
                actionStr = action.toString();
            }
            jointAction += actionStr;
            if(i < agents.size() - 1){
                jointAction += ",";
            }
        }
        jointAction += "]";
        if(noActions == agents.size()){
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

        percepts.replaceAll("\\[\\]", "");
        String[] returnVals = percepts.split(",");
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
