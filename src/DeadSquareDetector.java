import game.board.compact.BoardCompact;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

public class DeadSquareDetector {

    private DeadSquareDetector() {
        throw new IllegalStateException("Class should not be instantiated.");
    }

    private static final int TARGET_FLAG = 4101;
    private static final int WALL_FLAG = 1030;

    public static boolean[][] detect(BoardCompact board) {
        // return value of board. true where tile is dead, false where alive
        var res = new boolean[board.width()][board.height()];

        //This is where the box should go
        var targets = new HashSet<Position>();
        for (var x = 0; x < board.width(); x++) {
            for (var y = 0; y < board.height(); y++) {
                // also detect entities and objects on top of targets
                if (board.tile(x, y) >= TARGET_FLAG)
                    targets.add(new Position(x, y));
            }
        }

        //looping through all targets
        for (var target : targets) {
            var targetNode = new Node(target, true, null);
            //Target is always accessible, hence mark it as true
            res[target.x()][target.y()] = true;

            var frontier = new ArrayDeque<Node>();
            frontier.add(targetNode);

            //BFS?DFS?
            while(!frontier.isEmpty()) {
                var node = frontier.removeFirst();
                if (canReachParent(node, board)) {
                    node.setIsAccessible(true);
                }
                // change boolean board
                res[node.getPosition().x()][node.getPosition().y()] |= node.isAccessible();
                // add neighbours to queue
                addNeighbouringNodes(node, frontier, res, board);
            }
        }

        // flip boolean values in res cus it currently contains whether a state can access a goal point
        for (var i = 0; i < res.length; i++) {
            for (var j = 0; j < res[i].length; j++) {
                res[i][j] = !res[i][j];
            }
        }

        return res;
    }

    private static boolean canReachParent(Node node, BoardCompact board) {
        if (node.getParent() == null) return false;
        var parentPosition = node.getParent().getPosition();
        // parent is above the node
        if (parentPosition.x() == node.getPosition().x() && parentPosition.y() == node.getPosition().y() - 1) {
            // box needs to be pushed up from below
            // check what type of tile is below
            var belowPosition = new Position(node.getPosition().x(), node.getPosition().y() + 1);
            return belowPosition.y() < board.height() && board.tile(belowPosition.x(), belowPosition.y()) != WALL_FLAG;
        }
        // parent is below node
        else if (parentPosition.x() == node.getPosition().x() && parentPosition.y() == node.getPosition().y() + 1) {
            // box need to be pushed down
            var abovePosition = new Position(node.getPosition().x(), node.getPosition().y() - 1);
            return abovePosition.y() > 0 && board.tile(abovePosition.x(), abovePosition.y()) != WALL_FLAG;
        }
        // parent is right of node
        else if (parentPosition.x() == node.getPosition().x() + 1 && parentPosition.y() == node.getPosition().y()) {
            // box needs to be pushed right
            var leftPosition = new Position(node.getPosition().x() - 1, node.getPosition().y());
            return leftPosition.x() > 0 && board.tile(leftPosition.x(), leftPosition.y()) != WALL_FLAG;
        }
        // parent is left on node
        else if (parentPosition.x() == node.getPosition().x() - 1 && parentPosition.y() == node.getPosition().y()) {
            var rightPosition = new Position(node.getPosition().x() + 1 , node.getPosition().y());
            return rightPosition.x() < board.width() && board.tile(rightPosition.x(), rightPosition.y()) != WALL_FLAG;
        }
        return false;
    }

    private static void addNeighbouringNodes(Node node, ArrayDeque<Node> queue, boolean[][] canAccess, BoardCompact board) {
        // add nodes only if parent can access a goal state
        if (!node.isAccessible()) {
            return;
        }
        var position = node.getPosition();
        var neighbouringPositions = new ArrayList<Position>();

        // add above node
        if (position.y() != 0) neighbouringPositions.add(new Position(position.x(), position.y() - 1));
        // add below postition
        if (position.y() < board.height() - 1) neighbouringPositions.add(new Position(position.x(), position.y() + 1));
        // add left pos
        if (position.x() > 0) neighbouringPositions.add(new Position(position.x() - 1, position.y()));
        // right pos
        if (position.x() < board.width() - 1) neighbouringPositions.add(new Position(position.x() + 1, position.y()));

        for (Position neighbouringPosition : neighbouringPositions) {
            // dont add neighbour if pos has a wall on it
            // dont add neighbour if pos has been explored already and it can access a goal state
            if (board.tile(neighbouringPosition.x(), neighbouringPosition.y()) != WALL_FLAG
                    && !canAccess[neighbouringPosition.x()][neighbouringPosition.y()])
                queue.add(new Node(neighbouringPosition, false, node));
        }
    }

    static class Node {

        private Position position;
        private boolean isAccessible;
        private final Node parent;

        Node(Position position, boolean isAccessible, Node parent) {
            this.position = position; this.isAccessible = isAccessible; this.parent = parent;
        }

        public Node getParent() {
            return this.parent;
        }

        public boolean isAccessible() {
            return isAccessible;
        }

        public void setIsAccessible(boolean isAccessible) {
            this.isAccessible = isAccessible;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }
    }
}
