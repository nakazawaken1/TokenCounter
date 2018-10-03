
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import jp.qpg.TokenCounter.CounterType;

@SuppressWarnings("javadoc")
class TokenCounterTest {

	@Test
	void count() throws IOException {
		try(Scanner scanner = new Scanner(getClass().getResourceAsStream("Example.java"))) {
			scanner.useDelimiter("\\Z");
			assertEquals(51, CounterType.JAVA.counter().count(scanner.next(), (type, token) -> System.out.println(type + ": " + token)));
		}
	}
}
