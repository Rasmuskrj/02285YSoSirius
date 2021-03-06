package client;

import java.util.HashSet;

public abstract class Strategy {

	public HashSet<Node> explored;
	public long startTime = System.currentTimeMillis();
	
	public Strategy() {
		explored = new HashSet< Node >();
	}

	public void addToExplored(Node n) {
		explored.add(n);
	}

	public boolean isExplored(Node n) {
		return explored.contains(n);
	}

	public int countExplored() {
		return explored.size();
	}


	public String searchStatus() {
		return String.format("#Explored: %4d, #Frontier: %3d, Time: %3.2f s \t", countExplored(), countFrontier(), timeSpent());
	}

	
	public float timeSpent() {
		return (System.currentTimeMillis() - startTime) / 1000f;
	}

	public abstract Node getAndRemoveLeaf();

	public abstract void addToFrontier(Node n);

	public abstract boolean inFrontier(Node n);

	public abstract int countFrontier();

	public abstract boolean frontierIsEmpty();
	
	public abstract String toString();

}
