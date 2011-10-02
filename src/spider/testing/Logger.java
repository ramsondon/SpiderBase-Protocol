package spider.testing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

	private static Logger instance = new Logger();
	
	private BufferedWriter writer;	
	
	private Logger() {
		init();
	}
	
	public static Logger getInstance() {
		return instance;
	}
	
	private void init() {
		// TODO init logger from config file
		try {
			FileWriter fw = new FileWriter("log.txt", true);
			this.writer = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void log(String message) {
		try {
			writer.write(message);
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
