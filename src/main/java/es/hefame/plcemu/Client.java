package es.hefame.plcemu;

import java.io.IOException;
import java.util.Scanner;

import es.hefame.plcemu.client.Command;
import es.hefame.plcemu.client.PlcClientThread;
import es.hefame.plcemu.util.Log;

public class Client {
	private static PlcClientThread cThread = null;
	private static boolean maintainConnected = false;

	public static void main(String... strings) throws Exception {

		Log.init();
		Scanner s = new Scanner(System.in);
		String inputLine;
		

		Log.prompt();
		while (s.hasNextLine()) {
			inputLine = s.nextLine();
			if (inputLine == null || inputLine.length() == 0) {
				Log.prompt();
				continue;
			}

			Command c = new Command(inputLine);

			switch (c.getName()) {
			case "connect":
				connect(c);
				break;

			case "disconnect":
				disconnect();
				break;

			case "control":
				sendControl(c);
				break;
			
			case "mc":
				maintainConnected = !maintainConnected; 
				Log.i("Mantener online = " + maintainConnected);
				break;

			case "exit":
				disconnect();
				s.close();
				System.exit(0);
				break;
			default:
				Log.i("NO ENTIENDO....");
			}

			Log.prompt();
		}

		s.close();
	}

	public static void connect(Command c) {
		try {
			Log.i("CONECTANDO A " + c.asS(0) + " PUERTO " + c.asI(1) + " ...");
			cThread = new PlcClientThread(c.asS(0), c.asI(1), maintainConnected);
		} catch (IOException e) {
			Log.i("ERROR AL CONECTAR CON EL SERVIDOR: " + e.getMessage());
		}
	}

	public static void disconnect() {
		if (cThread != null) {
			cThread.close();
			Log.i("CONEXION CERRADA");
		}
	}

	public static void sendControl(Command c) {
		if (cThread != null) {

			
			try {
				if (c.noParms() == 2) {
					Log.i("ENVIANDO MENSAJE DE CONTROL [SEQ: d(0), STA: d(1), BOX: " + c.asS(0) + ", DOCK: " + c.asI(1) + "]");
					cThread.sendControlMessage(c.asS(0), c.asI(1));
				} else {
					Log.i("ENVIANDO MENSAJE DE CONTROL [SEQ: " + c.asI(0) + ", STA: " + c.asI(1) + ", BOX: " + c.asS(2) + ", DOCK: " + c.asI(3) + "]");
					cThread.sendControlMessage(c.asI(0), c.asI(1), c.asS(2), c.asI(3));
				}
			} catch (IOException e) {
				Log.i("ERROR AL CONECTAR CON EL SERVIDOR: " + e.getMessage());
			}

		} else {
			Log.i("NO CONECTADO");
		}

	}

}
