package client;

import heuristics.Heuristic;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class StrategyBestFirst extends Strategy {
	private static Heuristic heuristic;
	private PriorityQueue<Node> frontier; //TreeMap<Integer, ArrayDeque<Node>> frontier;
	
	public static Comparator<Node> nodeComparator = new Comparator<Node>(){  
        @Override
        public int compare(Node node1, Node node2) {
            return (int) (node1.getF() - node2.getF());
        }
    };
    
	public StrategyBestFirst(Heuristic h) {
		super();
		heuristic = h;
		frontier = new PriorityQueue<Node>(10, nodeComparator);//new TreeMap<Integer, ArrayDeque<Node>>();
	}
	
	public Node getAndRemoveLeaf() {
		Node leaf = null;
		leaf = frontier.poll();
		/*
		leaf = frontier.firstEntry().getValue().pollFirst();
		if (frontier.firstEntry().getValue().size() == 0) {
			frontier.remove(frontier.firstKey());
		}
		*/
		return leaf;
	}

	public void addToFrontier(Node n) {
		/*
		int key = heuristic.f(n);
		if (frontier.get(key) == null) {
			frontier.put(key, new ArrayDeque<Node>());
		}
		frontier.get(key).add(n);
		*/
		if(frontier.size() % 1000 == 0){
			System.err.println(this.searchStatus());
		}
		n.setF(heuristic.f(n));
		frontier.offer(n);
	}

	public int countFrontier() {
		/*
		int count = 0;
		for (Map.Entry<Integer, ArrayDeque<Node>> nodeList : frontier.entrySet()) {
			count += nodeList.getValue().size();
		}
		return count;
		*/
		return frontier.size();
	}

	public boolean frontierIsEmpty() {
		/*
		boolean isEmpty = true;
		for (Map.Entry<Integer, ArrayDeque<Node>> nodeList : frontier.entrySet()) {
			if (nodeList.getValue() != null) {
				isEmpty = false;
			}
		}
		*/
		return frontier.isEmpty();
	}

	public boolean inFrontier(Node n) {
		/*
		boolean inFrontier = false;
		for (Map.Entry<Integer, ArrayDeque<Node>> nodeList : frontier.entrySet()) {
			if (nodeList.getValue().contains(n)) {
				inFrontier = true;
			}
		}
		*/
		return frontier.contains(n);
	}

	public String toString() {
		return "Best-first Search (PriorityQueue) using " + heuristic.toString();
	}
}
