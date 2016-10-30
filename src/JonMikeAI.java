import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.ArrayList;

public class JonMikeAI extends CKPlayer {

	private final int CUTOFF_DEPTH = 2;
	private byte player;
	
	public JonMikeAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "JonMikeAI";
	}


	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}


	// abSearch(state) from slides
	@Override
	public Point getMove(BoardModel state) {

		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		// Determine which player is going by assuming it is the opposite of the
		// player that just made a move
		if(state.getLastMove() == null) {
			player = 1;
		} else {
			player = (byte)(state.getSpace(state.getLastMove()) == 1 ? 2 : 1);
		}
		int value;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		int depth = 0;
		Point move = null;

		for (int i = 0; i < availableMoves.size(); i++) {
			value = minValue(state.placePiece(availableMoves.get(i), player), depth+1, alpha, beta);
			if(value > alpha) {
				alpha = value;
				move = availableMoves.get(i);
			}
		}
		return move;
	}


	// maxValue(state, al, be) from slides
	private int maxValue(BoardModel state, int depth, int alpha, int beta) {
		// if recurse limit reached, eval position
		// if(terminal(state)) return utility(state);
		if (depth == CUTOFF_DEPTH)
			return heuristic(state);

		// otherwise, find the best child
		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		// v = -infty
		int value; 

		// for each action a:
		//	v = max(v, minValue(apply(state,a))
		for(int i = 0; i < availableMoves.size(); i++) {
			value = minValue(state.placePiece(availableMoves.get(i), player), depth+1, alpha, beta);
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
	private int minValue(BoardModel state, int depth, int alpha, int beta) {
		// If recursion limit reached, eval position
		// if(terminal(state)) return utility(state)
		if (depth == CUTOFF_DEPTH)
			return heuristic(state);

		// otherwise, find the worst child
		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		// v = infty
		int value; 

		// for each action a:
		//	v = max(v, maxValue(apply(state,a))
		for(int i = 0; i < availableMoves.size(); i++) {
			value = maxValue(state.placePiece(availableMoves.get(i), (byte)(player == 1 ? 2 : 1)), depth+1, alpha, beta);
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

	// New heuristic function
	private int heuristic(BoardModel state) {
		int value;
		int total = 0;

		// Iterate over the board
		for(int x = 0; x < state.getWidth(); x++) {
			for(int y = 0; y < state.getHeight(); y++) {	

				value = getVerticle(state, new Point(x,y));
				if(value == Integer.MAX_VALUE || value == Integer.MIN_VALUE)
					return value;
				else
					total += value;

				value =  getHorizontal(state, new Point(x,y));
				if(value == Integer.MAX_VALUE || value == Integer.MIN_VALUE)
					return value;
				else
					total += value;

				value = getDiagonalLeft(state, new Point(x,y));
				if(value == Integer.MAX_VALUE || value == Integer.MIN_VALUE)
					return value;
				else
					total += value;

				value = getDiagonalRight(state, new Point(x,y));
				if(value == Integer.MAX_VALUE || value == Integer.MIN_VALUE)
					return value;
				else
					total += value;

			}
		}
		return total;
	} 

	// Heuristic Helper Function (up)
	private int getVerticle(BoardModel state, Point position) {
		int p1Pieces = 0;
		int p2Pieces = 0;
		int empty = 0;
		if(boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x,position.y + k) == (byte) 1)
					p1Pieces++;	
				else if(state.getSpace(position.x,position.y + k) == (byte) 2)
					p2Pieces++;
				else
					empty++;
			}
		}
		return calculate(state, p1Pieces, p2Pieces, empty);
	}

	// Heuristic Helper Function (right)
	private int getHorizontal(BoardModel state, Point position) {
		int p1Pieces = 0;
		int p2Pieces = 0;
		int empty = 0;
		if(boundCheckRight(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x + k, position.y) == (byte) 1)
					p1Pieces++;	
				else if(state.getSpace(position.x + k, position.y) == (byte) 2)
					p2Pieces++;
				else
					empty++;
			}
		}
		return calculate(state, p1Pieces, p2Pieces, empty);
	}

	// Heuristic Helper Function (left and up)
	private int getDiagonalLeft(BoardModel state, Point position) {
		int p1Pieces = 0;
		int p2Pieces = 0;
		int empty = 0;
		if(boundCheckLeft(state, position) && boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x - k, position.y + k) == (byte) 1)
					p1Pieces++;	
				else if(state.getSpace(position.x - k, position.y + k) == (byte) 2)
					p2Pieces++;
				else
					empty++;
			}
		}
		return calculate(state, p1Pieces, p2Pieces, empty);
	}

	// Heuristic Helper Function (right and up)
	private int getDiagonalRight(BoardModel state, Point position) {
		int p1Pieces = 0;
		int p2Pieces = 0;
		int empty = 0;
		if(boundCheckRight(state, position) && boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x + k,position.y + k) == (byte) 1)
					p1Pieces++;	
				else if(state.getSpace(position.x + k,position.y + k) == (byte) 2)
					p2Pieces++;
				else
					empty++;
			}
		}
		return calculate(state, p1Pieces, p2Pieces, empty);
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

	// Helper function for getDiag/getVert/etc.
	// Calculates the heuristic value difference between players based on which player the AI is
	private int calculate(BoardModel state, int p1Pieces, int p2Pieces, int empty) {
		if(p1Pieces == state.getkLength())
			return this.player == (byte) 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		else if(p2Pieces == state.getkLength())
			return this.player == (byte) 2 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		else if(p1Pieces + empty == state.getkLength())
			return this.player == (byte) 1 ? 1 : -1;
		else if(p2Pieces + empty == state.getkLength())
			return this.player == (byte) 2 ? 1 : -1;
		else 
			return 0;
	}
}
