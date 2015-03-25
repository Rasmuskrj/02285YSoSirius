package client;

import heuristics.Heuristic;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.TreeMap;

public class StrategyBestFirst extends Strategy {
	private Heuristic heuristic;
	private TreeMap<Integer, ArrayDeque<Node>> frontier;
	public StrategyBestFirst(Heuristic h) {
		super();
		heuristic = h;
		frontier = new TreeMap<Integer, ArrayDeque<Node>>();
	}
	public Node getAndRemoveLeaf() {
		Node leaf = null;
		leaf = frontier.firstEntry().getValue().pollFirst();
		if (frontier.firstEntry().getValue().size() == 0) {
			frontier.remove(frontier.firstKey());
		}
		return leaf;
	}

	public void addToFrontier(Node n) {
		int key = heuristic.f(n);
		if (frontier.get(key) == null) {
			frontier.put(key, new ArrayDeque<Node>());
		}
		frontier.get(key).add(n);
	}

	public int countFrontier() {
		int count = 0;
		for (Map.Entry<Integer, ArrayDeque<Node>> nodeList : frontier.entrySet()) {
			count += nodeList.getValue().size();
		}
		return count;
	}

	public boolean frontierIsEmpty() {
		boolean isEmpty = true;
		for (Map.Entry<Integer, ArrayDeque<Node>> nodeList : frontier.entrySet()) {
			if (nodeList.getValue() != null) {
				isEmpty = false;
			}
		}
		return isEmpty;
	}

	public boolean inFrontier(Node n) {
		boolean inFrontier = false;
		for (Map.Entry<Integer, ArrayDeque<Node>> nodeList : frontier.entrySet()) {
			if (nodeList.getValue().contains(n)) {
				inFrontier = true;
			}
		}
		return inFrontier;
	}

	public String toString() {
		return "Best-first Search (PriorityQueue) using " + heuristic.toString();
	}
}
