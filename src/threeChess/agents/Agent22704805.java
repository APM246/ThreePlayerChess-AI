package threeChess.agents;

import threeChess.*;
import java.util.*;


public class Agent22704805 extends Agent {

    static final double TEMPERATURE = Math.sqrt(2);
    private long MAX_TIME;
    private Node root;

    public Agent22704805() {}

    public Position[] playMove(Board board) {
        int move_count = board.getMoveCount();

        // setting up root for first time
        if (move_count < 3)
        {
            root = new Node(board, null, null);
            root.populateChildren();
            MAX_TIME = 2000; // anytime algorithm, limit set to 2 seconds 
        }
        // move root using last 2 moves
        else
        {
            for (int i = 2; i > 0; i--)
            {
                Position[] move = board.getMove(move_count - i);
                ArrayList<Position> move_arr = new ArrayList<Position>();
                move_arr.add(move[0]); move_arr.add(move[1]);
                if (!root.has_populated_children) root.populateChildren();
                Node node = root.move_node_map.get(move_arr);
                root = node;
            }

            if (!root.has_populated_children) root.populateChildren();
        }

        int num_iterations = 0;
        // Monte Carlo Tree Search
        long current_time = System.currentTimeMillis();
        while (System.currentTimeMillis() - current_time < MAX_TIME)
        {
            Node leaf = selectChild(root);
            Colour winner = simulateGame(leaf);
            backPropagate(leaf, root, winner);
            num_iterations++;
        }

        System.out.println("number: " + num_iterations);
        root = selectBestNode(root);
        return root.last_move;
    }

    /**
     * recursively select child node using Upper Confidence Bound algorithm (selection policy) until leaf node reached
     */
    public Node selectChild(Node current_node)
    {
        while (true)
        {
            double max = -1;
            ArrayList<Position> best_child_key = null;
            Set<ArrayList<Position>> keys = current_node.move_node_map.keySet();

            for (ArrayList<Position> move: keys)
            {
                Node child = current_node.move_node_map.get(move);
                if (child.num_visits == 0) return child; // new unvisited node found

                // calculate UCB1 value for each child
                double exploration = Math.sqrt(Math.log(current_node.num_visits)/child.num_visits);
                double ucb1 = ((double) child.num_wins)/((double) child.num_visits) + TEMPERATURE*exploration; 
                if (ucb1 > max)
                {
                    max = ucb1;
                    best_child_key = move;
                }
            }

            current_node = current_node.move_node_map.get(best_child_key);
            if (current_node.state.gameOver()) return current_node; // terminal state reached
            if (!current_node.has_populated_children) current_node.populateChildren(); 
        }
    }

    /**
     * perform random rollout from new unvisited node
     * @param start
     */
    public Colour simulateGame(Node start)
    {
        Board current = Node.cloneBoard(start.state);
        ArrayList<Position[]> all_moves;
        Random random_generator = new Random();

        while (!current.gameOver())
        {
            do
            {
                Object[] positions = current.getPositions(current.getTurn()).toArray();
                int random_index = random_generator.nextInt(positions.length);
                Position position = (Position) positions[random_index];
                all_moves = Node.getLegalMovesForPosition(position, current);
            }
            while (all_moves.isEmpty());

            int random_index = random_generator.nextInt(all_moves.size());
            Position[] move = all_moves.get(random_index);
            try 
            {
                current.move(move[0], move[1]);
            }
            catch (Exception e) {} // errors already accounted for in getLegalMovesForPosition()
        }

        return current.getWinner();
    }

    /**
     * back propagates result of match by updating win ratio and number of visits
     * @param start
     * @param root
     * @param wonMatch
     */
    public void backPropagate(Node node, Node root, Colour winner)
    {
        while (node != root.parent)
        {
            node.num_visits++;
            if (Colour.values()[(node.colour.ordinal() + 2) % 3] == winner) node.num_wins++;
            node = node.parent;
        }
    }

    public Node selectBestNode(Node root)
    {
        Iterator<ArrayList<Position>> iterator = root.move_node_map.keySet().iterator();
        double max = -1;
        Node best_node = null;

        while (iterator.hasNext())
        {
            ArrayList<Position> key = iterator.next();
            Node node = root.move_node_map.get(key);
            double win_percentage = ((double) node.num_wins)/((double) node.num_visits);
            if (win_percentage > max)
            {
                max = win_percentage;
                best_node = node;
            }
        }

        return best_node;
    }

    public String toString() {
        return "22704805";
    }

    public void finalBoard(Board finalBoard) {

    }   
}