package threeChess.agents;

import threeChess.*;
import java.util.*;


public class Agent22704805 extends Agent {

    static final double TEMPERATURE = Math.sqrt(2);

    public Position[] playMove(Board board) {
        int iterations = 0; // change to time (limit of 10 seconds? etc)
        int max_iterations = 40; // EXPERIMENT WITH 
        

        // code to jump to child's child's node and set as root (need to check if that root has been visited before though),
        // use getMove()

        Node root = new Node(board, null);
        root.populateChildren();
        //System.out.println(root.children.size());

        while (iterations < max_iterations)
        {
            iterations++;
            Node current_node = root;
            Node leaf = selectChild(current_node);
            Colour winner = simulateGame(leaf);
            backPropagate(leaf, root, winner);
        }

        Node best_move = selectBestNode();
        return null;
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
            if (current.colour == winner) current.num_wins++;
            current = current.parent;
        }
    }

    public Node selectBestNode()
    {
        return null;
    }

    public String toString() {
        return "22704805";
    }

    public void finalBoard(Board finalBoard) {

    }
    
}
