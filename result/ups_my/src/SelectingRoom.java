import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectingRoom extends JFrame {
    int numLobby;
    int numRoom;
    public SelectingRoom(int maxLobby, int maxRoom) {
        numLobby = maxLobby;
        numRoom = maxRoom;
        setTitle("Settings:");
        setBounds(500, 200, 465, 315);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel settingPanel = new  JPanel(new GridLayout(3,2));
        JPanel buttonPanel = new JPanel(new GridLayout());

        add(settingPanel);
        add(buttonPanel);

        JTextField txtFieldLobby = new JTextField("1",16);
        JTextField txtFieldRoom = new JTextField("1",5);

        JLabel labelLobby = new JLabel("  Select Lobby:");
        JLabel labelRoom = new JLabel("  Select Room:");

        settingPanel.add(labelLobby);
        settingPanel.add(txtFieldLobby);
        settingPanel.add(labelRoom);
        settingPanel.add(txtFieldRoom);

        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");

        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int lobby = Integer.parseInt(txtFieldLobby.getText());
                int room = Integer.parseInt(txtFieldRoom.getText());
                String alertString = new String("");

                if(lobby>maxLobby) {
                    alertString = "There are " + String.valueOf(maxLobby) + " lobbies.\nYou select too large value.";
                    showAlert(alertString);
                }else if (room>maxRoom) {
                    alertString = "There are " + String.valueOf(maxRoom) + " rooms.\nYou select too large value.";
                    showAlert(alertString);
                }else{
                    global.network.write(String.valueOf(lobby));
                    String res = global.network.read();
                    if (res.contains("Room")) {
                        global.network.write(String.valueOf(room));
                        res = global.network.read();
                        switch (res){
                            case "Error":   //if the room number has an error
                                System.out.println("Error in room number. Please enter room number again.");
                                break;
                            case "Full":
                                System.out.println("Room is already full. Please enter other room number.");
                                break;
                            case "First": // if you are first player in the room.
                                System.out.println("You are first player in the room. Please wait for other player.");
                                global.inRoom = true;
                                setVisible(false);
                                dispose();
                                // wait until the second player is entered.
                                global.mainForm.startPlay(2);
                                break;
                            case "Second":  //if a player is ready in the room.
                                System.out.println("You are the second player in the room. You can start to play.");
                                global.inRoom = true;
                                setVisible(false);
                                dispose();
                                // start game
                                global.mainForm.startPlay(1);
                                break;
                            default:
                                break;
                        }
                    }

                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        setVisible(true);
    }

    public void showAlert(String alert){
        JOptionPane.showMessageDialog(this,alert,"WARNING",JOptionPane.WARNING_MESSAGE);
    }
}
