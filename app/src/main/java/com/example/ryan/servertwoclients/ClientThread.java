import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by ryan on 15/03/16.
 */
public class ClientThread extends Thread {

    protected Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    private boolean isReadyToPlay = false;
    private boolean isHost = false;
    String opponentIP = "";

    public ClientThread(Socket clientSocket) {//, String ipv4address) {
        this.socket = clientSocket;
    }

    public void run() {
        dataInputStream = null;
        dataOutputStream = null;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }

        while (true) {
            //dosomethinghere
            if(isReadyToPlay) {
                if(isHost) {
                    //dosomething
                    //eg dos.writeUTF(get ready to accept);
                } else if (opponentIP.length() > 0) {
                    //dosomething
                    //eg dos.writeUTF(connect to this IP to play);
                }

            }

        }
    }

    public boolean isActive() {
        return socket.isConnected();
    }

    public boolean isReadyToPlay() {
        return isReadyToPlay;
    }

    public void setAsHost() {
        isHost = true;
        isReadyToPlay = false;
        opponentIP = "";

    }

    public void setAsClient() {
        isHost = false;
        isReadyToPlay = false;
    }

    public void setOpponentIP(String s) throws IOException {
        opponentIP = s;
        dataOutputStream.writeUTF(opponentIP);
    }

    public static String getIPAddress() {
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