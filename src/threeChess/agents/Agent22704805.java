package threeChess.agents;

import threeChess.*;


public class Agent22704805 extends Agent {

    public Position[] playMove(Board board) {
        int iterations = 0; // change to time (limit of 10 seconds? etc)
        int max_iterations = 40;
        

        // code to jump to child's child's node and set as root (need to check if that root has been visited before though)

        Node root = new Node(board);
        root.populateChildren();
        System.out.println(root.children.size());

        while (iterations < max_iterations)
        {
            iterations++;
            
            

        }

        return null;
    }

    /**
     * select child to explore using Upper Confidence Bound algorithm (selection policy)
     */
    public Node selectChild(Node parent)
    {






        return null;
    }

    public String toString() {
        return "22704805";
    }

    public void finalBoard(Board finalBoard) {

    }
    
}
