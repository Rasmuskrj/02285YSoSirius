package heuristics;

import client.Node;

public class AStarHeuristic extends Heuristic {
	public AStarHeuristic(Node initialState) {
		super(initialState);
	}

	public int f(Node n) {
		return n.g() + h(n);
	}

	public String toString() {
		return "A* evaluation";
	}
}
