package Chess.piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import Chess.main.Board;
import Chess.main.GamePanel;
import Chess.main.Type;

public class Piece implements Cloneable{
    
    public Type type;
    public BufferedImage image;
    public int x, y;
    public int col, row, preCol, preRow;
    public int color;
    public Piece hittingPiece;
    public boolean moved, twoStepped;

    @Override
    public Piece clone() {
        try {
            return (Piece) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Piece(int color, int col, int row){
        this.color = color;
        this.col = col;
        this.row = row;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;
    }

    public BufferedImage getImage(String imagePath){
        BufferedImage image = null;

        try{
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return image;
    }

    public int getX(int col){
        return col * Board.SQUARE_SIZE;
    }

    public int getY(int row){
        return row * Board.SQUARE_SIZE;
    }

    public int getCol(int x){
        return (x + Board.HALF_SQAURE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getRow(int y){
        return (y + Board.HALF_SQAURE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getIndex(){
        for(int index = 0; index < GamePanel.simPieces.size(); index++){
            if(GamePanel.simPieces.get(index) == this){
                return index;
            }
        }

        return 0;
    }

    public void updatePositions(){

        //TO chekc En Passant
        if(type == Type.PAWN){
            if(Math.abs(row - preRow) == 2){
                twoStepped = true;
            }
        }

        x = getX(col);
        y = getY(row);
        preCol = getCol(x);
        preRow = getRow(y);
        moved = true;
    }

    public void resetPosition(){
        col = preCol;
        row = preRow;
        x = getX(col);
        y = getY(row);
    }

    public boolean canMove(int targetCol, int targetRow){
        return false;
    }

    public boolean isWithinBoard(int targetCol, int targetRow){
        if(targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7){
            return true;
        }
        return false;
    }

    public boolean isSameSquare(int targetCol, int targetRow){
        if(targetCol == preCol && targetRow == preRow){
            return true;
        }
        return false;
    }

    public Piece getHittingPiece(int targetCol, int targetRow){
        for(Piece piece : GamePanel.simPieces){
            if(piece.col == targetCol && piece.row == targetRow && piece != this){
                return piece;
            }
        }
        return null;
    }

    public boolean isValidSquare(int targetCol, int targetRow){
        hittingPiece = getHittingPiece(targetCol, targetRow);

        //Square is empty
        if(hittingPiece == null){
            return true;
        }
        //Square is occupied
        else{
            if(hittingPiece.color != this.color){
                return true;
            }
            else{
                hittingPiece = null;
            }
        }

        return false;
    }

    public boolean pieceIsOnStraightLine(int targetCol, int targetRow){
        for(int c = preCol-1; c > targetCol; c--){
            for(Piece piece : GamePanel.simPieces){
                if(piece.col == c && piece.row == targetRow){
                    hittingPiece = piece;
                    return true;
                }
            }
        }
        
        for(int c = preCol+1; c < targetCol; c++){
            for(Piece piece : GamePanel.simPieces){
                if(piece.col == c && piece.row == targetRow){
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        for(int r = preRow-1; r > targetRow; r--){
            for(Piece piece : GamePanel.simPieces){
                if(piece.row == r && piece.col == targetCol){
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        for(int r = preRow+1; r < targetRow; r++){
            for(Piece piece : GamePanel.simPieces){
                if(piece.row == r && piece.col == targetCol){
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean pieceIsOnDiagonalLine(int targetCol, int targetRow){
        if(targetRow < preRow){
            for(int c = preCol - 1; c > targetCol; c--){
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == preRow - diff){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }

            for(int c = preCol + 1; c < targetCol; c++){
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == preRow - diff){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }

        if(targetRow > preRow){
            for(int c = preCol - 1; c > targetCol; c--){
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == preRow + diff){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }

            for(int c = preCol + 1; c < targetCol; c++){
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == preRow + diff){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Piece other = (Piece) obj;
        return type == other.type && 
            color == other.color && 
            col == other.col && 
            row == other.row;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + color;
        result = 31 * result + col;
        result = 31 * result + row;
        return result;
    }

    public void draw(Graphics2D g2){
        // Resize images
        double scaleFactor = 0.65;
        int pieceSize = (int) (Board.SQUARE_SIZE * scaleFactor);
    
        // Calculate offset
        int offset = (Board.SQUARE_SIZE - pieceSize) / 2;

        g2.drawImage(image, x + offset, y + offset, pieceSize, pieceSize, null);
    }
}
