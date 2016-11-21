import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.ArrayList;

public class JonMikeAI extends CKPlayer {
	// Used for testing code, set to FALSE before submission
	private final Boolean DEBUG = true;
	
	
	private long begin;
	public JonMikeAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "JonMikeAI";
	}


	@Override
	public Point getMove(BoardModel state, int deadline) {

		begin = System.currentTimeMillis();
		return getMove(state);
	}


	// abSearch(state) from slides
	@Override
	public Point getMove(BoardModel state) {
		begin = System.currentTimeMillis();
		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		int value;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		int depth = 0;
		int cuttoff = 1;
		Point move = availableMoves.get(0);
		int move_index = 0;

		while (true) {
			if ((System.currentTimeMillis() - begin) > 4000){
				break;
			}

			// Start with the best known option from the last round of IDS
			value = minValue(state.placePiece(availableMoves.get(move_index), player), depth+1, alpha, beta, cuttoff);
			if(value > alpha) {
				alpha = value;
			}

			for (int i = 0; i < availableMoves.size(); i++) {

				if ((System.currentTimeMillis() - begin) > 4000){
					break;
				}
				if (i == move_index) {
					// No point in doing work twice
					continue;
				}

				value = minValue(state.placePiece(availableMoves.get(i), player), depth+1, alpha, beta, cuttoff);
				if(value > alpha) {
					alpha = value;
					move = availableMoves.get(i);
					move_index = i;
				}
			}
			cuttoff++;
		}
		return move;
	}


	// maxValue(state, al, be) from slides
	private int maxValue(BoardModel state, int depth, int alpha, int beta, int cuttoff) {
		// if recurse limit reached, eval position
		// if(terminal(state)) return utility(state);
		if (depth >= cuttoff || (System.currentTimeMillis() - begin) > 4000){
			int heuristicValue = heuristic(state);
			if(DEBUG) {
				System.out.println(String.format("DEBUG: maxValue() - depth: %s, alpha: %d, beta: %d, cuttoff: %d, heuristicValue: %d", depth, alpha, beta, cuttoff, heuristicValue));
			}
			return heuristicValue;
		}

		// otherwise, find the best child
		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		// v = -infty
		int value; 

		// for each action a:
		//	v = max(v, minValue(apply(state,a))
		for(int i = 0; i < availableMoves.size(); i++) {
			value = minValue(state.placePiece(availableMoves.get(i), player), depth+1, alpha, beta, cuttoff);
			if (value > alpha) {
				alpha = value;
			}
			if (alpha >= beta) {
				return Integer.MAX_VALUE;
			}
		}

		// return v
		return alpha;
	}

	// minValue(state, al, be) from slides
	private int minValue(BoardModel state, int depth, int alpha, int beta, int cuttoff) {
		// If recursion limit reached, eval position
		// if(terminal(state)) return utility(state)
		if (depth >= cuttoff || (System.currentTimeMillis() - begin) > 4500) {
			int heuristicValue = heuristic(state);
			if(DEBUG) {
				System.out.println(String.format("DEBUG: minValue() - depth: %s, alpha: %d, beta: %d, cuttoff: %d, heuristicValue: %d", depth, alpha, beta, cuttoff, heuristicValue));
			}
			return heuristicValue;
		}

		// otherwise, find the worst child
		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		// v = infty
		int value; 

		// for each action a:
		//	v = max(v, maxValue(apply(state,a))
		for(int i = 0; i < availableMoves.size(); i++) {
			value = maxValue(state.placePiece(availableMoves.get(i), (byte)(player == 1 ? 2 : 1)), depth+1, alpha, beta, cuttoff);
			if (value < beta) {
				beta = value;
			}
			if (alpha >= beta) {
				return Integer.MIN_VALUE;
			}
		}

		// return v
		return beta;
	}


	// Get available moves - Loop through each column and row selecting available moves.
	// If gravity is on, only select the lowest y-index move in each column.
	private ArrayList<Point> getAvailableMoves(BoardModel state) {	
		ArrayList<Point> availableMoves = new ArrayList<Point>();
		for (int i = 0; i < state.getWidth(); i++) {
			for (int j = 0; j < state.getHeight(); j++) {
				if (state.getSpace(i, j) == 0) {
					availableMoves.add(new Point(i,j));
					if (state.gravityEnabled()) {
						break;
					}
				}
			}
		}
		return availableMoves;
	}

	// Heuristic Function v3.0
	private int heuristic(BoardModel state) {
		int p1 = 0;
		int p2 = 0;
		int Player;
		int value;

		// Iterate over the board
		for(int x = 0; x < state.getWidth(); x++) {
			for(int y = 0; y < state.getHeight(); y++) {
				// Player 1
				Player = 1;
				// Get player 1 top value
				value = getVerticle(state, new Point(x,y), Player);
				if(value == Integer.MAX_VALUE) {
					return calculate(Integer.MAX_VALUE, p2);
				}
				else
					p1 += value;

				// Get player 1 right value
				value = getHorizontal(state, new Point(x,y), Player);
				if(value == Integer.MAX_VALUE) {
					return calculate(Integer.MAX_VALUE, p2);
				}
				else
					p1 += value;

				// Get player 1 diagonal left value
				value = getDiagonalLeft(state, new Point(x,y), Player);
				if(value == Integer.MAX_VALUE) {
					return calculate(Integer.MAX_VALUE, p2);
				}
				else
					p1 += value;

				// Get player 1 diagonal right value
				value = getDiagonalRight(state,new Point(x,y), Player);
				if(value == Integer.MAX_VALUE) {
					return calculate(Integer.MAX_VALUE, p2);
				}
				else
					p1 += value;

				// Player 2
				Player = 2;
				// Get player 2 top value
				value = getVerticle(state, new Point(x,y), Player);
				if(value == Integer.MAX_VALUE) {
					return calculate(p1, Integer.MAX_VALUE);
				}
				else
					p2 += value;

				// Get player 2 right value
				value = getHorizontal(state, new Point(x,y), Player);
				if(value == Integer.MAX_VALUE) {
					return calculate(p1, Integer.MAX_VALUE);
				}
				else
					p2 += value;

				// Get player 2 diagonal left value
				value = getDiagonalLeft(state, new Point(x,y), Player);
				if(value == Integer.MAX_VALUE) {
					return calculate(p1, Integer.MAX_VALUE);
				}
				else
					p2 += value;

				// Get player 2 diagonal right value
				value = getDiagonalRight(state,new Point(x,y), Player);
				if(value == Integer.MAX_VALUE) {
					return calculate(p1, Integer.MAX_VALUE);
				}
				else
					p2 += value;
			}		
		}

		return calculate(p1, p2);
	}

	// Heuristic helper function - calculates heuristic value depending on which player AI is
	private int calculate(int p1, int p2) {
		if(p1 == Integer.MAX_VALUE)
			if(this.player == 1)
				return Integer.MAX_VALUE;
			else
				return Integer.MIN_VALUE;
		if(p2 == Integer.MAX_VALUE)
			if(this.player == 2)
				return Integer.MAX_VALUE;
			else
				return Integer.MIN_VALUE;
		if(this.player == 1)
			return p1-p2;
		else 
			return p2-p1;
	}

	// Heuristic Helper Function (up)
	private int getVerticle(BoardModel state, Point position, int player) {
		// enemy player may be our actual enemy player, or the AI. enemyPlayer refers to the other player of 'player'
		int enemyPlayer = player == 1 ? 2 : 1;
		int value = 0;
		int pieces = 0;

		if(boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x, position.y + k) == enemyPlayer)
					return 0;
				pieces++;
				value += pieces*pieces;
			}
		}

		if(pieces == state.getkLength())
			return Integer.MAX_VALUE;
		return value;
	}

	// Heuristic Helper Function (right)
	private int getHorizontal(BoardModel state, Point position, int player) {
		// enemy player may be our actual enemy player, or the AI. enemyPlayer refers to the other player of 'player'
		int enemyPlayer = player == 1 ? 2 : 1;
		int value = 0;
		int pieces = 0;

		if(boundCheckRight(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x + k, position.y) == enemyPlayer)
					return 0;
				pieces++;
				value += pieces*pieces;
			}
		}

		if(pieces == state.getkLength())
			return Integer.MAX_VALUE;
		return value;
	}

	// Heuristic Helper Function (left and up)
	private int getDiagonalLeft(BoardModel state, Point position, int player) {
		// enemy player may be our actual enemy player, or the AI. enemyPlayer refers to the other player of 'player'
		int enemyPlayer = player == 1 ? 2 : 1;
		int value = 0;
		int pieces = 0;

		if(boundCheckLeft(state, position) && boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x - k, position.y + k) == enemyPlayer)
					return 0;
				pieces++;
				value += pieces*pieces;
			}
		}

		if(pieces == state.getkLength())
			return Integer.MAX_VALUE;
		return value;
	}

	// Heuristic Helper Function (right and up)
	private int getDiagonalRight(BoardModel state, Point position, int player) {
		// enemy player may be our actual enemy player, or the AI. enemyPlayer refers to the other player of 'player'
		int enemyPlayer = player == 1 ? 2 : 1;
		int value = 0;
		int pieces = 0;

		if(boundCheckRight(state, position) && boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x + k, position.y + k) == enemyPlayer)
					return 0;
				pieces++;
				value += pieces*pieces;
			}
		}

		if(pieces == state.getkLength())
			return Integer.MAX_VALUE;
		return value;
	}

	// Helper function for getDiag/getVert/etc.
	// Evaluates if current position is within the bounds of the game
	private boolean boundCheckUp(BoardModel state, Point position) {
		return state.getHeight() >= position.y + state.getkLength();
	}

	// Helper function for getDiag/getVert/etc.
	// Evaluates if current position is within the bounds of the game
	private boolean boundCheckLeft(BoardModel state, Point position) {
		return state.getkLength() <= position.x + 1;
	}

	// Helper function for getDiag/getVert/etc.
	// Evaluates if current position is within the bounds of the game
	private boolean boundCheckRight(BoardModel state, Point position) {
		return state.getWidth() >= position.x + state.getkLength();
	}
}