package es.hefame.plcemu.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.hefame.plcemu.ds.Message;

public class PrettyHex {

	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyMMdd;HHmmss");

	public enum Dir {
		S, E
	}

	public static void bump(String message) {
		Log.i(message);
	}

	public static void pretty(byte[] raw) {
		
		Log.i(" HEX DUMP  |  00 01 02 03 04 05 06 07   08 09 0A 0B 0C 0D 0E 0F  ");
		Log.i("-----------+-----------------------------------------------------+---------------------|");

		StringBuilder hexBuffer	= new StringBuilder();
		StringBuilder endBuffer = new StringBuilder();

		int j;
		for (j = 1; j < raw.length + 1; j++) {


			if (j % 16 == 1) 
				hexBuffer.append(String.format(" %07X0  |  ", j / 16));

			if (raw[j - 1] < 0x20 || raw[j - 1] > 0x7F)		endBuffer.append('.');
			else											endBuffer.append((char) raw[j - 1]);
			hexBuffer.append(String.format("%02X ", raw[j - 1]));

			if (j % 16 == 8) {
				endBuffer.append(' ');
				hexBuffer.append("  ");
			}

			if (j % 16 == 0) {
				Log.i(hexBuffer.toString() + " |  " + endBuffer.toString() + "  |");
				
				hexBuffer = new StringBuilder();
				endBuffer = new StringBuilder();
			}

		}

		if (j % 16 != 0) {
			while (j % 16 != 1) {
				hexBuffer.append("   ");
				endBuffer.append(' ');

				if (j % 16 == 8) {
					hexBuffer.append("  ");
					endBuffer.append(' ');
				}
				j++;
			}

			Log.i(hexBuffer.toString() + " |  " + endBuffer.toString() + "  |");
			
			hexBuffer = new StringBuilder();
			endBuffer = new StringBuilder();
		}

		Log.i("-----------+-----------------------------------------------------+---------------------|");
		
	}
	
	public static void pretty(Message message, Dir d) {

		try {
			byte[] msg = message.encode();

			Log.i("");

			if (d == Dir.S) {
				Log.d("CSV;" + SDF.format(new Date()) + ";O;" + message.toCSV());
				Log.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			} else {
				Log.d("CSV;" + SDF.format(new Date()) + ";I;" + message.toCSV());
				Log.i("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			}
			Log.i(message.toString());
			Log.i(" HEX DUMP  |  00 01 02 03 04 05 06 07   08 09 0A 0B 0C 0D 0E 0F  ");
			Log.i("-----------+-----------------------------------------------------+---------------------|");

			StringBuilder hexBuffer	= new StringBuilder();
			StringBuilder endBuffer = new StringBuilder();

			int j;
			for (j = 1; j < msg.length + 1; j++) {


				if (j % 16 == 1) {
					hexBuffer.append(String.format(" %07X0  |  ", j / 16));
				}

				if (msg[j - 1] < 0x20 || msg[j - 1] > 0x7F)		endBuffer.append('.');	
				else											endBuffer.append((char) msg[j - 1]);
				hexBuffer.append(String.format("%02X ", msg[j - 1]));

				if (j % 16 == 8) { 
					endBuffer.append(' ');
					hexBuffer.append("  ");
				}

				if (j % 16 == 0) { 
					Log.i(hexBuffer.toString() + " |  " + endBuffer.toString() + "  |");
					
					hexBuffer = new StringBuilder();
					endBuffer = new StringBuilder();
				}

			}

			if (j % 16 != 0) {
				while (j % 16 != 1) { 
					hexBuffer.append("   ");
					endBuffer.append(' ');

					if (j % 16 == 8) {
						hexBuffer.append("  ");
						endBuffer.append(' ');
					}
					j++;
				}

				Log.i(hexBuffer.toString() + " |  " + endBuffer.toString() + "  |");
				
				hexBuffer = new StringBuilder();
				endBuffer = new StringBuilder();
			}

			Log.i("-----------+-----------------------------------------------------+---------------------|");
			if (d == Dir.S) {
				Log.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			} else {
				Log.i("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			}

		} catch (IOException e) {

		}
	}
}
