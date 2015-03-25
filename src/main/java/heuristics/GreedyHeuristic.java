package heuristics;

import client.Node;

public class GreedyHeuristic extends Heuristic {

	public GreedyHeuristic(Node initialState) {
		super(initialState);
	}

	public int f(Node n) {
		return h(n);
	}

	public String toString() {
		return "Greedy evaluation";
	}
}
