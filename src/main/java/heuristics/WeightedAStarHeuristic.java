package heuristics;

import client.Node;

public class WeightedAStarHeuristic extends Heuristic {
	private int W;

	public WeightedAStarHeuristic(Node initialState) {
		super(initialState);
		W = 5; // You're welcome to test this out with different values, but for the reporting part you must at least indicate benchmarks for W = 5
	}

	public int f(Node n) {
		return n.g() + W * h(n);
	}

	public String toString() {
		return String.format("WA*(%d) evaluation", W);
	}
}
