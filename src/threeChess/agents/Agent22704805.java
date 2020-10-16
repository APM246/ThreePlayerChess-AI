package threeChess.agents;

import threeChess.*;
import java.util.*;


public class Agent22704805 extends Agent {

    static final double TEMPERATURE = Math.sqrt(2);
    private Node root;
    private long time;
    private int num_moves = 0;

    public Agent22704805(){} // argumentless constructor

    public Position[] playMove(Board board) {
        num_moves++;
        long current_time = System.currentTimeMillis();
        int iterations = 0; // change to time (limit of 10 seconds? etc)
        int max_iterations = 800; // EXPERIMENT WITH 
        
        int move_count = board.getMoveCount();

        // setting up root for first time
        if (move_count < 3)
        {
            root = new Node(board, null, null);
            root.populateChildren();
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
        }

        if (!root.has_populated_children) root.populateChildren();

        // Monte Carlo Tree Search
        while (iterations < max_iterations)
        {
            iterations++;
            Node leaf = selectChild(root);
            Colour winner = simulateGame(leaf);
            backPropagate(leaf, root, winner);
        }

        Node best_move = selectBestNode(root);
        root = best_move;
        time += System.currentTimeMillis() - current_time;
        System.out.println("AVERAGE TIME IS: " + time/num_moves);
        return best_move.last_move;
    }

    /**
     * recursively select child node using Upper Confidence Bound algorithm (selection policy) until leaf node reached
     */
    public Node selectChild(Node current_node)
    {
        while (true)
        {
            int num_children = current_node.children.size();
            double max = -1;
            int best_child = -1;
            for (int i = 0; i < num_children; i++)
            {
                Node child = current_node.children.get(i);
                if (child.num_visits == 0) return child; // new unvisited node found

                // calculate UCB1 value for each child
                double exploration = Math.sqrt(Math.log(current_node.num_visits)/child.num_visits);
                double ucb1 = ((double) child.num_wins)/((double) child.num_visits) + TEMPERATURE*exploration; 
                if (ucb1 > max)
                {
                    max = ucb1;
                    best_child = i;
                }
            }

            current_node = current_node.children.get(best_child);
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
            catch (Exception e) {} // already accounted for in getLegalMovesForPosition()
        }


        return current.getWinner();
    }

    /**
     * back propagates result of match by updating win ratio and number of visits
     * @param start
     * @param root
     * @param wonMatch
     */
    public void backPropagate(Node start, Node root, Colour winner)
    {
        Node current = start;
        while (current != root.parent)
        {
            current.num_visits++;
            if (Colour.values()[(current.colour.ordinal() + 2) % 3] == winner) current.num_wins++;
            current = current.parent;
        }
    }

    public Node selectBestNode(Node root)
    {
        Iterator<Node> iterator = root.children.iterator();
        double max = -1;
        Node best_node = null;

        while (iterator.hasNext())
        {
            Node node = iterator.next();
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
