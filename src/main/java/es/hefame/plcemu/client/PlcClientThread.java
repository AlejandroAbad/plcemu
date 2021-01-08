package es.hefame.plcemu.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import es.hefame.plcemu.ds.AckMessage;
import es.hefame.plcemu.ds.ControlMessage;
import es.hefame.plcemu.ds.RegisterMessage;
import es.hefame.plcemu.ds.Message.MessageType;
import es.hefame.plcemu.server.PlcServer;
import es.hefame.plcemu.util.Log;
import es.hefame.plcemu.util.PrettyHex;
import es.hefame.plcemu.util.PrettyHex.Dir;

public class PlcClientThread extends Thread {
	public Socket s;
	protected InputStream is;
	protected OutputStream os;
	protected boolean maintainConnected;
	protected String host;
	protected int port;

	public PlcClientThread(String host, int port, boolean maintainConnected) throws IOException {
		this.maintainConnected = maintainConnected;
		this.host = host;
		this.port = port;


		this.start();
	}

	public boolean isOnline() {
		return (s != null && this.s.isConnected() && this.s.isBound()
				&& !this.s.isClosed());
	}

	public void close() {
		this.close(false);
	}

	public void close(boolean reset) {

		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
			}
		}
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
			}

		}

	}

	@Override
	public void run() {

		boolean first = true;
		
		while (this.maintainConnected || first) {
			
			if (!first) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {}
			}
			
			first = false;
			try {
				this.s = new Socket(host, port);
				// this.s.setSoTimeout(12000);
				
				if (this.maintainConnected) {
					SendControlThread sendControlThread = new SendControlThread(this, 10000);
					sendControlThread.start();
				}
				
				if (this.isAlive()) {
					Log.i("CONECTADO");
				} else {
					Log.i("NO CONECTADO");
				}

				is = this.s.getInputStream();
				os = this.s.getOutputStream();

				byte[] buffer = new byte[110];

				int readBytes = 0;
				readBytes = is.read(buffer, readBytes, 110);

				while (readBytes > 0) {

					PrettyHex.bump("Leidos " + readBytes + " ...");

					if (readBytes >= 110) {

						PrettyHex.bump("Leida trama completa. La analizamos");

						readBytes = 0;
						if (buffer[1] == MessageType.A.code) {
							AckMessage ackMessage = new AckMessage(buffer);
							PrettyHex.pretty(ackMessage, Dir.E);
						} else if (buffer[1] == MessageType.R.code) {
							RegisterMessage registerMessage = new RegisterMessage(buffer);
							PrettyHex.pretty(registerMessage, Dir.E);
							sendAck(registerMessage.getSeqNumber());
						} else {
							PrettyHex.bump("NO ENTIENDO LO QUE VIENE EN EL MENSAJE:");
							PrettyHex.pretty(buffer);
						}
					}

					readBytes += is.read(buffer, readBytes, 110 - readBytes);
				}

			} catch (Exception e) {
				this.close();
				PrettyHex.bump("SE CIERRA LA CONEXION POR UN ERROR EN EL CANAL: " + e.getMessage());
			} finally {
				this.close();
				PrettyHex.bump("EL SERVIDOR CIERRA LA CONEXION");
			}
		}
		
		Log.i("Se acabó el hilo " + this.maintainConnected);

	}

	public void sendControlMessage(int seqN, int station, String boxN, int dockN) throws IOException {
		ControlMessage controlMessage = new ControlMessage(seqN, station, boxN, dockN);
		PrettyHex.pretty(controlMessage, Dir.S);
		if (s != null && !s.isClosed()) {

			try {
				s.getOutputStream().write(controlMessage.encode());
			} catch (Exception e) {
				PrettyHex.bump("ERROR AL ENVIAR EL MENSAJE");
				e.printStackTrace(System.out);
			}

		} else {
			PrettyHex.bump("NO SE ENVIA EL MENSAJE PORQUE NO ESTA CONECTADO");
		}
	}

	public void sendControlMessage(String boxN, int dockN) throws IOException {
		this.sendControlMessage(0, 1, boxN, dockN);
	}

	public void sendAck(int seq) {
		AckMessage ackMessage = new AckMessage(seq);
		PrettyHex.pretty(ackMessage, Dir.S);

		if (s != null && !s.isClosed()) {

			try {
				s.getOutputStream().write(ackMessage.encode());
			} catch (Exception e) {
				PrettyHex.bump("NO SE ENVIA EL MENSAJE PORQUE NO ESTA CONECTADO");
			}

		} else {
			PrettyHex.bump("NO SE ENVIA EL MENSAJE PORQUE NO ESTA CONECTADO");
		}

	}

}
