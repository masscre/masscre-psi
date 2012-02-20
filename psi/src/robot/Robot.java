package robot;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
Tomá¹ Malich
 */

public class Robot {
    
    // server = 1 ... klient = 2
    private int programState;
    private String serverName;
    private int port;
    
    private Program program = null;
    
    public Robot(int state, String serverName, int port) {
        this.programState = state;
        this.serverName = serverName;
        this.port = port;
        if (state == 1) {
            System.out.println("Program pracuje jako server");
            System.out.println("Server nasloucha na portu "+port);
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
    
    private ServerSocket serverSocket = null;
    
    public Server(int port) {
        try {
            serverSocket = new ServerSocket();
            SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port);
            serverSocket.bind(socketAddress);
            
            while(true) {
                Socket client = serverSocket.accept(); //poèkat na pøijetí klienta
                //vytvoøit proud, do kterého je mo¾né posílat øetìzce
                PrintStream output = new PrintStream(client.getOutputStream());

                output.println("Sem frajer"); //odeslat aktuální èas a datum

                try {
                    output.close(); //zavøít ná¹ vlastní výstupní proud
                    client.close(); //odpojit klienta
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
            
        } catch(IOException e) {
            e.printStackTrace();
        }
        finally {            
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                } catch(IOException e) {}
            }
        }
    }
}

class Client implements Program {
    public Client(String serverName, int port) {
         
    }
}

interface Program {
    
}
