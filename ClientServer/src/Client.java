import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Client extends JFrame implements KeyListener {

	private static Socket s;
	private double ver = 0.0;
	private String user;
	private Thread reading;

	protected JTextArea chat;
	protected JTextArea msg;
	protected JButton send;
	private JScrollPane jcpChat;
	
	private OutputStream outputStream = null;
	private ObjectOutputStream objectOutputStream = null;
	
	public static void main(String[] args) {
		new Client();
	}
	
	public Client() {
		getUsername();

		connectToServer();

		new Thread(() -> JOptionPane.showMessageDialog(null, "Witaj " + user + "!")).start();
		setFrame();
		setGui();
	}
	
	public Client(String name) {
		user = name;
		
		connectToServer();

		new Thread(() -> JOptionPane.showMessageDialog(null, "Witaj " + user + "!")).start();
		setFrame();
		setGui();
	}

	private void getUsername() {
		int repeat = 0;
		do {
			try {
				if (repeat >= 1 && repeat < 3) {
					user = JOptionPane.showInputDialog("Musisz podac swoja nazwe:").trim();
				} else if (repeat >= 3) {
					JOptionPane.showMessageDialog(null, "Nie wprowadzono nazwy, zamykam program.");
					System.exit(0);
					System.out.println("repeat: " + repeat);
				} else {
					user = JOptionPane.showInputDialog("Nazwa uzytkownika:").trim();
				}
			} catch (NullPointerException e) {
				user = "";
			}
			repeat++;
		} while (user.equals(""));
	}

	private void setFrame() {
		setSize(new Dimension(500, 400));
		setLocation(400, 100);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Czat grupowy" + user);
		setResizable(false);
	}

	private void connectToServer() {
		try {
			s = new Socket("localhost", 4999);
		} catch (UnknownHostException | ConnectException e) {
			JOptionPane.showMessageDialog(null, "Serwer nie odpowiada, proszę spróbować pózniej. Przepraszamy za problemy.\nServer error: 1");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Serwer nie odpowiada, proszę spróbować pózniej. Przepraszamy za problemy.\nServer error: 2");
		}
		if(s != null) {
			reading = new Thread(() -> runRead());
			reading.start();// wyrazenie lambda
		}else
			System.exit(0);
		send(new Message(getCurrentTime(), user, "///autor"));
	}

	private void runRead() {
		while (true) {
			try {
				read();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void setGui() {
		JPanel pane = new JPanel(new GridBagLayout());
		chat = new JTextArea(15, 80);
		msg = new JTextArea(15, 80);
		jcpChat = new JScrollPane(chat);
		JScrollPane jcpMsg = new JScrollPane(msg);
		send = new JButton("Wyślij");

		chat.setLineWrap(true);
		chat.setWrapStyleWord(true);

		msg.setLineWrap(true);
		msg.setWrapStyleWord(true);

		chat.setEditable(false);

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 40;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(jcpChat, c);

		c.anchor = GridBagConstraints.PAGE_START;
		c.gridy = 1;
		pane.add(jcpMsg, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 1.0; // request any extra vertical space
		c.anchor = GridBagConstraints.PAGE_END; // bottom of space
		c.insets = new Insets(10, 0, 0, 0); // top padding
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 0; // 2 columns wide
		c.gridy = 2;
		pane.add(send, c);
		msg.addKeyListener(this);

		add(pane);

		msg.requestFocusInWindow();

		pack();
		repaint();

		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!msg.getText().trim().equals("")) {
					Message wiadomosc = new Message(getCurrentTime(), user, msg.getText().trim());
//					chat.append(
//							wiadomosc.getCzas() + "   " + wiadomosc.getAutor() + "\n" + wiadomosc.getTresc() + "\n\n");
					send(wiadomosc);
					msg.requestFocusInWindow();
				}
				if (!msg.getText().equals(""))
					msg.setText("");
			}
		});
	}

	private String getCurrentTime() {
		return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
	}

	private void send(Message msg) {
		if (msg != null) {
			
			try {
				if(outputStream == null)
					outputStream = s.getOutputStream();
				if(objectOutputStream == null)
					objectOutputStream = new ObjectOutputStream(outputStream);
				objectOutputStream.writeObject(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void read() {
		String[] preFixes = { "/txt ", "/ver " }; // lista dostepnyhc prefixow
		int substringPrefix = preFixes[0].length(); // liczba znakow prefixa

		InputStreamReader in;
		try {
			in = new InputStreamReader(s.getInputStream());
			BufferedReader bf = new BufferedReader(in);
			String str = null;
			try {
				str = bf.readLine(); // wczytanie wiadomosci
			}catch (SocketException e) {
				send.setEnabled(false);
				msg.setEditable(false);

				send.repaint();
				msg.repaint();
				
				JOptionPane.showMessageDialog(this, "Serwer przestał odpowiadać, proszę zrestartować aplikację lub spróbować ponownie.");
				

				reading.stop();
			}

			if (str != null) { // jezeli wiadomosc nie jest pusta
				int i;
				for (i = 0; i < preFixes.length; i++) // sprawdzenie ktory to prefix
					if (str.startsWith(preFixes[i])) {
						str = str.substring(substringPrefix); // wyciecie prefixa komendy i zostawienie zawartosci
						break;
					}

				switch (i) {
				case 0: // txt
					chat.append(str);
					break;
				case 1: // ver
					double newVer = Double.parseDouble(str);
					if (ver < newVer) {
						System.out.println("Odebrano nowsza wersje rozmowy " + newVer + ". Obecnie posiadasz " + ver);
						ver = newVer;
					} else {
						System.out.println(
								"Odebrano starsza lub obecna wersje rozmowy " + newVer + ". Obecnie posiadasz " + ver);
					}
					break;
				}
				
				JScrollBar vertical = jcpChat.getVerticalScrollBar();
				vertical.setValue( vertical.getMaximum() );

			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Wystąpił błąd odbierania wiadomości przez klienta.");
			e.printStackTrace();
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			if (!msg.getText().trim().equals(""))
				send.doClick();
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			msg.setText("");
		}
	}
}

/*
 * TODO PO KOLEI: wybranie nazwy, polaczenie sie z serwerem, wiadomosc
 * powitalna, okno
 *
 */

class AppendingObjectOutputStream extends ObjectOutputStream {

	  public AppendingObjectOutputStream(OutputStream out) throws IOException {
	    super(out);
	  }

	  @Override
	  protected void writeStreamHeader() throws IOException {
	    reset();
	  }

	}