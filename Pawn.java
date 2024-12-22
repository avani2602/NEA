package Chess.piece;

import Chess.main.GamePanel;
import Chess.main.Type;

public class Pawn extends Piece{

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        
        type = Type.PAWN;

        if(color == GamePanel.WHITE){
            image = getImage("/Chess/resources/piece/w-pawn");
        }
        else{
            image = getImage("/Chess/resources/piece/b-pawn");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
            int moveValue;
            if(color == GamePanel.WHITE){
                moveValue = -1;
            }
            else{
                moveValue = 1;
            }

            hittingPiece = getHittingPiece(targetCol, targetRow);

            if(targetCol == preCol && targetRow == (preRow + moveValue) && hittingPiece == null){
                return true;
            }

            if(targetCol == preCol && 
               targetRow == (preRow + moveValue * 2) && 
               hittingPiece == null && 
               moved == false && 
               pieceIsOnStraightLine(targetCol, targetRow) == false
            ){
                return true;
            }

            if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingPiece != null && hittingPiece.color != color){
                return true;
            }

            //En Passant
            if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue){
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == targetCol && piece.row == preRow && piece.twoStepped == true){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }

        }
        return false;
    }
    
}
