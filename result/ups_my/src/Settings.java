import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class Settings extends JFrame {

    public Settings() {
        setTitle("Settings:");
        setBounds(500, 200, 465, 315);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel settingPanel = new  JPanel(new GridLayout(3,2));
        JPanel buttonPanel = new JPanel(new GridLayout());

        add(settingPanel);
        add(buttonPanel);

        JTextField txtFieldIp = new JTextField(16);
        try{
            System.out.println(InetAddress.getLocalHost());
            txtFieldIp.setText(InetAddress.getLocalHost().getHostAddress());
        }
        catch(UnknownHostException e) {

            e.printStackTrace();
        }

        JTextField txtFieldPort = new JTextField(5);
        JTextField txtFieldNickName = new JTextField(10);

        JLabel labelIp = new JLabel("  Server ADDRESS:");
        JLabel labelPort = new JLabel("      PORT:");
        JLabel labelNick = new JLabel("  NickName:");

        settingPanel.add(labelIp);
        settingPanel.add(txtFieldIp);
        settingPanel.add(labelPort);
        settingPanel.add(txtFieldPort);
        settingPanel.add(labelNick);
        settingPanel.add(txtFieldNickName);

        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");

        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                global.network.connectToServer(txtFieldIp.getText(),Integer.parseInt(txtFieldPort.getText()));
                if(global.network.isConnectedToServer()){
                    // get nickname from text field
                    global.nickName = txtFieldNickName.getText();
                    // send nickname to the server
                    global.network.write("NickName");
                    global.network.write(global.nickName);
                    // read the response from server
                    String res = global.network.read();
//                    System.out.println(res);
                    //if login is success
                    if(res.contains("success"))
                    {
                        // send response to the server
                        global.network.write("success");
                        // System.out.println("waiting lobby...\n");
                        global.network.write("Lobby");
                        // receive the lobby state from server
                        res = global.network.read();
                        // get the number of lobby
                        global.numLobby = Integer.parseInt(res);
                        System.out.println("Number of Lobby:");
                        System.out.println(global.numLobby);
                        // send response to the server
                        global.network.write(res);
                        // need number of room
                        global.network.write("Room");
                        // receive the room state from server
                        res = global.network.read();
                        // get the number of lobby
                        global.numRoom = Integer.parseInt(res);
                        System.out.println("Number of Room:");
                        System.out.println(global.numRoom);
                        // send response to the server
                        global.network.write(res);
                        //
                        setVisible(false);
                        dispose();
                        //
                        SelectingRoom selectingRoom = new SelectingRoom(global.numLobby, global.numRoom);
                    }

                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        setVisible(true);
    }
}
