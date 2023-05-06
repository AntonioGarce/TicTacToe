import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Form extends JFrame {
    GameField gameField;
    public Form(){
        setTitle("XO game GUI");
        setBounds(500, 100, 465, 515);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gameField = GameField.getInstance();

        JPanel buttonPanel = new JPanel(new GridLayout());

        add(gameField, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        JButton btnStart = new JButton("Start new game");
        JButton btnEnd = new JButton("End the game");

        buttonPanel.add(btnStart);
        buttonPanel.add(btnEnd);

        btnEnd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(btnStart.getText());
                if(global.isLoggedIn) {
                    SelectingRoom selectingRoom = new SelectingRoom(global.numLobby, global.numRoom);
                }else {
                    Settings gameSettingsForm = new Settings();
                }
            }
        });

        setVisible(true);
    }

    public void startPlay(int order)  {
        System.out.println("start playing.");
        System.out.println(gameField.r1.getState());
        if(order == 1)
        {
            gameField.startNewGame("X");
            gameField.setTickAvailable();

            if(gameField.r1.getState() == Thread.State.NEW) {
                gameField.r1.start();
            }else{

//                gameField.r1.join();
                gameField.r1.run();
            }

        }else{
            gameField.startNewGame("O");
//            gameField.waitResponse();
            gameField.r1.start();
        }
    }
}
