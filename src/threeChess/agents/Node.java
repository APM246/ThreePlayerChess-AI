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
    private HashSet<ArrayList<Position>> map; // integer stores position of move in children ArrayList (REMOVE?)

    public Node(Board state)
    {
        this.state = cloneBoard(state); // maybe don't need clone here since clone() used in move() and populateChildren()
        colour = state.getTurn();
        children = new ArrayList<Node>(initial_capacity);
        map = new HashSet();
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

    public void populateChildren()
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
                        ArrayList<Position> new_move = new ArrayList<Position>(); 
                        new_move.add(position); new_move.add(new_position);
        
                        if (!map.contains(new_move) && state.isLegalMove(position, new_position))
                        {
                            ArrayList<Position> move = new ArrayList<Position>(); 
                            move.add(position); move.add(new_position);
                            map.add(move);
                            Board new_state = cloneBoard(state);
                            new_state.move(position, new_position);
                            Node child = new Node(new_state);
                            children.add(child);
                            System.out.println(position.getRow() + "," + position.getColumn());
                        }
                    }
                    catch (Exception e) {}
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
                            ArrayList<Position> new_move = new ArrayList<Position>(); 
                            new_move.add(position); new_move.add(new_position);

                            if (!map.contains(new_move) && state.isLegalMove(position, new_position))
                            {
                                ArrayList<Position> move = new ArrayList<Position>(); 
                                move.add(position); move.add(new_position);
                                map.add(move);
                                Board new_state = cloneBoard(state);
                                new_state.move(position, new_position);
                                Node child = new Node(new_state);
                                children.add(child);
                                System.out.println(position.getRow() + "," + position.getColumn());
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
