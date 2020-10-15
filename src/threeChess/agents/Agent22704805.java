package threeChess.agents;

import threeChess.*;


public class Agent22704805 extends Agent {

    public Position[] playMove(Board board) {
        int iterations = 0; // change to time (limit of 10 seconds? etc)
        int max_iterations = 40;
        

        // code to jump to child's child's node and set as root (need to check if that root has been visited before though)

        Node root = new Node(board, null);
        root.populateChildren();
        //System.out.println(root.children.size());

        while (iterations < max_iterations)
        {
            iterations++;
            Node current_node = root;
            boolean have_not_found_leaf = true;
            double temperature = Math.sqrt(2);

            while (have_not_found_leaf)
            {
                int num_children = current_node.children.size();
                double max = -1;
                int best_child = -1;
                for (int i = 0; i < num_children; i++)
                {
                    Node child = current_node.children.get(i);
                    if (child.num_visits == 0) 
                    {
                        have_not_found_leaf = false;
                        current_node = child;
                        break;
                    }

                    double exploration = Math.sqrt(Math.log(current_node.num_visits)/child.num_visits);
                    double ucb1 = ((double) child.num_wins)/((double) child.num_visits) + temperature*exploration; 
                    if (ucb1 > max)
                    {
                        max = ucb1;
                        best_child = i;
                    }

                }

                if (have_not_found_leaf)
                {
                    current_node = current_node.children.get(best_child);
                    if (!current_node.has_populated_children) current_node.populateChildren();
                }
            }

            
            
            

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

    public void simulateGame(Node start)
    {
        
    }

    public String toString() {
        return "22704805";
    }

    public void finalBoard(Board finalBoard) {

    }
    
}
