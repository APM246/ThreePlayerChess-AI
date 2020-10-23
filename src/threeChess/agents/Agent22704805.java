package threeChess.agents;

import threeChess.*;
import java.util.*;


public class Agent22704805 extends Agent {

    // UCT exploration constant
    static final double TEMPERATURE = Math.sqrt(2);
    // maximum time spent per move
    private long MAX_TIME;
    private Node root;

    public Agent22704805() {}

    /**
     * Play a move in the game. 
     * The agent is given a Board Object representing the position of all pieces, 
     * the history of the game and whose turn it is. 
     * They respond with a move represented by a pair (two element array) of positions: 
     * the start and the end position of the move.
     * @param board The representation of the game state.
     * @return a two element array of Position objects, where the first element is the 
     * current position of the piece to be moved, and the second element is the 
     * position to move that piece to.
     * **/
    public Position[] playMove(Board board) {
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
                root = root.move_node_map.get(move_arr);
                // For some reason one of the last 2 moves is not recognised, thus start again with an empty game tree
                if (root == null)
                {
                    root = new Node(board, null, null);
                    break;
                }
            }

            if (!root.has_populated_children) root.populateChildren();
        }

        MAX_TIME = board.getTimeLeft(root.colour)/25; // anytime algorithm, limit set to 1/25th of time left
        // Monte Carlo Tree Search
        long current_time = System.currentTimeMillis();
        while (System.currentTimeMillis() - current_time < MAX_TIME)
        {
            Node child = selectChild(root);
            Colour winner = simulateGame(child);
            backPropagate(child, root, winner);
        }

        root = selectBestNode(root);
        return root.last_move;
    }

    /**
     * Recursively select child node using Upper Confidence Bound for Trees algorithm until a leaf node reached
     * @param current_node node to start search from
     * @return an unvisited child node of the leaf node
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
     * Perform random rollout from node, by picking a random position and then 
     * random move from the position each time
     * @param start Node to start simulation from
     * @return winner of rollout
     */
    public Colour simulateGame(Node start)
    {
        Board current = cloneBoard(start.state);
        ArrayList<Position[]> all_moves;
        Random random_generator = new Random();
        // keep making moves until game completes
        while (!current.gameOver())
        {
            do
            {
                Object[] positions = current.getPositions(current.getTurn()).toArray();
                int random_index = random_generator.nextInt(positions.length);
                Position position = (Position) positions[random_index]; // random position found
                all_moves = getLegalMovesForPosition(position, current);
            }
            while (all_moves.isEmpty()); // this position has no legal moves, thus try again

            int random_index = random_generator.nextInt(all_moves.size());
            Position[] move = all_moves.get(random_index); // random move chosen
            try 
            {
                current.move(move[0], move[1]);
            }
            catch (Exception e) {} // errors already accounted for in getLegalMovesForPosition()
        }

        return current.getWinner();
    }

    /**
     * Back propagates result of match by updating number of visits and possibly win ratio
     * @param start first Node to update
     * @param root last Node to update
     * @param wonMatch winner of simulation
     */
    public void backPropagate(Node node, Node root, Colour winner)
    {
        while (node != root.parent)
        {
            node.num_visits++;
            if (Colour.values()[(node.colour.ordinal() + 2) % 3] == winner) node.num_wins++;
            node = node.parent; // move to parent and repeat
        }
    }

    /**
     * After time limit reached, examine all children of root and select the best child, i.e. the node
     * with the highest win percentage
     * @param root
     * @return best child of root
     */
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

    /**
     * Find all legal moves for a particular piece on the board. Used in rollouts.
     * @param position position to examine
     * @param state 
     * @return all possible moves that can be made for given position
     */
    public static ArrayList<Position[]> getLegalMovesForPosition(Position position, Board state)
    {
        ArrayList<Position[]> moves = new ArrayList<Position[]>();
        Piece piece = state.getPiece(position);
        Direction[][] steps = piece.getType().getSteps();
        int num_steps = piece.getType().getStepReps();
        HashSet<ArrayList<Position>> list_of_moves = new HashSet<ArrayList<Position>>();
            
        if (num_steps == 1)
        {
            for (Direction[] step: steps)
            {
                try 
                { 
                    Position new_position = state.step(piece, step, position); 
                    ArrayList<Position> new_move = new ArrayList<Position>(); 
                    new_move.add(position); new_move.add(new_position);
        
                    if (!list_of_moves.contains(new_move) && state.isLegalMove(position, new_position))
                    {
                        list_of_moves.add(new_move);
                        moves.add(new Position[] {position, new_position});
                    }
                }
                catch (Exception e) {}
            }
        }

        // only applies to Queen, Bishop and Rook
        else 
        {
            for (Direction[] step: steps)
            {
                Position new_position = position;
                boolean reverse = false;
                for (int i = 0; i < num_steps; i++)
                {
                    try
                    {
                        new_position = state.step(piece, step, new_position, reverse);
                        // another player's part of board has been entered
                        if (new_position.getColour() != position.getColour()) reverse = true;
                        ArrayList<Position> new_move = new ArrayList<Position>(); 
                        new_move.add(position); new_move.add(new_position);

                        // check that move has not already been added and that it is legal
                        if (!list_of_moves.contains(new_move) && state.isLegalMove(position, new_position))
                        {
                            list_of_moves.add(new_move);
                            moves.add(new Position[] {position, new_position});
                        }
                        else break;
                    }
                        
                    catch (Exception e) {break;} // piece moved off board 
                }
            }
        }
        
        return moves;
    }

    /**
     * clones a Board instance
    */
    public Board cloneBoard(Board board)
    {
        try
        {
            return (Board) board.clone();
        }
    
        catch (Exception e)
        {
            return null;
        }
    }

    public String toString() {
        return "22704805";
    }

    public void finalBoard(Board finalBoard) {

    }

    private class Node {

        public int num_visits;
        public int num_wins;
        // internal representation of ThreeChess game
        public Board state;
        // represents current turn
        public final Colour colour;
        public Node parent;
        // move that led to this node being created
        public Position[] last_move; 
        // maps a particular legal move to its resulting node
        public HashMap<ArrayList<Position>, Node> move_node_map;
        // true if all possible children have been added to move_node_map
        public boolean has_populated_children;
        // initial capacity of move node map
        public static final int INITIAL_CAPACITY = 40;
    
        public Node(Board state, Node parent, Position[] move)
        {
            this.state = state;
            colour = state.getTurn();
            this.parent = parent;
            has_populated_children = false;
            last_move = move;
            move_node_map = new HashMap<>(INITIAL_CAPACITY);
        }
    
        /**
         * Adds a child to the move_node_map. A helper method for populateChildren()
         * @param position position of piece before move
         * @param new_position position of piece after move
         * @throws Exception move is not legal
         */
        private void addChild(Position position, Position new_position) throws Exception
        {
            ArrayList<Position> new_move = new ArrayList<Position>(); 
            new_move.add(position); new_move.add(new_position);
            // checks that move has not already been considered and it is a legal move
            if (!move_node_map.containsKey(new_move) && state.isLegalMove(position, new_position))
            {
                Board new_state = cloneBoard(state);
                new_state.move(position, new_position);
                Node child = new Node(new_state, this, new Position[] {position, new_position});
                move_node_map.put(new_move, child);
            }
            else throw new Exception();
        }
    
        /**
         * Examines all possible legal moves from current state and adds the corresponding Nodes to 
         * move_node_map
         */
        public void populateChildren()
        {
            has_populated_children = true;
            Set<Position> positions = state.getPositions(colour);
    
            for (Position position: positions)
            {
                Piece piece = state.getPiece(position);
                Direction[][] steps = piece.getType().getSteps();
                int num_steps = piece.getType().getStepReps();
                
                if (num_steps == 1)
                {
                    for (Direction[] step: steps)
                    {
                        try 
                        { 
                            Position new_position = state.step(piece, step, position); 
                            addChild(position, new_position);
                        }
                        catch (Exception e) {} // if piece has moved off board
                    }
                }
    
                // only applies to Rook, Bishop and Queen
                else 
                {
                    for (Direction[] step: steps)
                    {
                        Position new_position = position;
                        boolean reverse = false; // when entering another player's part of the board, reverse the directions
                        for (int i = 0; i < num_steps; i++)
                        {
                            try
                            {
                                new_position = state.step(piece, step, new_position, reverse);
                                // another player's part of board entered 
                                if (new_position.getColour() != position.getColour()) reverse = true;
                                addChild(position, new_position);
                            }
                            catch (Exception e) {break;} // moved off board 
                        }
                    }
                }
            }
        }
    }
}