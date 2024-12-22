package Chess.piece;

import Chess.main.GamePanel;
import Chess.main.Type;

public class Rook extends Piece{

    public Rook(int color, int col, int row) {
        super(color, col, row);

        type = Type.ROOK;
        
        if(color == GamePanel.WHITE){
            image = getImage("/Chess/resources/piece/w-rook");
        }
        else{
            image = getImage("/Chess/resources/piece/b-rook");
        }
    }
    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) ==  false){
            if(targetCol == preCol || targetRow == preRow){
                if(isValidSquare(targetCol, targetRow) && pieceIsOnStraightLine(targetCol, targetRow) == false){
                    return true;
                }
            }
        }
        return false;
    }
}
