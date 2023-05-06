import com.sun.org.apache.bcel.internal.generic.RETURN;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GameField extends JPanel implements MouseListener {

    private static GameField instance = null;
    public static final int FIELD_SIZE = 450;
    public final String NOT_SIGN = "*";
    boolean gameOver = false;
    String gameOverMessage = "";
    static int linesCount = 10;
    int cellSize;
    int x;
    int y;
    boolean nextTurn = false;
    Player player1;
    Player player2;
    int gameMode = 1;
    public String[][] cell;
    public boolean isMyOrder = false;
    String mySign = "X";
    Thread r1;
    private GameField(){
        startNewGame(mySign);
        addMouseListener(this);

//        r1.start();

    }

    public static synchronized GameField getInstance() {
        if (instance == null)
            instance = new GameField();
        return instance;
    }

    public void setTickAvailable() {
        isMyOrder = true;
    }

    void startNewGame(String sign) {
        r1 = new Thread(){
            public void run(){
                try{
                    waitResponse();
                    System.out.println("thread is stopped.");
                }catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        };
        mySign = sign;
        gameOver = false;
        gameOverMessage = "";
        cellSize = FIELD_SIZE / linesCount;
        cell = new String[linesCount][linesCount];
        repaint();
        for (int i = 0; i < linesCount; i++) {
            for (int j = 0; j < linesCount; j++) {
                cell[i][j] = NOT_SIGN;
            }
        }
        setVisible(true);

    }

    boolean isCellBusy(int x, int y) {
        if (x < 0 || y < 0 || x > linesCount - 1 || y > linesCount - 1) {
            return false;
        }
        return cell[x][y] != NOT_SIGN;
    }
    public boolean isFieldFull() {
        for (int i = 0; i < linesCount; i++) {
            for (int j = 0; j < linesCount; j++) {
                if (cell[i][j] == NOT_SIGN)
                    return false;
            }
        }
        return true;
    }
    public boolean checkWin(String sign) {
        int countSignToWin = 5;
        //C
        for (int i = 0; i<countSignToWin+1;i++){
            for (int j=0; j<cell.length;j++){
                if (cell[i][j] == sign) {
                    if ((cell[i + 1][j] == sign && cell[i + 2][j] == sign && cell[i + 3][j] == sign && cell[i + 4][j] == sign)) {
                        return true;
                    }
                }
            }
        }

        //S
        for (int i = 0; i < cell.length; i++) {
            for (int j = 0; j < countSignToWin + 1; j++) {
                if (cell[i][j] == sign) {
                    //строки начиная слева (рабочий)
                    if ((cell[i][j + 1] == sign && cell[i][j + 2] == sign && cell[i][j + 3] == sign && cell[i][j + 4] == sign)) {
                        return true;
                    }
                }
            }
        }
        //D
        for (int i = 0; i <= countSignToWin; i++) {
            for (int j = 0; j <= countSignToWin; j++) {
                if (cell[i][j] == sign) {
                    if (cell[i + 1][j + 1] == sign && cell[i + 2][j + 2] == sign && cell[i + 3][j + 3] == sign && cell[i + 4][j + 4] == sign) {
                        return true;
                    }
                }
            }
        }
        //D
        for (int i = 0; i < countSignToWin + 1; i++) {
            for (int j = cell.length-1; j >= countSignToWin - 1; j--) {
                if (cell[i][j] == sign) {
                    if (cell[i + 1][j - 1] == sign && cell[i + 2][j - 2] == sign && cell[i + 3][j - 3] == sign && cell[i + 4][j - 4] == sign) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i <= this.linesCount; i++) {
            g.drawLine(0, i * this.cellSize, FIELD_SIZE, i * this.cellSize);
            g.drawLine(i * this.cellSize, 0, i * this.cellSize, FIELD_SIZE);
        }
        for (int i = 0; i < linesCount; i++) {
            for (int j = 0; j < linesCount; j++) {
                if (cell[i][j] != NOT_SIGN) {
                    if (cell[i][j] == "X") {
                        g.setColor(Color.RED);
                        g.drawLine((i * cellSize), (j * cellSize), (i + 1) * cellSize, (j + 1) * cellSize);
                        g.drawLine((i + 1) * cellSize, (j * cellSize), (i * cellSize), (j + 1) * cellSize);
                    }
                    if (cell[i][j] == "O") {
                        g.setColor(Color.BLUE);
                        g.drawOval((i * cellSize), (j * cellSize), cellSize, cellSize);
                    }
                }
            }
        }

        if (gameOver) {
            g.setColor(Color.BLACK);
            g.fillRect(0, FIELD_SIZE / 2, FIELD_SIZE, FIELD_SIZE / 8);
            g.setColor(Color.RED);
            g.setFont(new Font("Tahoma", 10, 40));
            g.drawString(gameOverMessage, 0, 19 * FIELD_SIZE / 32);
        }
    }

    public void waitResponse(){
//        System.out.println("Waiting Response\n");
//        System.out.println(mySign);
        while (!gameOver) {
            System.out.println("Waiting Response\n");
            try {
                String res = global.network.read();
                if(res.contains("END")){
                    return;
                }
                System.out.println(res);
                String[] cmd;
                String otherSign = "O";
                int x, y;
                cmd = res.split(":");
                if (cmd[0].contains("POS") || cmd[0].contains("WIN")) {
                    String[] strPos = cmd[1].split(",");
                    x = Integer.parseInt(strPos[0]);
                    y = Integer.parseInt(strPos[1]);

                    if (mySign.contains("O"))
                        otherSign = "X";

                    cell[x][y] = otherSign;
                    repaint();
                    if (checkWin(otherSign)) {
                        gameOver = true;
                        String command = "DEF:"+String.valueOf(0)+","+String.valueOf(0);
                        global.network.write(command);

                        String alertString = "You are defeated.";
                        JOptionPane.showMessageDialog(this,alertString,"WOW",JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }else {
                        setTickAvailable();
                    }

                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

        }

    }

    public void mouseClicked(MouseEvent m)
    {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("mouse clicked");
        if (isMyOrder){
            int x = e.getX()*linesCount/FIELD_SIZE;;
            int y = e.getY()*linesCount/FIELD_SIZE;;
            if(isCellBusy(x,y)){
                return;
            }
            cell[x][y]=mySign;
            repaint();

            String command = "POS:";
            if(checkWin(mySign)) {
                command = "WIN:";
                String alertString = "Congratulations! You win!!!";
                JOptionPane.showMessageDialog(this,alertString,"Winner",JOptionPane.INFORMATION_MESSAGE);
            }
            command = command+String.valueOf(x)+","+String.valueOf(y);

            global.network.write(command);
            isMyOrder = false;

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

}
