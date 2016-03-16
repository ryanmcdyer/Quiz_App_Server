import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by ryan on 15/03/16.
 */

public class ServerLookForClients {



    public static void main(String args[]) {
        final int PORT = 8080;
        ClientThreadManager ctm = new ClientThreadManager();
        String myipv4address = getIPAddress();
        ServerSocket serverSocket = null;
        Socket socket = null;

        System.out.println("My IP is " + myipv4address);
        System.out.println("I'm waiting on port " + PORT);

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                System.out.println("Ready to accept");
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("Client @ " + clientIP + " is joining");

            ctm.addClient(clientIP, socket);
            //new ClientThread(socket).start();
            //clients.put(clientIP, new ClientThread(socket));
            /*if(clients.size() > 1) {
                for (String s: clients.keySet()){

                    String key = s.toString();
                    System.out.println("Client connected @ " + key);


                }
            }*/
        }
    }

    private static String getIPAddress() {
        String addr = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while(enumNetworkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while(enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (!(inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isMulticastAddress())) {
                        if (inetAddress.getHostAddress().matches("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$")) {
                            addr = inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if(addr.length()<1) {
            return null;
        }
        return addr;
    }
}

class ClientThreadManager extends Thread {

    private HashMap<String, ClientThread> clients;
    private HashSet<String> readyClients;

    boolean isClientAlive = false;
    boolean isClientReadyToPlay = false;

    public ClientThreadManager() {
        this.clients = new HashMap<>();
        this.readyClients = new HashSet<>();
    }

    public void addClient(String string, Socket socket) {
        ClientThread ct = new ClientThread(socket);
        ct.start();
        clients.put(string, ct);
    }

    private void manageClients() {
        if(clients.size() > 1) {
            for (String key: clients.keySet()){
                isClientAlive = clients.get(key).isActive();
                if(!isClientAlive) {
                    clients.remove(key);
                    System.out.println(key + " removed for being not connected");
                }
                else {
                    if(clients.get(key).isReadyToPlay()) {
                        readyClients.add(key);
                    } else {
                        readyClients.remove(key);
                    }
                }
                System.out.println("Client connected @ " + key);
            }
        }
    }

    public void run() {
        while(true) {
            if(clients.size() == 0) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            manageClients();
            while(readyClients.size() > 1) {
                //If there's 2 people ready to play eachother
                String[] tmpClients = (String[] ) readyClients.toArray();
                //match them
                matchClients(tmpClients[0], tmpClients[1]);
            }
        }
    }

    private void matchClients(String p1, String p2){
        //select a host
        int rand = (int) (Math.random()*100);
        try {
            if (rand > 0.5) {
                //p1 is host
                clients.get(p1).setAsHost();
                clients.get(p2).setAsClient();

                // p1.setReadyToAccept();
                clients.get(p2).setOpponentIP(p1);
            } else {
                //p2 is host
                clients.get(p2).setAsHost();
                clients.get(p1).setAsClient();

                // p2.setReadyToAccept();
                clients.get(p1).setOpponentIP(p2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        readyClients.remove(p1);
        readyClients.remove(p2);
    }
}