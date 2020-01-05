import java.io.*;
import java.net.*;

public class ClientThread extends Thread {
    protected Socket socket;
    protected Server server;
    private String user = "";
    public ClientThread(Socket clientSocket, Server s) {
        this.socket = clientSocket;
        server = s;
    }

    public void run() {
    	System.out.println("Wątek klienta " + socket.getInetAddress());
        InputStream inputStream = null;
        ObjectInputStream objectInputStream = null;
        Message msg = null;
        
        try {
            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
        	e.printStackTrace();
            return;
        }
        
        while (true) {
            try {
                msg = (Message) objectInputStream.readObject();
                if (msg == null) {
                	System.out.println(user + " się rozłączył ");
                    socket.close();
                    return;
                } else {
                	if(msg.getTresc().equals("///autor")) {
                		user = msg.getAutor();
                		System.out.println("Klient " + user + " dołączył do czatu!");
                	}else
                		server.newMessage(msg);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (SocketException se) {
            	System.out.println(user +" się rozłączyć");
            	return;
            } catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
}
