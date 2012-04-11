package robot;

import java.io.*;
import java.util.Scanner;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Robot {
    private static Server server = null;
    private static Klient klient = null;
    
    public static void main(String[] args) throws IOException {        
        if (args.length == 2) {
            System.out.println("Program je v roli klienta.");
            String ip = args[0];
            int port = Integer.parseInt(args[1]);
            Robot.klient = new Klient(ip, port);
        } else           
        if (args.length == 1) {
            System.out.println("Program je v roli serveru.");
            int port = Integer.parseInt(args[0]);
            Robot.server = new Server(port);
        } else {
            Scanner in = new Scanner(System.in);
            System.out.println("Server - 1, Klient - 2");
            int x = in.nextInt();
            if (x == 1) {
                System.out.println("Program je v roli serveru.");
                System.out.print("Zadejte port: ");
                int port = in.nextInt();
                Robot.server = new Server(port);
            }
            if (x == 2) {
                System.out.println("Program je v roli klienta.");
                System.out.print("Zadejte ip adresu: ");
                String ip = in.next();
                System.out.print("Zadejte port: ");
                int port = in.nextInt();
                Robot.klient = new Klient(ip, port);
                Robot.klient.start();
            }
        }
    }
}

class Klient extends Thread {
    
    private Socket socket;
    private String ip;
    private int port;
    private PrintStream out;
    private BufferedReader in; 
    private BufferedWriter wr;
    private String name;

    Klient(String ip, int port) throws UnknownHostException, IOException {
        this.ip = ip;
        this.port = port;               
        this.socket = new Socket(ip, port);         
        this.out = new PrintStream(socket.getOutputStream(), true);
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(6000000);
        socket.setSendBufferSize(Integer.MAX_VALUE);
        socket.setReceiveBufferSize(Integer.MAX_VALUE);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //this.wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String jmeno = readCommand(in); 
        jmeno = jmeno.substring(jmeno.indexOf("Oslovuj mne")+12);
        jmeno = jmeno.substring(0, jmeno.indexOf('.'));
        System.out.println("Jmeno robota: "+jmeno);
        this.name = jmeno;        
    }
    
    int x = 0;
    int y = 0;
    int orientation = -1;    
    String respond = null;
    
