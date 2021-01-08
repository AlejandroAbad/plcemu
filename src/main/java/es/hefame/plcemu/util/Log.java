package es.hefame.plcemu.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	public enum LogMode {
		SERVER, CLIENT
	};

	private static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss dd/MM/yyy");
	private static String path;
	private static FileWriter fw;
	private static LogMode mode = LogMode.CLIENT;

	public static void init() {
		mode = LogMode.CLIENT;
	}

	public static void init(String path) {
		mode = LogMode.SERVER;
		Log.path = path;
		Log.fw = null;

		try {
			Log.fw = new FileWriter(path, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getPath() {
		return path;
	}

	public static void reset() {
		if (mode == LogMode.CLIENT)
			return;

		if (fw != null)
			try {
				fw.close();
			} catch (IOException ignore) {
			}

		File f = new File(path);
		f.delete();

		init(path);
	}
	
	public static void prompt() {
		if (mode == LogMode.CLIENT) {
			System.out.print(SDF.format(new Date()) + " | > ");
		}
	}

	public static void d(String data) {
		if (mode == LogMode.CLIENT) {
			System.out.println(data);
		} else {
			try {
				fw.write(data);
				fw.write('\n');
				fw.flush();
			} catch (Exception e) {
			}
		}
	}

	public static void i(String data) {
		if (mode == LogMode.CLIENT) {
			System.out.println(SDF.format(new Date()) + " | " + data);
		} else {
			try {
				fw.write(SDF.format(new Date()) + " | " + data);
				fw.write('\n');
				fw.flush();
			} catch (Exception e) {
			}
		}
	}

}
