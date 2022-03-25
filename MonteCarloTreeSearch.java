import java.util.*;
import java.lang.Math;

public class MonteCarloTreeSearch extends Main {
  State curr;
  static int ble;

  public MonteCarloTreeSearch(State s){
    curr = s;
    ble = 0;
  }

  // 12500
  public int next(){
    return findBestNextMove(new Node(curr, true),12500);
  }

  static class Node {
    State state;
    Node parent;
    List<Node> children;
    int move;
    int numWins;
    int numPlayed;
    boolean opponentsMove;
    
    public Node(State s, boolean o) {
    	state = s;
    	children = new ArrayList<Node>();
    	numWins = 0;
    	numPlayed = 0;
      opponentsMove = o;
    }
    
    public Node(State s, Node p) {
    	state = s;
    	parent = p;
    	children = new ArrayList<Node>();
    	numWins = 0;
    	numPlayed = 0;
      if(p.opponentsMove){
        opponentsMove = false;
      } else {
        opponentsMove = true;
      }
    }
    
    public Node(State s, Node p, int m) {
    	state = s;
    	parent = p;
    	children = new ArrayList<Node>();
    	move = m;
    	numWins = 0;
    	numPlayed = 0;
      if(p.opponentsMove){
        opponentsMove = false;
      } else {
        opponentsMove = true;
      }
    }
    
    public Node(State s, Node p, List<Node> c, int wins, int plays) {
    	state = s;
    	parent = p;
    	children = c;
    	numWins = wins;
    	numPlayed = plays;
      if(p.opponentsMove){
        opponentsMove = false;
      } else {
        opponentsMove = true;
      }
    }
  }

	/**
	 * Finds the UCT value of a node. (The UCT value is a number that measures how
	 * "good" it is to explore the node)
	 * 
	 * @param node
	 * @return The UCT value of a node.
	 */
	public static double getUCTValue(Node node) {

		double UCTValue = 0;

		int numWins = node.numWins;
		int numPlayed = node.numPlayed;

		if (numPlayed == 0) {
			UCTValue = Integer.MAX_VALUE;
		} else {
			UCTValue = numWins / numPlayed + Math.sqrt(2) * Math.sqrt(Math.log(node.parent.numPlayed) / numPlayed);
		}

		return UCTValue;
	}

	/**
	 * Finds the node with the highest UCT value. Starting at some node: Go through
	 * children If child has no children, add that child to list. If child has
	 * children, find the best node under that child.
	 * 
	 * 
	 * @param node
	 * @return The child node with the highest UCT value.
	 */
	public static Node findBestNode(Node node) {
		if (node.children.size() == 0) {
			return node;
		}

		List<Node> children = node.children;
		List<Node> bestNodes = new ArrayList<Node>();

		for (int i = 0; i < children.size(); i++) {
			Node currentChild = children.get(i);
			// If child has no children
			if (currentChild.children.size() == 0) {
        // Add that child to list of possible best nodes.
				bestNodes.add(i, currentChild);
			}
			// If child has children
			else {
        // Find the best child under that child.
				bestNodes.add(i, findBestNode(currentChild));
			}
		}

		Node bestNode = bestNodes.get(0);
		double max = getUCTValue(bestNode);

		for (int i = 0; i < children.size(); i++) {
			Node currentNode = bestNodes.get(i);
			if (getUCTValue(currentNode) > max) {
				bestNode = currentNode;
				max = getUCTValue(currentNode);
			}
		}

		return bestNode;
	}

	/**
	 * Expand if the best node has been run x times already.
	 * 
	 * @param node
	 */
	public static void expand(Node node) {
		int[] possibleMoves = node.state.getMoves();
	//	node.state.printBoard();
	//	System.out.println("Expanding. " + possibleMoves.length + " moves added.");
		for (int i = 0; i < possibleMoves.length; i++) {
			if(possibleMoves[i] == -1) {
			} else {
			//	System.out.println(i + " " + possibleMoves[i]);
				State newState = node.state.next(possibleMoves[i]);
				Node newNode = new Node(newState, node, possibleMoves[i]);
				node.children.add(newNode);
			}
		}
	}
	
	/**
	 * Simulate playing through a game from a certain state.
	 * @param node
	 */
	public static int simulate(Node node) {
		State state = node.state;
		while(state.done() == 0) {
			int nextMove = state.random();
			state = state.next(nextMove);
		}
		return state.done();
	}
	
	/**
	 * Update stats through the game tree.
	 * @param node
	 * @param int
	 */
	public static void update(Node node, int winner) {
		Node currentNode = node;
		currentNode.numPlayed++;
		if (winner == 1 && currentNode.opponentsMove == false) {
			currentNode.numWins++;
		}
		if (winner == 2 && currentNode.opponentsMove == true) {
		  currentNode.numWins++;
		}
		if (currentNode.parent != null) {
			update(currentNode.parent, winner);
		}
	}
	
	public static int findBestNextMove(Node node, int iterations) {
		for(int i = 0; i < iterations; i++) {
	    Node bestNode = findBestNode(node);
	    if(bestNode.numPlayed > 5) {
	    	expand(bestNode);
        if(bestNode.state.done() == 0) {
          int rand = new Random().nextInt(bestNode.children.size());
	    		bestNode =  bestNode.children.get(rand);
        }
	    }
	    int winner = simulate(bestNode);
	    update(bestNode, winner);  	
	  }
		
		double bestWinRate = -1;
		int bestMove = -1;
		for(int i = 0; i < node.children.size(); i++) {
			Node child = node.children.get(i);
			double childWinRate = (double) child.numWins/child.numPlayed;
			if (childWinRate > bestWinRate) {
			  bestWinRate = childWinRate;
				bestMove = child.move;
			}
		}
		return bestMove;
	}

}
