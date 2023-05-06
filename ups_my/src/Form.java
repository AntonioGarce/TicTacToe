import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class Form extends JFrame {
    public Form(){
        setTitle("XO game GUI");
        setBounds(500, 100, 465, 515);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GameField gameField = GameField.getInstance();
        JPanel buttonPanel = new JPanel(new GridLayout());

        //add(gameField, BorderLayout.CENTER);
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
                Settings gameSettingsForm = new Settings();
            }
        });

        setVisible(true);
    }
}
