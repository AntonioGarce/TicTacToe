public class Player {

    protected String sign;
    GameField gameField;
    int isShotReady = 1;

    public Player(String sign) {
        this.sign = sign;
    }

    boolean shot(int x, int y) {
        gameField = GameField.getInstance();
        if (!gameField.isCellBusy(x, y)) {
            gameField.cell[x][y] = sign;
            return true;
        }
        return false;
    }

    boolean win() {
        gameField = GameField.getInstance();
        return gameField.checkWin(this.sign);
    }
}
