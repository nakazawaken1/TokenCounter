package jp.qpg;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Token counter for java
 * 
 * @author nakazawaken1@gmail.com
 */
@SuppressWarnings("javadoc")
public interface TokenCounter {

	/**
	 * usage
	 * <ul>
	 * <li>java jp.qpg.TokenCounter #current folder all extensions</li>
	 * <li>java jp.qpg.TokenCounter -DshowToken #current folder all extensions with
	 * token output</li>
	 * <li>java jp.qpg.TokenCounter -Dfile.encoding=UTF-8 . .java .cs #current
	 * folder .java, .cs extensions UTF-8 encoding</li>
	 * </ul>
	 * 
	 * @param args [0]root folder [1-]target extensions(all target if empty)
	 */
	public static void main(String[] args) {
		if (args.length <= 0) {
			args = new String[] { "." };
		}
		Path folder;
		try {
			folder = Paths.get(args[0]).toRealPath();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		BiConsumer<TokenType, String> onTokenParsed = (type, token) -> {
		};
		if (Boolean.parseBoolean(System.getProperty("showToken"))) {
			onTokenParsed = (type, token) -> System.out.println(type + ": " + token);
		}
		String[] extensions = Arrays.copyOfRange(args, 1, args.length);
		System.out.println("[target folder] " + folder);
		String targetExtensions = extensions.length > 0 ? String.join(" ", extensions) : "(all)";
		System.out.println("[target extensions] " + targetExtensions);
		int[] files = countFiles((file, count) -> System.out.println(file.toAbsolutePath() + ": " + count), onTokenParsed,
				folder, extensions);
		System.out.println(files[0] + " files, " + files[1] + " tokens");
	}

	enum TokenType {
		COMMENT, NUMBER, ID, STRING, CHARACTER, MARK
	}

	enum CounterType {
		JAVA(Java::new, ".java");
		public final Supplier<TokenCounter> instance;
		public final List<String> extensions;

		CounterType(Supplier<TokenCounter> instance, String... extensions) {
			this.instance = instance;
			this.extensions = Arrays.asList(extensions);
		}

		public static CounterType of(String file) {
			file = file.toLowerCase();
			for (CounterType i : CounterType.values()) {
				if (i.extensions.stream().anyMatch(file::endsWith)) {
					return i;
				}
			}
			throw new CounterNotFoundException(file);
		}

		public TokenCounter counter() {
			return instance.get();
		}
	}

	@SuppressWarnings("serial")
	static class CounterNotFoundException extends RuntimeException {
		public CounterNotFoundException(String file) {
			super(file);
		}
	}

	int count(String source, BiConsumer<TokenType, String> onTokenParsed);

	default int count(String source) {
		return count(source, (type, token) -> {
		});
	}

	static int[] countFiles(BiConsumer<Path, Integer> onCounted, BiConsumer<TokenType, String> onTokenParsed,
			Path folder, String... suffixes) {
		AtomicInteger files = new AtomicInteger();
		AtomicInteger total = new AtomicInteger();
		boolean empty = suffixes.length <= 0;
		try {
			Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (empty || Stream.of(suffixes).anyMatch(suffix -> file.toString().endsWith(suffix))) {
						try {
							TokenCounter counter = CounterType.of(file.toString()).counter();
							String source = new String(Files.readAllBytes(file), System.getProperty("file.encoding"));
							int count = counter.count(source, onTokenParsed);
							onCounted.accept(file, count);
							files.incrementAndGet();
							total.addAndGet(count);
						} catch (CounterNotFoundException e) {
							System.err.println(e.toString());
						}
					}
					return super.visitFile(file, attrs);
				}
			});
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return new int[] { files.get(), total.get() };
	}

	static class Java implements TokenCounter {
		@Override
		public int count(String source, BiConsumer<TokenType, String> onTokenParsed) {
			this.source = source;
			max = source.length();
			index = 0;
			count = 0;
			tokenStart = index;
			this.onTokenParsed = onTokenParsed;
			try {
				do {
					skipSpaces();
				} while (comment() || number() || text() || mark() || id());
			} catch (EOTException e) {
			}
			return count;
		}

		@SuppressWarnings("serial")
		static class EOTException extends RuntimeException {
		}

		String source;
		int max;
		int index;
		int count;
		int tokenStart;
		BiConsumer<TokenType, String> onTokenParsed;

		boolean done(TokenType type) {
			count++;
			String token = source.substring(tokenStart, index).trim();
			if (!token.isEmpty()) {
				onTokenParsed.accept(type, token);
			}
			tokenStart = index;
			return true;
		}

		char eat() {
			if (index >= max) {
				throw new EOTException();
			}
			char c = source.charAt(index++);
			return c;
		}

		boolean eat(String text) {
			int length = text.length();
			boolean b = index + length < max && source.startsWith(text, index);
			if (b) {
				index += length;
			}
			return b;
		}

		void back() {
			index--;
		}

		static final String spaces = " \t\r\n";
		static final String numbers = ".0123456789";
		static final String separators = "(){}[];:,.?";
		static final String operators = "!#$%&-=^~\\|@`+*/<>";
		static final String marks = operators + separators;
		static final String marksSpaces = marks + spaces;

		void skipSpaces() {
			while (spaces.indexOf(eat()) >= 0)
				;
			back();
			tokenStart = index;
		}

		boolean comment() {
			if (eat("/*")) {
				while (!eat("*/")) {
					eat();
				}
				return done(TokenType.COMMENT);
			} else if (eat("//")) {
				while (eat() != '\n')
					;
				back();
				return done(TokenType.COMMENT);
			}
			return false;
		}

		boolean number() {
			char c = eat();
			if (numbers.indexOf(c) >= 0) {
				TokenType type = c == '.' ? TokenType.MARK : TokenType.NUMBER;
				while (numbers.indexOf(eat()) >= 0) {
					type = TokenType.NUMBER;
				}
				back();
				return done(type);
			}
			back();
			return false;
		}

		boolean id() {
			char c = eat();
			if (marks.indexOf(c) < 0) {
				while (marksSpaces.indexOf(eat()) < 0)
					;
				back();
				return done(TokenType.ID);
			}
			back();
			return false;
		}

		boolean text() {
			char c = eat();
			if ("\"'".indexOf(c) >= 0) {
				while (c != eat() || source.charAt(index - 2) == '\\')
					;
				return done(c == '"' ? TokenType.STRING : TokenType.CHARACTER);
			}
			back();
			return false;
		}

		boolean mark() {
			char c = eat();
			if (separators.indexOf(c) >= 0) {
				return done(TokenType.MARK);
			}
			if (marks.indexOf(c) >= 0) {
				while (operators.indexOf(eat()) >= 0)
					;
				back();
				return done(TokenType.MARK);
			}
			back();
			return false;
		}
	}
}
