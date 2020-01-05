import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

class ClientSide {

	@Test
	void test() {
		   System.out.println(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));  
	}

}
