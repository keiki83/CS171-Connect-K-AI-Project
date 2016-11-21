import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.ArrayList;

public class JonMikeAI extends CKPlayer {
	// Used for testing code, set to FALSE before submission
	private final Boolean DEBUG_MINIMAX = false;
	private final Boolean DEBUG_HEURISTIC = false;
	private final Boolean DEBUG_TIMING = false;

	// Variables
	private long begin;
	private long deadline = 5000;
	private long buffer = 200;
	
	// Constructor
	public JonMikeAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "JonMikeAI";
	}


	@Override
	// getMove with a specified deadline
	public Point getMove(BoardModel state, int deadline) {

		begin = System.currentTimeMillis();
		this.deadline = deadline;
		
		if(DEBUG_TIMING) {
			System.out.format("DEBUG: getMove with deadline - deadline: %d, time: %d, buffer: %d%n", deadline, this.deadline, System.currentTimeMillis() - begin, buffer);
		}
		
		Point move = getMove(state);
		
		if(DEBUG_TIMING) {
			System.out.format("DEBUG: getMove with deadline returning move - time: %d%n", System.currentTimeMillis() - begin);
		}
		
		return move;
	}


	// abSearch(state) from slides
	@Override
	// getMove using default deadline
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
			if ((System.currentTimeMillis() - begin) > (deadline - buffer)) {
				break;
			}

			// Start with the best known option from the last round of IDS
			value = minValue(state.placePiece(availableMoves.get(move_index), player), depth+1, alpha, beta, cuttoff);
			if(value > alpha) {
				alpha = value;
			}

			for (int i = 0; i < availableMoves.size(); i++) {

				if ((System.currentTimeMillis() - begin) > (deadline - buffer)) {
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
		if (depth >= cuttoff || (System.currentTimeMillis() - begin) > (deadline - buffer)) {
			int heuristicValue = heuristic(state);
			if(DEBUG_HEURISTIC) {
				System.out.println(String.format("DEBUG: Heuristic value: %d\n\n", heuristicValue));
			}
			if(DEBUG_MINIMAX) {
				System.out.println(String.format("DEBUG: maxValue() - depth: %s, alpha: %d, beta: %d, cuttoff: %d, heuristicValue: %d", depth, alpha, beta, cuttoff, heuristicValue));
				System.out.println(state.toString());
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
		if (depth >= cuttoff || (System.currentTimeMillis() - begin) > (deadline - buffer)) {
			int heuristicValue = heuristic(state);
			if(DEBUG_HEURISTIC) {
				System.out.println(String.format("DEBUG: Heuristic value: %d\n\n", heuristicValue));
			}
			if(DEBUG_MINIMAX) {
				System.out.println(String.format("DEBUG: minValue() - depth: %s, alpha: %d, beta: %d, cuttoff: %d, heuristicValue: %d", depth, alpha, beta, cuttoff, heuristicValue));
				System.out.println(state.toString());
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

		if(DEBUG_HEURISTIC) {
			printBoard(state);;
		}
		
		// Iterate over the board
		for(int x = 0; x < state.getWidth(); x++) {
			for(int y = 0; y < state.getHeight(); y++) {

				// Player 1
				Player = 1;

				// Get player 1 up value
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

				// Get player 2 up value
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
				if(state.getSpace(position.x, position.y + k) == enemyPlayer) {
					value = 0;
					break;
				}
				if(state.getSpace(position.x, position.y + k) == player) {
					pieces++;
					value += (pieces*pieces);
				}
			}
		}
				
		if(pieces == state.getkLength())
			value = Integer.MAX_VALUE;
		
		if(DEBUG_HEURISTIC) {
			System.out.println(String.format("DEBUG: [%d,%d] verticle   - board value: %d, player: %d, enemyPlayer: %d, pieces: %d, value: %d", position.x, position.y, state.getSpace(position.x, position.y), player, enemyPlayer, pieces, value));
			System.out.flush();
		}
		
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
				if(state.getSpace(position.x + k, position.y) == enemyPlayer) {
					value = 0;
					break;
				}
				if(state.getSpace(position.x + k, position.y) == player) {
					pieces++;
					value += (pieces*pieces);
				}
			}
		}

		if(pieces == state.getkLength())
			value = Integer.MAX_VALUE;
		
		if(DEBUG_HEURISTIC) {
			System.out.println(String.format("DEBUG: [%d,%d] horizontal - board value: %d, player: %d, enemyPlayer: %d, pieces: %d, value: %d", position.x, position.y, state.getSpace(position.x, position.y), player, enemyPlayer, pieces, value));
			System.out.flush();
		}
		
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
				if(state.getSpace(position.x - k, position.y + k) == enemyPlayer) {
					value = 0;
					break;
				}
				if(state.getSpace(position.x - k, position.y + k) == player) {
					pieces++;
					value += (pieces*pieces);
				}
			}
		}

		if(pieces == state.getkLength())
			value = Integer.MAX_VALUE;
		
		if(DEBUG_HEURISTIC) {
			System.out.println(String.format("DEBUG: [%d,%d] diagLeft   - board value: %d, player: %d, enemyPlayer: %d, pieces: %d, value: %d", position.x, position.y, state.getSpace(position.x, position.y), player, enemyPlayer, pieces, value));
			System.out.flush();
		}
		
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
				if(state.getSpace(position.x + k, position.y + k) == enemyPlayer) {
					value = 0;
					break;
				}
				if(state.getSpace(position.x + k, position.y + k) == player) {
					pieces++;
					value += (pieces*pieces);
				}
			}
		}

		if(pieces == state.getkLength())
			value = Integer.MAX_VALUE;
		
		if(DEBUG_HEURISTIC) {
			System.out.println(String.format("DEBUG: [%d,%d] diagRight  - board value: %d, player: %d, enemyPlayer: %d, pieces: %d, value: %d", position.x, position.y, state.getSpace(position.x, position.y), player, enemyPlayer, pieces, value));
			System.out.flush();
		}
		
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
	
	// debug function - print game board
	private void printBoard(BoardModel state) {
		for(int y = state.getHeight() - 1; y >= 0 ; y--) {
			System.out.print("DEBUG: ");
			for(int x = 0; x < state.getWidth(); x++) {
				System.out.print(state.getSpace(x, y));
			}
			System.out.print("\n");
		}
	}
}