    @Override
    public void run() {      
        out.print(command(2));
        try {
            while((respond = readCommand(in)) != null && socket.isConnected()) {
                this.sleep(0);
                if (respond != null) {
                    System.out.println("Server: "+respond);
                    if (respond.startsWith("210")) {
                        String tajemstvi = parseTajemstvi(respond);
                        System.out.println("TAJEMSTVI JE: " + tajemstvi);
                        respond = "x";
                        try {
                            this.socket.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Klient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    else if (respond.startsWith("572")) {
                        try {
                            this.socket.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Klient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    else if (respond.startsWith("580")) {
                        int processor = parseProcessor(respond);
                            if (processor >= 1 && processor <= 9) {
                                System.out.println(name+" OPRAVIT "+processor);
                                out.print(name+" OPRAVIT "+processor+"\r\n");
                                respond = null;
                            }
                        respond = null;
                    }
                    else if (respond.startsWith("240")) {
                        boolean nula = false;
                        this.x = parseX(respond);
                        this.y = parseY(respond);
                        printPosition();
                        if (x == 0 && y == 0) {
                                out.print(command(3));
                                nula = true;
                            }
                        if (orientation == -1) {
                            out.print(command(1));

                            try {
                                respond = readCommand(in);
                            } catch (IOException ex) {
                                Logger.getLogger(Klient.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.out.println(respond);
                            if (respond.startsWith("240")) {
                                int tempX = parseX(respond);
                                int tempY = parseY(respond);
                                if (tempY > y) orientation = 0;
                                if (tempX < x) orientation = 1;
                                if (tempY < y) orientation = 2;
                                if (tempX > x) orientation = 3;
                                this.x = tempX;
                                this.y = tempY;
                                printPosition();
                                printOrientation();
                            }
                            if (respond.startsWith("580")) {
                                int processor = parseProcessor(respond);
                                out.print(command(4, processor));
                            }
                        }
                        if (orientation == 0 && nula == false) {
                            if (y < 0) {
                                out.print(command(1));
                            }
                            if (y > 0) {
                                out.print(command(2));
                                out.print(command(2));
                                orientation = 2;
                            }
                            if (y == 0) {
                                out.print(command(2));
                                orientation = 1;
                            }
                        }
                        if (orientation == 1 && nula == false) {
                            if (x == 0 && y == 0) {
                                out.print(command(3));
                            }
                            if (x > 0) {
                                out.print(command(1));
                            }
                            if (x < 0) {
                                out.print(command(2));
                                out.print(command(2));
                                orientation = 3;
                            }
                            if (x == 0) {
                                out.print(command(2));
                                orientation = 2;
                            }
                        }
                        if (orientation == 2 && nula == false) {
                            if (x == 0 && y == 0) {
                                out.print(command(3));
                            }
                            if (y > 0) {
                                out.print(command(1));
                            }
                            if (y < 0) {
                                out.print(command(2));
                                out.print(command(2));
                                orientation = 0;
                            }
                            if (y == 0) {
                                out.print(command(2));
                                orientation = 3;
                            }
                        }
                        if (orientation == 3 && nula == false) {
                            if (x == 0 && y == 0) {
                                out.print(command(3));
                            }
                            if (x < 0) {
                                out.print(command(1));
                            }
                            if (x > 0) {
                                out.print(command(2));
                                out.print(command(2));
                                orientation = 1;
                            }
                            if (x == 0) {
                                out.print(command(2));
                                orientation = 0;
                            }
                        }
                        respond = null;

                    }
                    else {
                        out.print(command(2));
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }   
    
    private void flush(String command) {
        try {
            wr.write(command);
            wr.flush();
        } catch (IOException ex) {
            Logger.getLogger(Klient.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void printOrientation() {
        System.out.print("Robot miri na ");
        if (orientation == 0) System.out.println("SEVER");
        if (orientation == 1) System.out.println("ZAPAD");
        if (orientation == 2) System.out.println("JIH");
        if (orientation == 3) System.out.println("VYCHOD");
    }
    
    private void printPosition() {
        //System.out.println("Souradnice robota: x = " + x + " y = " + y);
    }
    
    private String parseTajemstvi(String command) {
        command = command.substring(command.indexOf("USPECH")+6);
        return command;
    }
    
    private int parseProcessor(String command) {        
        command = command.substring(command.indexOf("PROCESORU")+10);          
        int n = Integer.parseInt(command);        
        return n;
    }
    
    private int parseX(String command) {
        command = command.substring(command.indexOf('(')+1, command.indexOf(')'));
        String[] a = command.split(",");
        int x = Integer.parseInt(a[0]);
        return x;
    }
    
    private int parseY(String command) {
        command = command.substring(command.indexOf('(')+1, command.indexOf(')'));
        String[] a = command.split(",");
        int y = Integer.parseInt(a[1]);
        return y;
    }
    
    private String command(int command) {
        // command 1 - KROK
        // command 2 - VLEVO
        // command 3 - ZVEDNI        
        if (command == 1) {
            System.out.println(name+" KROK");
            return name+" KROK\r\n";
        }
        if (command == 2) {
            System.out.println(name+" VLEVO");
            return name+" VLEVO\r\n";
        }
        if (command == 3) {
            System.out.println(name+" ZVEDNI");
            return name+" ZVEDNI\r\n";
        }        
        return null;
    }
    
    private String command(int command, int processorNumber) {
        // command 4 - OPRAVIT
        if (command == 4 && processorNumber >= 1 && processorNumber <= 9) {
            System.out.println(name+" OPRAVIT "+processorNumber);
            String prikaz = name+" OPRAVIT "+processorNumber+"\r\n";
            return prikaz;
        }
        return null;
    }
        
        private int b = 1024 * 512;        
        private StringBuilder stringB = new StringBuilder(2*b);
        
        private String readCommand(BufferedReader bufferedReader) throws IOException {            
            int y;
            char[] buffer = new char[b];
            String s;        

            while ((y = bufferedReader.read(buffer)) != -1) {
                stringB.append(buffer, 0, y);
                buffer = new char[b];
                if ((s = stringB.toString()).contains("\r\n")) {
                    s = s.substring(0, s.indexOf("\r\n"));
                    stringB = stringB.delete(0, s.length() + 2);
                    return s;
                }               
            }      
            System.out.println("Chyba");
            return null;
        }
    
}


class Server {
    
    private ServerSocket server;
    private int port;
    private List<Server.ClientThread> clients;
    
    Server(int port) {
        this.port = port;        
        clients = new java.util.ArrayList<Server.ClientThread>();
        try {
            server = new ServerSocket(port);
            while(true) {                
                Socket socket = server.accept();
                System.out.println("Nalezeno spojeni");
                Server.ClientThread ct = new Server.ClientThread(socket);
                clients.add(ct);
                ct.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            if(server != null) {                
                for(Server.ClientThread clt: getClients()) clt.close();
                clients.clear();
                try { server.close(); }
                catch(IOException e) {}
            }
        }
    }  
    
    public synchronized List<Server.ClientThread> getClients() {
        return clients;
    }
    
    private class ClientThread extends Thread {
        Socket socket;
        PrintStream out;
        BufferedReader in;
        Boolean pozdraven = false;
        Gameboard game = new Gameboard();
        String secred = "Pro hloupeho kazdy hloupy";
        
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
        }

        String message = null;
        @Override
        public void run() {
            try {
                while((message = readCommand(in)) != null) {

                        //String message = readCommand(in);
                        System.out.println("Client: "+message);
                        String command = null;
                        boolean ok = false;
                        if (message != null) {
                            //System.out.println("Prichozi zprava: "+message);
                            if (message.startsWith("Geddy ")) {
                                command = message.substring(6);
                            }
                            if (message.contains("Geddy ")){
                                command = message.substring(message.indexOf("Geddy")+6);
                            }
                        }
                        if (command != null && command.equals("VLEVO")) {
                            ok = true;
                            int result = game.turn();
                            if (result == 0) {
                                out.print("240 OK ("+game.getX()+","+game.getY()+")\r\n");
                                System.out.println("Server: "+"240 OK ("+game.getX()+","+game.getY()+")\r\n");
                            }
                        }
                        if (command != null && command.equals("KROK")) {
                            ok = true;
                            int result = game.go();
                            if (result == 0) {
                                out.print("240 OK ("+game.getX()+","+game.getY()+")\r\n");
                                System.out.println("Server: "+"240 OK ("+game.getX()+","+game.getY()+")\r\n");
                            }
                            if (result == 91) {
                                out.print("530 HAVARIE\r\n");
                                System.out.println("Server: "+"530 HAVARIE\r\n");
                                this.close();
                                break;
                            }
                            if (result == 92) {
                                out.print("572 ROBOT SE ROZPADL\r\n");
                                System.out.println("Server: "+"572 ROBOT SE ROZPADL\r\n");
                                this.close();
                                break;
                            }
                            if (result == 81) {
                                out.print("580 SELHANI PROCESORU 1\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 1\r\n");
                            }
                            if (result == 82) {
                                out.print("580 SELHANI PROCESORU 2\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 2\r\n");
                            }
                            if (result == 83) {
                                out.print("580 SELHANI PROCESORU 3\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 3\r\n");
                            }
                            if (result == 84) {
                                out.print("580 SELHANI PROCESORU 4\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 4\r\n");
                            }
                            if (result == 85) {
                                out.print("580 SELHANI PROCESORU 5\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 5\r\n");
                            }
                            if (result == 86) {
                                out.print("580 SELHANI PROCESORU 6\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 6\r\n");
                            }
                            if (result == 87) {
                                out.print("580 SELHANI PROCESORU 7\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 7\r\n");
                            }
                            if (result == 88) {
                                out.print("580 SELHANI PROCESORU 8\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 8\r\n");
                            }
                            if (result == 89) {
                                out.print("580 SELHANI PROCESORU 9\r\n");
                                System.out.println("Server: "+"580 SELHANI PROCESORU 9\r\n");
                            }
                        }
                        if (command != null && command.equals("ZVEDNI")) {
                            ok = true;
                            int result = game.pickUp();
                            if (result == 0) {
                                out.print("210 USPECH " + this.secred + "\r\n");
                                System.out.println("Server: "+"210 USPECH " + this.secred + "\r\n");
                                this.close();
                                break;
                            }
                            if (result == 91) {
                                out.print("550 NELZE ZVEDNOUT ZNACKU\r\n");
                                System.out.println("Server: "+"550 NELZE ZVEDNOUT ZNACKU\r\n");
                                this.close();
                                break;
                            }
                        }
                        if (command != null && command.startsWith("OPRAVIT") && command.length() == 9) {
                            ok = true;
                            int procesorNumber = Integer.parseInt(command.substring(8, 9));
                            if (procesorNumber < 1 || procesorNumber > 9) {
                                out.print("500 NEZNAMY PRIKAZ\r\n");
                                System.out.println("Server: "+"500 NEZNAMY PRIKAZ\r\n");
                            }
                            int result = game.fix(procesorNumber);
                            if (result == 0) out.print("240 OK ("+game.getX()+","+game.getY()+")\r\n");
                            if (result == 91) {
                                out.print("571 PROCESOR FUNGUJE\r\n");
                                System.out.println("Server: "+"571 PROCESOR FUNGUJE\r\n");
                                this.close();
                                break;
                            }
                        }
                        if (message != null && ok == false) {
                            out.print("500 NEZNAMY PRIKAZ\r\n");
                            System.out.println("Server: "+"500 NEZNAMY PRIKAZ\r\n");
                        }

                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        
        private int x = 1024 * 512;        
        private StringBuilder stringB = new StringBuilder(2*x);
        
        private String readCommand(BufferedReader bufferedReader) throws IOException {            
            int y;
            char[] buffer = new char[x];
            String s;        

            while ((y = bufferedReader.read(buffer)) != -1) {
                stringB.append(buffer, 0, y);
                buffer = new char[x];
                if ((s = stringB.toString()).contains("\r\n")) {
                    s = s.substring(0, s.indexOf("\r\n"));
                    stringB = stringB.delete(0, s.length() + 2);
                    return s;
                }
            }        
            return null;
        }
        
        public void close() {           
            try {
                out.close(); 
                in.close(); 
                socket.close(); 
            } catch(IOException e) {}
            getClients().remove(this); 
        }
    }
    
}

class Gameboard {

    int x = 0;
    int y = 0;
    int orientation = 0;
    int numberOfSteps = 0;
    
    boolean processor1 = true;
    boolean processor2 = true;
    boolean processor3 = true;
    boolean processor4 = true;
    boolean processor5 = true;
    boolean processor6 = true;
    boolean processor7 = true;
    boolean processor8 = true;
    boolean processor9 = true;
    
    public Gameboard() {
        this.x = (int)(Math.random()*41) - 20;
        this.y = (int)(Math.random()*41) - 20;
        this.orientation = (int)(Math.random()*4);
        System.out.println("Souradnice robota: " + x + " " + y);
        System.out.println("Orientace robota: " + orientation);
    }
    
    int getX() {
        return this.x;
    }
    
    int getY() {
        return this.y;
    }
    
    int getOrientation() {
        return this.orientation;
    }
    
    int pickUp() {
        // 0 - zvedne znacku
        // 91 - nezvedne znacku
        if (this.x == 0 && this.y == 0) return 0;
        return 91;
    }
    
    int crash() {
        int processor = (int)(Math.random()*8 + 1);
        if (this.numberOfSteps == 10) {
            this.numberOfSteps = 0;
            return processor;
        }
        this.numberOfSteps++;
        int crash = (int)(Math.random()*5);
        if (crash == 0) return processor;
        return 0;
    }
    
    int fix(int processorNumber) {
        // 0 - opraveni v poradku
        // 91 - procesor fungoval
        if (processorNumber == 1) {
            if (this.processor1 == false) {
                this.processor1 = true;
                return 0;
            } else return 91;
        }
        if (processorNumber == 2) {
            if (this.processor2 == false) {
                this.processor2 = true;
                return 0;
            } else return 91;
        }
        if (processorNumber == 3) {
            if (this.processor3 == false) {
                this.processor3 = true;
                return 0;
            } else return 91;
        }
        if (processorNumber == 4) {
            if (this.processor4 == false) {
                this.processor4 = true;
                return 0;
            } else return 91;
        }
        if (processorNumber == 5) {
            if (this.processor5 == false) {
                this.processor5 = true;
                return 0;
            } else return 91;
        }
        if (processorNumber == 6) {
            if (this.processor6 == false) {
                this.processor6 = true;
                return 0;
            } else return 91;
        }
        if (processorNumber == 7) {
            if (this.processor7 == false) {
                this.processor7 = true;
                return 0;
            } else return 91;
        }
        if (processorNumber == 8) {
            if (this.processor8 == false) {
                this.processor8 = true;
                return 0;
            } else return 91;
        }
        if (processorNumber == 9) {
            if (this.processor9 == false) {
                this.processor9 = true;
                return 0;
            } else return 91;
        }
        return 90;
    }
    
    int turn() {               
        if (orientation == 0) {
            this.orientation++;
            return 0;
        }
        if (orientation == 1) {
            this.orientation++;
            return 0;
        }
        if (orientation == 2) {
            this.orientation++;
            return 0;
        }
        if (orientation == 3) {
            this.orientation = 0;
            return 0;
        }
        return 90;
    }
    
    int go() {
        // 0 - otoceni v poradku
        // 91 - mimo oblash
        // 92 - neopraveny procesor
        // 8x - procesor crash
        int processorCrash = crash(); 
        
        if (this.processor1 == false) return 92;
        if (this.processor2 == false) return 92;
        if (this.processor3 == false) return 92;
        if (this.processor4 == false) return 92;
        if (this.processor5 == false) return 92;
        if (this.processor6 == false) return 92;
        if (this.processor7 == false) return 92;
        if (this.processor8 == false) return 92;
        if (this.processor9 == false) return 92;
        
        if (processorCrash == 1) this.processor1 = false;
        if (processorCrash == 2) this.processor2 = false;
        if (processorCrash == 3) this.processor3 = false;
        if (processorCrash == 4) this.processor4 = false;
        if (processorCrash == 5) this.processor5 = false;
        if (processorCrash == 6) this.processor6 = false;
        if (processorCrash == 7) this.processor7 = false;
        if (processorCrash == 8) this.processor8 = false;
        if (processorCrash == 9) this.processor9 = false;
        
        if (orientation == 0) {            
            int fail = processorFail(processorCrash);
            if (fail != 0) return fail;
            this.y++;
            if (y > 21) return 91;
            return 0;
        } 
        if (orientation == 1) {       
            int fail = processorFail(processorCrash);
            if (fail != 0) return fail;
            this.x--;
            if (x < -21) return 91;
            return 0;
        } 
        if (orientation == 2) {            
            int fail = processorFail(processorCrash);
            if (fail != 0) return fail;
            this.y--;
            if (y < -21) return 91;
            return 0;
        }               
        if (orientation == 3) {            
            int fail = processorFail(processorCrash);
            if (fail != 0) return fail;
            this.x++;
            if (x > 21) return 91;
            return 0;
        }    
        return 90;
    }
    
    int processorFail(int x) {
        if (x == 1) return 81;
        if (x == 2) return 82;
        if (x == 3) return 83;
        if (x == 4) return 84;
        if (x == 5) return 85;
        if (x == 6) return 86;
        if (x == 7) return 87;
        if (x == 8) return 88;
        if (x == 9) return 89;     
        return 0;
    }
    
}


