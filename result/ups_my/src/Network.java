import java.util.Scanner;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Network {

    private String serverIp = "localhost";
    private int serverPort = 22222;
    private Scanner scanner = new Scanner(System.in);
    private boolean isConnected = false;
    Socket socket = null;
     BufferedReader in;
    // PrintWriter out;
//    DataInputStream in;
    DataOutputStream out;

    public Network() {
        System.out.println("network is created.");
    }

    public String getServerAddr(){
        return serverIp;
    }

    public int getServerPort(){
        return serverPort;
    }

    public void setServerAddr(String ipaddr){
        serverIp = ipaddr;
    }

    public void setServerPort(int port){
        serverPort = port;
    }

    public boolean isConnectedToServer(){
        return isConnected;
    }
    /**
        func: connect to the server with input of ip address and port
        ipaddr: Ip address of Server
        port: port number of Server
     */
    public void connectToServer(String ipaddr, int port) {

        while (port < 1 || port > 65535) {
            System.out.println("The port you entered was invalid, please input another port: ");
            port = scanner.nextInt();
        }

        try {
            System.out.println(ipaddr);
            System.out.println(port);

            serverIp = ipaddr;
            serverPort = port;

            socket = new Socket(ipaddr,port);
            isConnected = true;

//            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream( socket.getOutputStream() );
            in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
            // String message_distant = in.readLine();
            // System.out.println(message_distant);

        }catch (UnknownHostException e) {

            e.printStackTrace();
        }catch (IOException e) {

            e.printStackTrace();
        }

    }

    /**
        func: connect to the server.
     */
    public void connectToServer() {

        while (serverPort < 1 || serverPort > 65535) {
            System.out.println("The port you entered was invalid. ");
        }

        try {
            System.out.println(serverIp);
            System.out.println(serverPort);

            socket = new Socket(serverIp,serverPort);
            isConnected = true;

//            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream( socket.getOutputStream() );

            in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
            // String message_distant = in.readLine();
            // System.out.println(message_distant);

        }catch (UnknownHostException e) {

            e.printStackTrace();
        }catch (IOException e) {

            e.printStackTrace();
        }
    }
    /**
        close the socket
     */
    public void close(){
        try{
            socket.close();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String read(){
        String line ="";
        try{
            line = in.readLine();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return line;
    }

    public void write(String str){
        try{
            out.writeBytes(str+'\n');
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

}
