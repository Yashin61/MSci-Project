public Movement minimax(int depth, Movement move, boolean player)
        {
        boolean isEmpty = Machine.getMoves().isEmpty();

        if(isEmpty || depth == 0)
        {
        move.setScore(heuristicValue(0));
        return move;
        }
        if(player == true)
        return Maximize(depth, move, player);
        else
        return Minimize(depth, move, player);
        }


//Maximize computer
private Movement Maximize(int depth, Movement movement, boolean player) {
        ArrayList<Movement> possibleMoves = Machine.getMoves();

        int i = 0;
        while (i < possibleMoves.size()) {
        Machine.Move(possibleMoves.get(i));
        Board.rotateBoard();
        Movement score = minimax(depth - 1, possibleMoves.get(i), false);
        int currentScore = score.getPoints();

        Board.rotateBoard();
        Machine.undoMove(possibleMoves.get(i));

        i++;
        }

        movement.setScore(currentScore);
        return movement;
        }

//Minimize player/human
private Movement Minimize(int depth, Movement movement, boolean player) {
        ArrayList<Movement> possibleMoves = Machine.getMoves();

        int i = 0;
        while (i < possibleMoves.size()) {
        Machine.Move(possibleMoves.get(i));
        Board.rotateBoard();
        Movement score = minimax(depth - 1, possibleMoves.get(i), true);
        int currentScore = score.getPoints();
        Board.rotateBoard();
        Machine.undoMove(possibleMoves.get(i));
        i++;

        movement.setScore(currentScore);
        return movement;
        }


//heu
//Evaluate
public int heuristicValue(int point) {
        point = point + (primaryEval(0, 0) + secodaryEval(0, 0));
        Board.rotateBoard();
        point = point - (primaryEval(0, 0) + secodaryEval(0, 0));
        Board.rotateBoard();
        return -point;
        }

//Primary storage
public int primaryEval(int point, int i) {
        while (i < 8) {
        int j = 0;
        while (j < 8) {
        if (tableBoard[i][j] == 'X') {
        normalPiece++;
        }
        if (tableBoard[i][j] == 'K') {
        kingPiece++;
        }
        j++;
        }
        i++;
        }
        point = normalPiece + kingPiece * 3;
        return point;
        }
//Secodary storage
public int secodaryEval(int point, int i) {
        int[][] content = {
        {0, 4, 0, 4, 0, 4, 0, 4},
        {4, 0, 3, 0, 3, 0, 3, 0},
        {0, 3, 0, 2, 0, 2, 0, 4},
        {4, 0, 2, 0, 1, 0, 3, 0},
        {0, 3, 0, 1, 0, 2, 0, 4},
        {4, 0, 2, 0, 2, 0, 3, 0},
        {0, 3, 0, 3, 0, 3, 0, 4},
        {4, 0, 4, 0, 4, 0, 4, 0},};

        while (i < 8) {
        int j = 0;
        while (j < 8) {
        if (Character.isUpperCase(tableBoard[i][j])) {
        point = point + content[i][j];
        }
        j++;
        }
        i++;
        }
        return point;
        }