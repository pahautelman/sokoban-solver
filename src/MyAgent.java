import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.CAction;
import game.actions.compact.CMove;
import game.actions.compact.CPush;
import game.board.compact.BoardCompact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import static java.lang.System.out;

/**
 * The simplest Tree-DFS agent.
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {

	private static final int TARGET_FLAG = 4101;
	private static final int BOX_FLAG = 1033;

	protected BoardCompact board;
	protected int searchedNodes;
	
	@Override
	protected List<EDirection> think(BoardCompact board) {
		this.board = board;
		searchedNodes = 0;
		long searchStartMillis = System.currentTimeMillis();

		var deadSquares = DeadSquareDetector.detect(board);


		var heuristicFunction = new TaxicabDistance();
		var result = search(deadSquares, heuristicFunction); // the number marks how deep we will search (the longest plan we will consider)

		long searchTime = System.currentTimeMillis() - searchStartMillis;
        
        if (verbose) {
            out.println("Nodes visited: " + searchedNodes);
            out.printf("Performance: %.1f nodes/sec\n",
                        ((double)searchedNodes / (double)searchTime * 1000));
        }
		
		return result;
	}

	private List<EDirection> search(boolean[][] deadSquares, HeuristicFunction heuristicFunction) {
		var heuristicCost = new HashMap<BoardCompact, Double>();
		heuristicCost.put(board, heuristicFunction.call(board));

		var pathCost = new HashMap<BoardCompact, Double>();
		pathCost.put(board, 0d);

		var frontier = new PriorityQueue<Node>();
		frontier.add(new Node(board, null, null, 0, heuristicCost.get(board)));

		while (!frontier.isEmpty()) {
			var curNode = frontier.poll();
			assert curNode != null;
			var curState = curNode.getState();
			if (curState.isVictory()) {
				return getSolution(curNode);
			}

			// COLLECT POSSIBLE ACTIONS
			var actions = new ArrayList<CAction>(4);

			for (CMove move : CMove.getActions()) {
				// if move is possible
				if (move.isPossible(curState)) {
					actions.add(move);
				}
			}
			for (CPush push : CPush.getActions()) {
				// if move does not push the box to a dead state
				// and if move is possible
				if (push.isPossible(curState) && !deadSquares[curState.playerX + 2*push.getDirection().dX][curState.playerY + 2*push.getDirection().dY]) {
					actions.add(push);
				}
			}

			for (var action : actions) {
				// PERFORM THE ACTION

				var neighbour = performAction(curState, action);
				// cost is increased by 1 per action
				double pathCostToNeighbour = curNode.getCost() + 1;

				if (pathCostToNeighbour < pathCost.getOrDefault(neighbour, Double.POSITIVE_INFINITY)) {
					pathCost.put(neighbour, pathCostToNeighbour);
					heuristicCost.computeIfAbsent(neighbour, heuristicFunction::call);
					frontier.add(new Node(neighbour, curNode, action.getDirection(), pathCostToNeighbour, heuristicCost.get(neighbour)));
				}
			}
		}
		// todo: weblab might require null
		return new ArrayList<>();
	}

	private BoardCompact performAction(BoardCompact state, CAction action) {
		var resultingState = state.clone();
		if (action.isPossible(resultingState)) {
			action.perform(resultingState);
		} else {
			throw new RuntimeException("da fuck??");
		}
		return resultingState;
	}

	private List<EDirection> getSolution(Node goalNode) {
		var actions = new LinkedList<EDirection>();

		var node = goalNode;
		while (node.getParentNode() != null) {
			actions.addFirst(node.getActionFromParentState());
			node = node.getParentNode();
		}
		return actions;
	}

	private static class Node implements Comparable<Node> {
		private final BoardCompact state;
		private final Node parentNode;
		private final EDirection actionFromParentState;
		private final double cost;
		private final double heuristicCost;

		Node(BoardCompact state, Node parentNode, EDirection actionFromParentState, double cost, double heuristicCost) {
			this.state = state; this.parentNode = parentNode; this.actionFromParentState = actionFromParentState;
			this.cost = cost; this.heuristicCost = heuristicCost;
		}

		public BoardCompact getState() {
			return state;
		}

		public Node getParentNode() {
			return parentNode;
		}

		public EDirection getActionFromParentState() {
			return this.actionFromParentState;
		}

		public double getCost() {
			return cost;
		}

		public double getHeuristicCost() {
			return heuristicCost;
		}

		@Override
		public int compareTo(Node o) {
			var totalScore = this.cost + this.heuristicCost;
			var otherTotalScore = o.getCost() + o.getHeuristicCost();
			if (totalScore == otherTotalScore) return 0;
			return totalScore < otherTotalScore ? -1 : 1;
		}
	}

	private interface HeuristicFunction {
		Double call(BoardCompact board);
	}

	private static class TaxicabDistance implements HeuristicFunction {

		@Override
		public Double call(BoardCompact board) {
			var targets = new ArrayList<Position>();
			var boxes = new ArrayList<Position>();

			for (var x = 0; x < board.width(); x++) {
				for (var y = 0; y < board.height(); y++) {
					if (board.tile(x, y) == TARGET_FLAG) targets.add(new Position(x, y));
					if (board.tile(x, y) == BOX_FLAG) boxes.add(new Position(x, y));

				}
			}

			if (boxes.size() != board.boxCount - board.boxInPlaceCount) {
				throw new RuntimeException("fucked up");
			}

			var heuristicCost = 0d;
			for (var target : targets) {
				var minDist = Integer.MAX_VALUE;
				for (var box : boxes) {
					var taxicabDist = Math.abs(target.x() - box.x()) + Math.abs(target.y() - box.y());
					minDist = Math.min(minDist, taxicabDist);
				}
				heuristicCost += minDist;
			}
			return heuristicCost;
		}
	}

	private static class MCTS implements HeuristicFunction {

		@Override
		public Double call(BoardCompact board) {
			return 0d;
		}
	}
}
