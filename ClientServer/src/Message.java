import java.io.Serializable;

public class Message implements Serializable{
	private String tresc;
	private String autor;
	private String czas;
	
	public Message(String czas, String autor, String tresc) {
		this.czas = czas;
		this.autor = autor;
		this.tresc = tresc;
	}

	public String getTresc() {
		return tresc;
	}

	public String getAutor() {
		return autor;
	}

	public String getCzas() {
		return czas;
	}
}
