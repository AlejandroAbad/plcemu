package es.hefame.plcemu.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import es.hefame.plcemu.ds.AckMessage;
import es.hefame.plcemu.ds.ControlMessage;
import es.hefame.plcemu.ds.ControlQueue;
import es.hefame.plcemu.ds.Message.MessageType;
import es.hefame.plcemu.util.PrettyHex;
import es.hefame.plcemu.util.PrettyHex.Dir;
import es.hefame.plcemu.ds.RegisterMessage;

public class PlcServerThread extends Thread {
	public Socket s;
	protected InputStream is;
	protected OutputStream os;

	protected boolean running;

	public PlcServerThread(Socket s) {

		this.s = s;

		try {
			// 
			// this.s.setSoTimeout(60000);
			// this.s.setTcpNoDelay(true); // Esto hace que se manden paquetes menos a menudo, pero con tamaños mas grandes.
			this.s.setKeepAlive(true); // El tiempo entre keepalives lo define el SO y ahí debe configurarse. El PLC lo tiene a 30 segs.
			// this.s.setReceiveBufferSize(8192); // Tamaño de la ventana de transmisión TCP. PLC indica 8Kb.
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	public boolean isOnline() {
		return (s != null && PlcServer.client.s.isConnected() && PlcServer.client.s.isBound()
				&& !PlcServer.client.s.isClosed());
	}

	public void close() {
		this.close(false);
	}
	public void close(boolean reset) {
		running = false;
		try {
			if (reset) this.s.setSoLinger(true, 0);
		} catch (IOException e) {		}
		
		if (is != null) {try { is.close(); } catch (IOException e) {}}
		if (os != null) {try { os.close(); } catch (IOException e) {}}
		if (s != null) {try { s.close(); } catch (IOException e) {}}

	}

	@Override
	public void run() {
		try {
			running = true;
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
					} else if (buffer[1] == MessageType.C.code) {
						ControlMessage controlMessage = new ControlMessage(buffer);
						PrettyHex.pretty(controlMessage, Dir.E);
						ControlQueue.add(controlMessage);

						if (PlcServer.getAutoAck())
							sendAck(controlMessage.getSeqNumber());
						else
							PrettyHex.bump(">> No se envia ACK por estar deshabilitado");
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
			PrettyHex.bump("EL CLIENTE CIERRA LA CONEXION");
		}

	}

	public void sendRegisterMessage(int seqN, int station, String boxN, int dockN, int weight) throws IOException {
		RegisterMessage registerMessage = new RegisterMessage(seqN, station, boxN, dockN, weight);
		PrettyHex.pretty(registerMessage, Dir.S);

		if (s != null && !s.isClosed()) {

			try {
				s.getOutputStream().write(registerMessage.encode());
			} catch (Exception e) {
				PrettyHex.bump("NO SE ENVIA EL MENSAJE PORQUE NO HAY NINGUN CLIENTE CONECTADO");
			}

		} else {
			PrettyHex.bump("NO SE ENVIA EL MENSAJE PORQUE NO HAY NINGUN CLIENTE CONECTADO");
		}

	}

	public void sendRegisterMessage(ControlMessage controlMessage) throws IOException {
		this.sendRegisterMessage(controlMessage.getSeqNumber() + 1, controlMessage.getStationNumber(),
				new String(controlMessage.getBoxNumber()), controlMessage.getDockNumber(), 0);
	}

	public void sendRegisterMessage(ControlMessage controlMessage, int dock) throws IOException {
		this.sendRegisterMessage(controlMessage.getSeqNumber() + 1, controlMessage.getStationNumber(),
				new String(controlMessage.getBoxNumber()), dock, 0);
	}

	public void sendAck(int seq) {
		AckMessage ackMessage = new AckMessage(seq);
		PrettyHex.pretty(ackMessage, Dir.S);

		if (s != null && !s.isClosed()) {

			try {
				s.getOutputStream().write(ackMessage.encode());
			} catch (Exception e) {
				PrettyHex.bump("NO SE ENVIA EL MENSAJE PORQUE NO HAY NINGUN CLIENTE CONECTADO");
			}

		} else {
			PrettyHex.bump("NO SE ENVIA EL MENSAJE PORQUE NO HAY NINGUN CLIENTE CONECTADO");
		}

	}

}
