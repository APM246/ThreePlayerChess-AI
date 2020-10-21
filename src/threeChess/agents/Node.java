package threeChess.agents;

import java.util.*;

import threeChess.*;

/**
 * use array or other data structure to store children? 36 is waste of space, array won't be filled sometimes
 */

public class Node {

    public int num_visits;
    public int num_wins;
    public Board state;
    public final Colour colour;
    public Node parent;
    public Position[] last_move; // move that led to this node being created
    public boolean has_populated_children;
    public HashMap<ArrayList<Position>, Node> move_node_map; // maps a particular move to its resulting node

    public static final int INITIAL_CAPACITY = 40; // empirically tested

    public Node(Board state, Node parent, Position[] move)
    {
        this.state = state;
        colour = state.getTurn();
        this.parent = parent;
        has_populated_children = false;
        last_move = move;
        move_node_map = new HashMap<>(INITIAL_CAPACITY);
    }


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

    public void addChild(Position position, Position new_position) throws Exception
    {
        ArrayList<Position> new_move = new ArrayList<Position>(); 
        new_move.add(position); new_move.add(new_position);
        if (!move_node_map.containsKey(new_move) && state.isLegalMove(position, new_position))
        {
            Board new_state = cloneBoard(state);
            new_state.move(position, new_position);
            Node child = new Node(new_state, this, new Position[] {position, new_position});
            move_node_map.put(new_move, child);
        }
        else throw new Exception();
    }

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
                    catch (Exception e) {}
                }
            }

            else 
            {
                for (Direction[] step: steps)
                {
                    Position new_position = position;
                    Position intermediate_position;
                    boolean reverse = false; // when entering another player's part of the board, reverse the directions
                    boolean tried_switch = false; // when Bishop or Queen gets to centre of board and can go other directions
                    for (int i = 0; i < num_steps; i++)
                    {
                        try
                        {
                            intermediate_position = state.step(piece, step, new_position, reverse);
                            if (intermediate_position.getColour() != position.getColour()) 
                            {
                                // when Bishop or Queen gets to centre of board and can go other directions
                                if (!tried_switch && piece.getType() != PieceType.ROOK && step.length > 1)
                                {
                                    tried_switch = true;
                                    Direction[] new_step = new Direction[] {step[1], step[0]};
                                    for (int j = i; j < num_steps; j++)
                                    {
                                        try 
                                        {
                                            new_position = state.step(piece, new_step, new_position, reverse);
                                            addChild(position, new_position);
                                            reverse = true;
                                        }
                                        catch (Exception e) {break;}
                                    }
                                }
                                reverse = true;
                            }
                            new_position = intermediate_position;
                            addChild(position, new_position);
                        }
                        catch (Exception e) {break;} // moved off board 
                    }
                }
            }
        }
    }

    /**
     * find all legal moves for a particular piece on the board
     * @param position
     * @param state
     * @return
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

        else 
        {
            for (Direction[] step: steps)
            {
                Position new_position = position;
                boolean reverse = false;
                boolean tried_switch = false;
                Position intermediate_position;
                for (int i = 0; i < num_steps; i++)
                {
                    try
                    {
                        intermediate_position = state.step(piece, step, new_position, reverse);
                        if (intermediate_position.getColour() != position.getColour()) 
                        {
                            if (piece.getType() != PieceType.ROOK && !tried_switch && step.length > 1)
                            {
                                tried_switch = true;
                                Direction[] new_step = new Direction[] {step[1], step[0]};
                                for (int j = i; j < num_steps; j++)
                                {
                                    try
                                    {
                                        new_position = state.step(piece, new_step, new_position, reverse);
                                        ArrayList<Position> new_move = new ArrayList<Position>(); 
                                        new_move.add(position); new_move.add(new_position);
                                        if (!list_of_moves.contains(new_move) && state.isLegalMove(position, new_position))
                                        {
                                            list_of_moves.add(new_move);
                                            moves.add(new Position[] {position, new_position});
                                            reverse = true;
                                        }
                                        
                                    }
                                    catch (Exception e) {break;}
                                }
                            }
                            reverse = true;
                        }
                        new_position = intermediate_position;
                        ArrayList<Position> new_move = new ArrayList<Position>(); 
                        new_move.add(position); new_move.add(new_position);

                        if (!list_of_moves.contains(new_move) && state.isLegalMove(position, new_position))
                        {
                            list_of_moves.add(new_move);
                            moves.add(new Position[] {position, new_position});
                        }
                        else break;
                    }
                        
                    catch (Exception e) {break;} // moved off board 
                }
            }
        }
        
        return moves;
    }
}
