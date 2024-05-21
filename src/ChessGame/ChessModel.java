package ChessGame;

/*import java.util.HashSet;
import java.util.Set;

public class ChessModel {
	private Set<ChessPiece> piecesBox = new HashSet<ChessPiece>();
	private Player playerInTurn = Player.WHITE;
	
	void reset() {
		piecesBox.removeAll(piecesBox);
		
		for (int i = 0; i < 2; i++) {
			piecesBox.add(new ChessPiece(0 + i * 7, 7, Player.BLACK, Rank.ROOK, ChessConstants.bRook));
			piecesBox.add(new ChessPiece(0 + i * 7, 0, Player.WHITE, Rank.ROOK, ChessConstants.wRook));

			piecesBox.add(new ChessPiece(1 + i * 5, 7, Player.BLACK, Rank.KNIGHT, ChessConstants.bKnight));
			piecesBox.add(new ChessPiece(1 + i * 5, 0, Player.WHITE, Rank.KNIGHT, ChessConstants.wKnight));

			piecesBox.add(new ChessPiece(2 + i * 3, 7, Player.BLACK, Rank.BISHOP, ChessConstants.bBishop));
			piecesBox.add(new ChessPiece(2 + i * 3, 0, Player.WHITE, Rank.BISHOP, ChessConstants.wBishop));
		}
		
		for (int i = 0; i < 8; i++) {
			piecesBox.add(new ChessPiece(i, 6, Player.BLACK, Rank.PAWN, ChessConstants.bPawn));
			piecesBox.add(new ChessPiece(i, 1, Player.WHITE, Rank.PAWN, ChessConstants.wPawn));
		}
		
		piecesBox.add(new ChessPiece(3, 7, Player.BLACK, Rank.QUEEN, ChessConstants.bQueen));
		piecesBox.add(new ChessPiece(3, 0, Player.WHITE, Rank.QUEEN, ChessConstants.wQueen));
		piecesBox.add(new ChessPiece(4, 7, Player.BLACK, Rank.KING, ChessConstants.bKing));
		piecesBox.add(new ChessPiece(4, 0, Player.WHITE, Rank.KING, ChessConstants.wKing));
		
		playerInTurn = Player.WHITE;
	}
	

	
	void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
		ChessPiece movingPiece = pieceAt(fromCol, fromRow);
		if (movingPiece == null || fromCol == toCol && fromRow == toRow) {
			return;
		}
		
		ChessPiece target = pieceAt(toCol, toRow);
		if (target != null) {
			if (target.getPlayer() == movingPiece.getPlayer()) {
				return;
			} else {
				piecesBox.remove(target);
			}
		}
		switch (movingPiece.getRank()) {
        case KING:
            if (!isValidKingMove(fromCol, fromRow, toCol, toRow)) {
                return;
            }
            break;
        case QUEEN:
            if (!isValidQueenMove(fromCol, fromRow, toCol, toRow)) {
                return;
            }
            break;
        case BISHOP:
            if (!isValidBishopMove(fromCol, fromRow, toCol, toRow)) {
                return;
            }
            break;
        case KNIGHT:
            if (!isValidKnightMove(fromCol, fromRow, toCol, toRow)) {
                return;
            }
            break;
        case ROOK:
            if (!isValidRookMove(fromCol, fromRow, toCol, toRow)) {
                return;
            }
            break;
        case PAWN:
            if (!isValidPawnMove(fromCol, fromRow, toCol, toRow, movingPiece.getPlayer())) {
                return;
            }
            // Check for pawn promotion
            handlePawnPromotion(fromCol, fromRow, toCol, toRow, movingPiece.getPlayer());
            break;
    }
		
		piecesBox.remove(movingPiece);
		piecesBox.add(new ChessPiece(toCol, toRow, movingPiece.getPlayer(), movingPiece.getRank(), movingPiece.getImgName()));
		playerInTurn = playerInTurn == Player.WHITE ? Player.BLACK : Player.WHITE;
	}
	
	ChessPiece pieceAt(int col, int row) {
		for (ChessPiece chessPiece : piecesBox) {
			if (chessPiece.getCol() == col && chessPiece.getRow() == row) {
				return chessPiece;
			}
		}
		return null;
	}
	boolean isValidPawnMove(int fromCol, int fromRow, int toCol, int toRow, Player player) {
	    int direction = (player == Player.WHITE) ? 1 : -1;
	    ChessPiece target = pieceAt(toCol, toRow);

	    // Pawn can move forward one square
	    if (toCol == fromCol && toRow == fromRow + direction && target == null) {
	        return true;
	    }

	    // Pawn can move forward two squares on its first move
	    if (toCol == fromCol && toRow == fromRow + 2 * direction && fromRow == (player == Player.WHITE ? 1 : 6) && target == null) {
	        // Check if the squares in between are empty
	        return pieceAt(fromCol, fromRow + direction) == null;
	    }

	    // Pawn can capture diagonally
	    if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction && target != null && target.getPlayer() != player) {
	        return true;
	    }

	    return false;
	}

	boolean isValidRookMove(int fromCol, int fromRow, int toCol, int toRow) {
	    // Rook can move horizontally or vertically
	    return fromCol == toCol || fromRow == toRow;
	}

	boolean isValidKnightMove(int fromCol, int fromRow, int toCol, int toRow) {
	    // Knight moves in an L-shape: two squares in one direction and then one square perpendicular
	    int dx = Math.abs(toCol - fromCol);
	    int dy = Math.abs(toRow - fromRow);
	    return (dx == 1 && dy == 2) || (dx == 2 && dy == 1);
	}

	boolean isValidBishopMove(int fromCol, int fromRow, int toCol, int toRow) {
	    // Bishop can move diagonally
	    return Math.abs(toCol - fromCol) == Math.abs(toRow - fromRow);
	}

	boolean isValidQueenMove(int fromCol, int fromRow, int toCol, int toRow) {
	    // Queen can move horizontally, vertically, or diagonally
	    return isValidRookMove(fromCol, fromRow, toCol, toRow) || isValidBishopMove(fromCol, fromRow, toCol, toRow);
	}

	boolean isValidKingMove(int fromCol, int fromRow, int toCol, int toRow) {
	    // King can move one square in any direction
	    int dx = Math.abs(toCol - fromCol);
	    int dy = Math.abs(toRow - fromRow);
	    return dx <= 1 && dy <= 1;
	}

	void handlePawnPromotion(int fromCol, int fromRow, int toCol, int toRow, Player player) {
	    // Check if the pawn has reached the opposite end of the board
	    if ((player == Player.WHITE && toRow == 7) || (player == Player.BLACK && toRow == 0)) {
	        // Promote pawn to queen
	        piecesBox.remove(pieceAt(fromCol, fromRow));
	        piecesBox.add(new ChessPiece(toCol, toRow, player, Rank.QUEEN, player == Player.WHITE ? ChessConstants.wQueen : ChessConstants.bQueen));
	    }
	}
	
	

	@Override
	public String toString() {
		String desc = "";
		
		for (int row = 7; row >= 0; row--) {
			desc += "" + row;
			for (int col = 0; col < 8; col++) {
				ChessPiece p = pieceAt(col, row);
				if (p == null) {
					desc += " .";
				} else {
					desc += " ";
					switch (p.getRank()) {
					case KING: 
						desc += p.getPlayer() == Player.WHITE ? "k" : "K";
						break;
					case QUEEN: 
						desc += p.getPlayer() == Player.WHITE ? "q" : "Q";
						break;
					case BISHOP: 
						desc += p.getPlayer() == Player.WHITE ? "b" : "B";
						break;
					case ROOK: 
						desc += p.getPlayer() == Player.WHITE ? "r" : "R";
						break;
					case KNIGHT: 
						desc += p.getPlayer() == Player.WHITE ? "n" : "N";
						break;
					case PAWN: 
						desc += p.getPlayer() == Player.WHITE ? "p" : "P";
						break;
					}
				}
			}
			desc += "\n";
		}
		desc += "  0 1 2 3 4 5 6 7";
		
		return desc;
	}
}
*/

