import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;

public class JonMikeAI extends CKPlayer {
	private final int CUTOFF_DEPTH = 2;
	private final boolean MAX = true;
	private final boolean MIN = false;

	public JonMikeAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "JonMikeAI";
	}

	// Heuristic function
	public int heuristic(BoardModel state) {
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

	}


	public int minMax(BoardModel state, int depth, boolean max) {
		// Return the heuristic value of the current state if we have reached our depth limit
		if (depth == CUTOFF_DEPTH) {
			return heuristic(state);
		}

		// Figure out how many available moves we could have (Width w/ gravity, Width*Height w/o gravity)
		int maxMoves = (state.gravityEnabled())	? state.getWidth() : state.getWidth() * state.getHeight();

		Point[] availableMoves = new Point[maxMoves]; 

		// Loop through each column and row selecting available moves.  If gravity is on,
		// only select the lowest y-index move in each column.
		int movesSoFar = 0;
		for (int i = 0; i < state.getWidth(); i++) {
			for (int j = 0; j < state.getHeight(); j++) {
				if (state.getSpace(i, j) == 0) {
					availableMoves[movesSoFar++] = new Point(i,j);
					if (state.gravityEnabled()) {
						break;
					}
				}
			}
		}

		int maxVal = Integer.MIN_VALUE;
		int minVal = Integer.MAX_VALUE;
		int moveVal;

		// Loop through each move performing a min/max evaluation on each recursively
		for (int i = 0; i < movesSoFar; i++) {
			// Determine which player is going by assuming it is the opposite of the
			// player that just made a move
			byte player = (byte)(state.getSpace(state.getLastMove()) == 1 ? 2 : 1);

			// Recurse into the minMax function
			// Pass the state we would get by making the move
			// Increment the depth
			// If this level is a max level then the next will be a min and vice versa
			moveVal = minMax(state.placePiece(availableMoves[i], player), depth+1, !max);

			if (moveVal < minVal) {
				minVal = moveVal;
			}

			if (moveVal > maxVal) {
				maxVal = moveVal;
			}
		}

		if (max) {
			return maxVal;
		} else {
			return minVal;
		}
	}

	@Override
	public Point getMove(BoardModel state) {
		// Figure out how many available moves we could have (Width w/ gravity, Width*Height w/o gravity)
		int maxMoves = (state.gravityEnabled()) ? state.getWidth() : state.getWidth() * state.getHeight();

		Point[] availableMoves = new Point[maxMoves]; 

		// Loop through each column and row selecting available moves.  If gravity is on,
		// only select the lowest y-index move in each column.
		int movesSoFar = 0;
		for (int i = 0; i < state.getWidth(); i++) {
			for (int j = 0; j < state.getHeight(); j++) {
				if (state.getSpace(i, j) == 0) {
					availableMoves[movesSoFar++] =  new Point(i,j);
					if (state.gravityEnabled()) {
						break;
					}
				}
			}
		}

		int maxVal = Integer.MIN_VALUE;
		Point maxValMove = null;
		int moveVal;

		// Loop through each move performing a min/max evaluation on each recursively
		for (int i = 0; i < movesSoFar; i++) {
			// Determine which player is going by assuming it is the opposite of the
			// player that just made a move
			byte player;
			if(state.getLastMove() == null) {
				player = 1;
			}
			else {
				player = (byte)(state.getSpace(state.getLastMove()) == 1 ? 2 : 1);
			}
			
			// Recurse into the minMax function
			// Pass the state we would get by making the move
			moveVal = minMax(state.placePiece(availableMoves[i], player), 0, MAX);

			if (moveVal > maxVal) {
				maxVal = moveVal;
				maxValMove = availableMoves[i];
			}
		}
		return maxValMove;
	}

	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}
	

	
	
}
