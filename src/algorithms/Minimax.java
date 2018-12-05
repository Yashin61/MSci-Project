package algorithms;
//import logic.Move;
import ui.Board;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Minimax
{
    private Board.color color;
    private Board.color oppColor;
//    private Tree descisionTree;
    private Move lastMove;
    private Board node;
    private Move move;
    private int score;
    private ArrayList<gameLogic.Tree> children;

    public Minimax(Board.color color, Board node, Move move, int score, Minimax ... children)
    {
        this.node = node;
        this.score = score;
        this.move = move;
        this.children = new ArrayList<>(Arrays.asList(children));
        this.color = color;

        if(color == Board.color.RED)
            oppColor = Board.color.BLACK;
        else
            oppColor = Board.color.RED;
    }

//    public Board.color getColor() { return color; } // returning the color of opponent as computer

    private Board getBoard() { return node; }  // returning board of tree

    private Move getMove() { return move; }   // returning move of tree

    private int getScore() { return score; }   // returning score of tree

    private List<Minimax> getChildren() { return children; }   // returning tree's children

    private int getNumChildren() { return children.size(); }   // returning number of children of tree

    private void setScore(int newVal) {score = newVal; }  // changing score of tree by newVal

    private Minimax getChild(int index) { return children.get(index); }   // returning a child at chosen index

    private void addChild(Minimax child) { children.add(child); }    // adding child to the tree

    private void addChildren(Minimax ... children)  // adding multiple children to tree
    {
        for(Minimax child : children)
            addChild(child);
    }

    private Move getAIMove(Board board)  // picking and returning a move from all possible moves based on current state of board
    {
        descisionTree = makeDescisionTree(board);
        lastMove = pickMove();
        return lastMove;
    }

    /*
    creating a tree with four layers with all possible moves for the next three moves of the game. "board" is the state
    the tree will be based on. returning a tree with all possible moves
    */
    private Minimax makeDescisionTree(Board board)
    {
        Minimax mainTree = new Minimax(board, null, score(board));
        ArrayList<Move> moves;

        if(board.isJumped())    // handling multiple jumps
            moves = board.getJumps(lastMove.movRow, lastMove.movCol);
        else
            moves = board.getAllLegalMovesForColor(color);

        for(Move move : moves)  // making second layer
        {
            Board temp = copyBoard(board);
            temp.movePiece(move);
            temp.handleJump(move);
            Minimax firstLayer = new Minimax(temp, move, score(temp));
            ArrayList<Move> secondMoves = temp.getAllLegalMovesForColor(oppColor);

            for(Move sMove : secondMoves)   // making third layer
            {
                Board temp2 = copyBoard(temp);
                temp2.movePiece(sMove);
                temp2.handleJump(sMove);
                Minimax secondLayer = new Minimax(temp2, sMove, score(temp2));
                ArrayList<Move> thirdMoves = temp2.getAllLegalMovesForColor(color);

                for(Move tMove : thirdMoves)    // making fourth layer
                {
                    Board temp3 = copyBoard(temp2);
                    temp3.movePiece(tMove);
                    temp3.handleJump(tMove);
                    secondLayer.addChild(new Minimax(temp3, tMove, score(temp3)));
                }

                firstLayer.addChild(secondLayer);
            }

            mainTree.addChild(firstLayer);
        }

        return mainTree;
    }

    private Move pickMove() // returning selected move
    {
        int max = -13;
        int index = 0;

        for(int i = 0; i < descisionTree.getNumChildren(); i++)
        {
            Minimax child = descisionTree.getChild(i);
            int smin = 13;

            for(Minimax sChild : child.getChildren())   // finding max leaf
            {
                int tMax = -13;

                for(Minimax tchild : sChild.getChildren())
                {
                    if(tchild.getScore() >= tMax)
                        tMax = tchild.getScore();
                }

                sChild.setScore(tMax);

                if(sChild.getScore() <= smin)   // finding min in the third layer
                    smin = sChild.getScore();
            }

            child.setScore(smin);

            if(child.getScore() >= max) // finding max in the second layer and saving index
            {
                max = child.getScore();
                index = i;
            }
        }

        return descisionTree.getChild(index).getMove();
    }

    private int score(Board board)  // scoring the given board based on a weighted system
    {
        if(color == Board.color.RED)
            return board.getRedWeightedScore() - board.getBlackWeightedScore();
        else
            return board.getRedWeightedScore() - board.getRedWeightedScore();
    }

    private Board copyBoard(board board)    // creating new board with the same information as the given board
    {
        Board.color[][] color = new board.color[8][8];

        for(int row = 0; row < 8; row++)
            for(int col = 0; col < 8; col++)
                color[row][col] = board.getInfoAtPosition(row, col);

        return new Board(color, board.getNumRed(), board.getNumBlack(), board.getNumRedKing(), board.getNumBlackKing());
    }
}
