package robot;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
Tomá¹ Malich
 */

public class Robot {
    
    // server = 1 ... klient = 2
    private int programState;
    private String serverName;
    private int port;
    public static ServerWindow window;
    private Program program = null;
    
    public Robot(int state, String serverName, int port) {
        this.programState = state;
        this.serverName = serverName;
        this.port = port;
        if (state == 1) {
            System.out.println("Program pracuje jako server");
            System.out.println("Server nasloucha na portu "+port);
            window = new ServerWindow(port);  
            window.setVisible(true);
            program = new Server(this.port);            
        }
        if (state == 2) {
            System.out.println("Program pracuje jako client");
            System.out.println("Klient se pokousi pripojit k serveru "+serverName+":"+port);
            program = new Client(this.serverName, this.port);
        }        
        
        
    }
    
    public static void main(String[] args) {        
        Robot robot = new Robot(1, "", 3999);         
    }   
    
}

class Server implements Program {   
    
    private ServerSocket server;
    private int port;
    private List<ClientThread> clients;
    
    public Server(int port) {
        this.port = port;
        clients = new java.util.ArrayList<ClientThread>();
        try {
            server = new ServerSocket(port);
            while(true) {                
                Socket socket = server.accept();
                System.out.println("Nalezeno spojeni");
                ClientThread ct = new ClientThread(socket);
                clients.add(ct);
                ct.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            if(server != null) {                
                for(ClientThread clt: getClients()) clt.close();
                clients.clear();
                try { server.close(); }
                catch(IOException e) {}
            }
        }
    }
    
    public synchronized List<ClientThread> getClients() {
        return clients;
    }
    
    private class ClientThread extends Thread {
        Socket socket;
        PrintStream out;
        BufferedReader in;
        Boolean pozdraven = false;
        
        ClientThread(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintStream(socket.getOutputStream()); 
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            }
            catch(IOException e) {
                e.printStackTrace(System.err);
                close();
            }
            out.print("200 Ahoj. Oslovuj mne Geddy. \r\n");
            System.out.println("Client vytvoren");
        }
        
        @Override
        public void run() {
            try {
                while(true) {                    
                    String message = in.readLine();
                    String command = null;
                    if (message != null) {
                        Robot.window.addLine(message);
                        System.out.println("Prichozi zprava: "+message);                    
                        if (message.startsWith("Geddy ")) {
                            command = message.substring(6);                            
                        }
                    }
                    if (command != null && command.equals("VLEVO")) {                        
                        
                    }
                    if (command != null && command.equals("KROK")) {                        
                        
                    }
                    if (command != null && command.equals("ZVEDNI")) {
                        
                    }
                    if (command != null && command.startsWith("OPRAVIT") && command.length() == 9) {
                        int procesorNumber = Integer.parseInt(command.substring(8, 9));                        
                    }
                }
            }
            catch(IOException e) {
                System.err.println("Kvuli chybe odpojen klient.");
                e.printStackTrace(System.err);
            }
            finally {
                close(); 
            }
        }
        public void close() {
            getClients().remove(this); 
            try {
                out.close(); 
                in.close(); 
                socket.close(); 
            } catch(IOException e) {}
        }
    }
}

class Client implements Program {
    public Client(String serverName, int port) {
         
    }
}

interface Program {
    
}

class GameBoard {
    
}

class ServerWindow extends JFrame {
    JLabel jLabel1;
    JTextArea output;
    public ServerWindow(int port) {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(320, 200);
        this.setLayout(null);
        this.setTitle("Robot server");
        jLabel1 = new javax.swing.JLabel();
        jLabel1.setText("Server nasloucha na portu: "+port);
        jLabel1.setBounds(10, 5, 200, 40);
        output = new JTextArea();
        output.setBounds(10, 50, 200, 80);
        output.setEditable(false);      
        JScrollPane sc = new JScrollPane(output);
        sc.setBounds(10, 50, 285, 105);
        this.add(sc);
        //this.add(output);              
        this.add(jLabel1);
        this.setVisible(true);
    }
    public void addLine(String line) {
        output.append(line+"\n");
        repaint();
    }    
}
