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

	// Heuristic function
	private int heuristic(BoardModel state) {
		int p1Win = 0;				// sum of # of player1 possible win paths
		int p2Win = 0;				// sum of # of player2 possible win paths

		// Iterate over the board
		for(int i = 0; i < state.width; i++) {
			for(int j = 0; j < state.height; j++) {	

				// up [i][j+k]
				if(state.height >= j + state.kLength) {
					int p1Pieces = 0;
					int p2Pieces = 0;
					for(int k = 0; k < state.kLength; k++) {
						if(state.pieces[i][j+k] == 1) p1Pieces++;
						if(state.pieces[i][j+k] == 2) p2Pieces++;
					}
					if(p1Pieces == 0) p2Win++;
					if(p2Pieces == 0) p1Win++;
				}

				// right & up [i+k][j+k]
				if((state.width >= i + state.kLength) && (state.height >= j + state.kLength)) {					
					int p1Pieces = 0;
					int p2Pieces = 0;
					for(int k = 0; k < state.kLength; k++) {	
						if(state.pieces[i+k][j+k] == 1)	p1Pieces++;
						if(state.pieces[i+k][j+k] == 2)	p2Pieces++;
					}
					if(p1Pieces == 0) p2Win++;
					if(p2Pieces == 0) p1Win++;
				}

				// right [i+k][j]
				if(state.width >= i + state.kLength) {
					int p1Pieces = 0;
					int p2Pieces = 0;
					for(int k = 0; k < state.kLength; k++) {	
						if(state.pieces[i+k][j] == 1) p1Pieces++;
						if(state.pieces[i+k][j] == 2) p2Pieces++;
					}
					if(p1Pieces == 0) p2Win++;
					if(p2Pieces == 0) p1Win++;
				}

				// right & down [i+k][j-k]
				if((state.width >= i + state.kLength) && (state.kLength <= j + 1)) {
					int p1Pieces = 0;
					int p2Pieces = 0;
					for(int k = 0; k < state.kLength; k++) {	
						if(state.pieces[i+k][j-k] == 1)	p1Pieces++;
						if(state.pieces[i+k][j-k] == 2)	p2Pieces++;
					}
					if(p1Pieces == 0) p2Win++;
					if(p2Pieces == 0) p1Win++;
				}

				// down [i][j-k]
				if(state.kLength <= j + 1) {					
					int p1Pieces = 0;
					int p2Pieces = 0;
					for(int k = 0; k < state.kLength; k++) {	
						if(state.pieces[i][j-k] == 1) p1Pieces++;
						if(state.pieces[i][j-k] == 2) p2Pieces++;
					}
					if(p1Pieces == 0) p2Win++;
					if(p2Pieces == 0) p1Win++;
				}

				// left & down [i-k][j-k]
				if((state.kLength <= i + 1) && (state.kLength <= j + 1)) {
					int p1Pieces = 0;
					int p2Pieces = 0;
					for(int k = 0; k < state.kLength; k++) {	
						if(state.pieces[i-k][j-k] == 1) p1Pieces++;
						if(state.pieces[i-k][j-k] == 2) p2Pieces++;
					}
					if(p1Pieces == 0) p2Win++;
					if(p2Pieces == 0) p1Win++;
				}

				// left [i-k][j]
				if(state.kLength <= i + 1) {					
					int p1Pieces = 0;
					int p2Pieces = 0;
					for(int k = 0; k < state.kLength; k++) {	
						if(state.pieces[i-k][j] == 1) p1Pieces++;
						if(state.pieces[i-k][j] == 2) p2Pieces++;
					}
					if(p1Pieces == 0) p2Win++;
					if(p2Pieces == 0) p1Win++;
				}

				// left & up [i-k][j+k]
				if((state.kLength <= i + 1) && (state.height >= j + state.kLength)) {					
					int p1Pieces = 0;
					int p2Pieces = 0;
					for(int k = 0; k < state.kLength; k++) {	
						if(state.pieces[i-k][j+k] == 1) p1Pieces++;
						if(state.pieces[i-k][j+k] == 2)	p2Pieces++;
					}
					if(p1Pieces == 0) p2Win++;
					if(p2Pieces == 0) p1Win++;
				}


			}	// end verticle board iteration
		}	 	// end horizonal board iteration

		Point lastMove = state.getLastMove();
		return (state.pieces[lastMove.x][lastMove.y] == 1) ? (p2Win - p1Win) : (p1Win - p2Win);

	} // end Heuristic function

}
