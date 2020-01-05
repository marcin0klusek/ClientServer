import java.awt.Container;
import java.awt.Dimension;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server extends JFrame{
	
	private static final int PORT = 4999;
	private static ServerSocket ss;
	private static int amountOfClients = 0;
	private static ArrayList<Message> wiadomosci = new ArrayList<Message>();
	private static double listVer = 1.0;
	private JScrollPane jcpChat;
	private JTextArea chat = new JTextArea(15,80);
	
	public static void main(String[] args) {
		new Server();
	}
	
	public Server() {
		setSize(new Dimension(500, 400));
		setLocation(100, 100);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Serwer czatu grupowego");
		setResizable(false);
		
		setGui();
		
		startServer();
	}

	private void startServer() {
		try {
			ss = new ServerSocket(PORT);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Podczas startu serwera program napotkał problem.\nServer error: 1");
			e.printStackTrace();
		}
		
		JOptionPane.showMessageDialog(null, "Serwer wystartował bez problemu. Trwa nasłuchiwanie...");
		while (true) {
			Socket socket = new Socket();
            try {
            	socket = ss.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            //gdy klient polaczy sie z serwerem.

    		System.out.println("\n\nServer: client nr. " + ++amountOfClients + " is connected on port ");
            new ClientThread(socket, this).start();
        }
	}

	private void setGui() {
		Container pane = getContentPane();
		
		jcpChat = new JScrollPane(chat);
        chat.setLineWrap(true);
        chat.setWrapStyleWord(true);
        
		chat.setEditable(false);
		
		pane.add(jcpChat);
		
		chat.append("\tSerwer wystartował:\t" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
		pack();
		repaint();
	}

	protected void newMessage(Message m) {
		Message lastMsg = new Message("", "", "");
		if(wiadomosci.size()>0)
			lastMsg = wiadomosci.get(wiadomosci.size()-1);
		wiadomosci.add(m); listVer += 0.01;
		if(lastMsg.getAutor().equals(m.getAutor())) {
			chat.append("\n" + m.getTresc());
		}else {
			chat.append("\n\n" + m.getCzas() + "    " + m.getAutor() + "\n" + m.getTresc());
		}
		System.out.println("Wersja listy: " + listVer);
		
		JScrollBar vertical = jcpChat.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
	}
}


/*TODO
 * lista aktywnych uzytkowniku(user nickname) jlist.repaint() jako refresh
 */