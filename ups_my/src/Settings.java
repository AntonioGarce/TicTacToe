import javax.swing.*;

public class Settings extends JFrame {
    Settings gameSettingsForm = this;

    public Settings() {
        setTitle("Settings:");
        setBounds(500, 100, 240, 250);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JLabel jLabelMode = new JLabel("Choose lobby:");
        add(jLabelMode);

        JButton jButtonLobby1 = new JButton("Lobby 1");
        add(jButtonLobby1);

        JButton jButtonLobby2 = new JButton("Lobby 2");
        add(jButtonLobby2);

        setVisible(true);
    }
}