import java.util.HashSet;
import java.util.Set;

public class ChessModel {
    private Set<ChessPiece> piecesBox = new HashSet<>();
    private PlayerColour playerInTurn = PlayerColour.WHITE;
    private boolean isBlackFirst = false;

    ChessModel() {
        reset();
    }

    public void reset() {
        piecesBox.clear();

        for (int i = 0; i < 2; i++) {
            piecesBox.add(new ChessPiece(0 + i * 7, isBlackFirst ? 7 : 0, PlayerColour.BLACK, Rank.ROOK,
                    ChessConstants.bRook));
            piecesBox.add(new ChessPiece(0 + i * 7, isBlackFirst ? 0 : 7, PlayerColour.WHITE, Rank.ROOK,
                    ChessConstants.wRook));

            piecesBox.add(new ChessPiece(1 + i * 5, isBlackFirst ? 7 : 0, PlayerColour.BLACK, Rank.KNIGHT,
                    ChessConstants.bKnight));
            piecesBox.add(new ChessPiece(1 + i * 5, isBlackFirst ? 0 : 7, PlayerColour.WHITE, Rank.KNIGHT,
                    ChessConstants.wKnight));

            piecesBox.add(new ChessPiece(2 + i * 3, isBlackFirst ? 7 : 0, PlayerColour.BLACK, Rank.BISHOP,
                    ChessConstants.bBishop));
            piecesBox.add(new ChessPiece(2 + i * 3, isBlackFirst ? 0 : 7, PlayerColour.WHITE, Rank.BISHOP,
                    ChessConstants.wBishop));
        }

        for (int i = 0; i < 8; i++) {
            piecesBox.add(new ChessPiece(i, isBlackFirst ? 6 : 1, PlayerColour.BLACK, Rank.PAWN, ChessConstants.bPawn));
            piecesBox.add(new ChessPiece(i, isBlackFirst ? 1 : 6, PlayerColour.WHITE, Rank.PAWN, ChessConstants.wPawn));
        }

        piecesBox.add(new ChessPiece(3, isBlackFirst ? 7 : 0, PlayerColour.BLACK, Rank.QUEEN, ChessConstants.bQueen));
        piecesBox.add(new ChessPiece(3, isBlackFirst ? 0 : 7, PlayerColour.WHITE, Rank.QUEEN, ChessConstants.wQueen));
        piecesBox.add(new ChessPiece(4, isBlackFirst ? 7 : 0, PlayerColour.BLACK, Rank.KING, ChessConstants.bKing));
        piecesBox.add(new ChessPiece(4, isBlackFirst ? 0 : 7, PlayerColour.WHITE, Rank.KING, ChessConstants.wKing));

        playerInTurn = PlayerColour.WHITE;
    }

