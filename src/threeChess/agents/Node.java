package threeChess.agents;

import java.util.*;

import threeChess.*;

/**
 * use array or other data structure to store children? 36 is waste of space, array won't be filled sometimes
 */

public class Node {

    public int num_visits;
    public int num_wins;
    public double win_ratio;
    public Board state;
    public ArrayList<Node> children;
    public final int initial_capacity = 31; // idk?
    private final Colour colour;
    private HashMap<ArrayList<Position>, Integer> map; // integer stores position of move in children ArrayList

    public Node(Board state)
    {
        this.state = cloneBoard(state); // maybe don't need clone here since clone() used in move() and populateChildren()
        colour = state.getTurn();
        children = new ArrayList<Node>(initial_capacity);
        populateChildren();
    }

    private Board cloneBoard(Board board)
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

    private void populateChildren()
    {
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
                        if (state.isLegalMove(position, new_position))
                        {
                            Board new_state = cloneBoard(state);
                            new_state.move(position, new_position);
                            Node child = new Node(new_state);
                            children.add(child);
                        }
                    }
                    catch (Exception e) {e.printStackTrace();}
                }
            }

            else 
            {
                Position original = Position.values()[position.ordinal()]; // REVERT BACK 
                for (Direction[] step: steps)
                {
                    for (int i = 0; i < num_steps; i++)
                    {
                        try
                        {
                            Position new_position = state.step(piece, step, position);
                            if (state.isLegalMove(position, new_position))
                            {
                                Board new_state = cloneBoard(state);
                                new_state.move(position, new_position);
                                Node child = new Node(new_state);
                                children.add(child);
                                position = new_position;
                            }
                            else break;
                        }
                        
                        catch (Exception e) {break;} // moved off board 
                    }

                    position = original;
                }
            }
        }
    }
}
