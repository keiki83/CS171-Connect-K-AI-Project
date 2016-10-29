import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.ArrayList;

public class JonMikeAI extends CKPlayer {

	private final int CUTOFF_DEPTH = 2;

	public JonMikeAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "JonMikeAI";
	}

	
	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}

	
	// mmSearch(state) from slides
	@Override
	public Point getMove(BoardModel state) {

		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		int value;
		int maxValue = Integer.MIN_VALUE;
		int depth = 0;
		Point move = null;

		for (int i = 0; i < availableMoves.size(); i++) {
			value = minValue(state.placePiece(availableMoves.get(i), (byte) 1), depth+1);
			if(value > maxValue) {
				maxValue = value;
				move = availableMoves.get(i);
			}
		}
		return move;
	}

	
	// maxValue(state) from slides
	private int maxValue(BoardModel state, int depth) {
		// if recurse limit reached, eval position
		// if(terminal(state)) return utility(state);
		if (depth == CUTOFF_DEPTH)
			return heuristic(state);

		// otherwise, find the best child
		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		// v = -infty
		int value; 
		int maxValue = Integer.MIN_VALUE;

		// for each action a:
		//	v = max(v, minValue(apply(state,a))
		for(int i = 0; i < availableMoves.size(); i++) {
			value = minValue(state.placePiece(availableMoves.get(i), (byte) 1), depth+1);
			if (value > maxValue)
				maxValue = value;
		}

		// return v
		return 1;
	}

	// minValue(state) from slides
	private int minValue(BoardModel state, int depth) {
		// If recursion limit reached, eval position
		// if(terminal(state)) return utility(state)
		if (depth == CUTOFF_DEPTH)
			return heuristic(state);

		// otherwise, find the worst child
		ArrayList<Point> availableMoves = getAvailableMoves(state); 

		// v = infty
		int value; 
		int minValue = Integer.MAX_VALUE;

		// for each action a:
		//	v = max(v, maxValue(apply(state,a))
		for(int i = 0; i < availableMoves.size(); i++) {
			value = maxValue(state.placePiece(availableMoves.get(i), (byte) 2), depth+1);
			if (value < minValue) 
				minValue = value;
		}

		// return v
		return 1;
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
		int p1Win = 0;				// sum of # of player1 possible win paths
		int p2Win = 0;				// sum of # of player2 possible win paths

		// Iterate over the board
		for(int x = 0; x < state.width; x++) {
			for(int y = 0; y < state.height; y++) {	
				p1Win += getVerticle(state, new Point(x,y), (byte) 1);
				p1Win += getHorizontal(state, new Point(x,y), (byte) 1);
				p1Win += getDiagonalLeft(state, new Point(x,y), (byte) 1);
				p1Win += getDiagonalRight(state, new Point(x,y), (byte) 1);
				p2Win += getVerticle(state, new Point(x,y), (byte) 2);
				p2Win += getHorizontal(state, new Point(x,y), (byte) 2);
				p2Win += getDiagonalLeft(state, new Point(x,y), (byte) 2);
				p2Win += getDiagonalRight(state, new Point(x,y), (byte) 2);

			}
		}

		return p1Win - p2Win;

	} 

	// Heuristic Helper Function (up)
	private int getVerticle(BoardModel state, Point position, Byte player) {
		int pieces = 0;
		int empty = 0;
		if(boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x,position.y + k) == player)
					pieces++;	
				if(state.getSpace(position.x,position.y + k) == 0)
					empty++;
			}
		}
		if(pieces == state.getkLength()) 
			return Integer.MAX_VALUE;
		else if (pieces + empty == state.getkLength())
			return 1;
		else
			return 0;
	}

	// Heuristic Helper Function (right)
	private int getHorizontal(BoardModel state, Point position, Byte player) {
		int pieces = 0;
		int empty = 0;
		if(boundCheckRight(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x + k,position.y) == player)
					pieces++;
				if(state.getSpace(position.x + k,position.y) == 0)
					empty++;
			}
		}
		if(pieces == state.getkLength()) 
			return Integer.MAX_VALUE;
		else if (pieces + empty == state.getkLength())
			return 1;
		else
			return 0;
	}

	// Heuristic Helper Function (left and up)
	private int getDiagonalLeft(BoardModel state, Point position, Byte player) {
		int pieces = 0;
		int empty = 0;
		if(boundCheckLeft(state, position) && boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x - k,position.y + k) == player)
					pieces++;
				if(state.getSpace(position.x - k,position.y + k) == 0)
					empty++;
			}
		}
		if(pieces == state.getkLength()) 
			return Integer.MAX_VALUE;
		else if (pieces + empty == state.getkLength())
			return 1;
		else
			return 0;
	}

	// Heuristic Helper Function (right and up)
	private int getDiagonalRight(BoardModel state, Point position, Byte player) {
		int pieces = 0;
		int empty = 0;
		if(boundCheckRight(state, position) && boundCheckUp(state, position)) {					
			for(int k = 0; k < state.getkLength(); k++) {
				if(state.getSpace(position.x + k,position.y + k) == player)
					pieces++;
				if(state.getSpace(position.x + k,position.y + k) == 0)
					empty++;
			}
		}
		if(pieces == state.getkLength()) 
			return Integer.MAX_VALUE;
		else if (pieces + empty == state.getkLength())
			return 1;
		else
			return 0;
	}

	private boolean boundCheckUp(BoardModel state, Point position) {
		return state.getHeight() >= position.y + state.getkLength();
	}

	private boolean boundCheckLeft(BoardModel state, Point position) {
		return state.getkLength() <= position.x + 1;
	}

	private boolean boundCheckRight(BoardModel state, Point position) {
		return state.getWidth() >= position.x + state.getkLength();
	}

}
