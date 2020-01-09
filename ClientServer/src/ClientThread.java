import java.io.*;
import java.net.*;

public class ClientThread extends Thread {
    protected Socket socket;
    protected Server server;
    private String user = "";
	private OutputStream outputStream = null;
	private ObjectOutputStream objectOutputStream = null;
    
    
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
                	server.notification("Klient " + user +" się rozłączył.");
                    socket.close();
                    return;
                } else {
                	if(msg.getTresc().equals("///autor")) {
                		user = msg.getAutor();
                		server.notification("Klient " + user + " dołączył do czatu!");
                	}else
                		server.newMessage(msg);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (SocketException se) {
            	server.notification("Server: Klient " + user +" się rozłączył.");
            	return;
            } catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
    protected void send(Message msg) {
    	try {
			if(outputStream == null)
				outputStream = socket.getOutputStream();
			if(objectOutputStream == null)
				objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(msg);
		} catch (IOException e) {
			System.out.println("Nie znaleziono klienta " + user);
		}
    }
}
