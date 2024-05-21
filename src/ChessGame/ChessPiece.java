package ChessGame;

enum PlayerColour{
	BLACK,
	WHITE
}

enum Rank {
	KING,
	QUEEN,
	BISHOP,
	ROOK,
	KNIGHT,
	PAWN,
}

public class ChessPiece {
	private final int col;
	private final int row;
	private final PlayerColour player;
	private final Rank rank;
	private final String imgName;

	public ChessPiece(int col, int row, PlayerColour player, Rank rank, String imgName) {
		super();
		this.col = col;
		this.row = row;
		this.player = player;
		this.rank = rank;
		this.imgName = imgName;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	public PlayerColour getPlayer(){
		return player;
	}

	public Rank getRank() {
		return rank;
	}

	public String getImgName() {
		return imgName;
	}
}
