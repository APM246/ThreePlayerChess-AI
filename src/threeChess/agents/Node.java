package threeChess.agents;

import java.util.*;

import threeChess.*;

public class Node {

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
     * clones a Board instance
     */
    public static Board cloneBoard(Board board)
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