    public void setBlackPlayerBoard(boolean value) {
        this.isBlackFirst = value;
        reset();
    }

    void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece movingPiece = pieceAt(fromCol, fromRow);
        if (movingPiece == null || fromCol == toCol && fromRow == toRow) {
            return;
        }

        ChessPiece target = pieceAt(toCol, toRow);
        if (target != null) {
            if (target.getPlayer() == movingPiece.getPlayer()) {
                return;
            } else {
                piecesBox.remove(target);
            }
        }
        //if (isValidMove(movingPiece, fromCol, fromRow, toCol, toRow)) {
            piecesBox.remove(movingPiece);
            piecesBox.add(new ChessPiece(toCol, toRow, movingPiece.getPlayer(), movingPiece.getRank(), movingPiece.getImgName()));
            playerInTurn = playerInTurn == PlayerColour.WHITE ? PlayerColour.BLACK : PlayerColour.WHITE;
        //}
    }


    

    ChessPiece pieceAt(int col, int row) {
        for (ChessPiece chessPiece : piecesBox) {
            if (chessPiece.getCol() == col && chessPiece.getRow() == row) {
                return chessPiece;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder desc = new StringBuilder();

        for (int row = 7; row >= 0; row--) {
            desc.append(row);
            for (int col = 0; col < 8; col++) {
                ChessPiece p = pieceAt(col, row);
                if (p == null) {
                    desc.append(" .");
                } else {
                    desc.append(" ");
                    switch (p.getRank()) {
                        case KING:
                            desc.append(p.getPlayer() == PlayerColour.WHITE ? "k" : "K");
                            break;
                        case QUEEN:
                            desc.append(p.getPlayer() == PlayerColour.WHITE ? "q" : "Q");
                            break;
                        case BISHOP:
                            desc.append(p.getPlayer() == PlayerColour.WHITE ? "b" : "B");
                            break;
                        case ROOK:
                            desc.append(p.getPlayer() == PlayerColour.WHITE ? "r" : "R");
                            break;
                        case KNIGHT:
                            desc.append(p.getPlayer() == PlayerColour.WHITE ? "n" : "N");
                            break;
                        case PAWN:
                            desc.append(p.getPlayer() == PlayerColour.WHITE ? "p" : "P");
                            break;
                    }
                }
            }
            desc.append("\n");
        }
        desc.append("  0 1 2 3 4 5 6 7");
        desc.append("\n");
        desc.append("tessss");
        return desc.toString();
    }
}
