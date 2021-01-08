package es.hefame.plcemu.server;

import java.net.ServerSocket;
import java.net.Socket;

import es.hefame.plcemu.util.PrettyHex;

public class PlcServer extends Thread
{

	private ServerSocket			ss;
	private int						port;

	public static PlcServerThread	client;

	private static boolean autoAck = true;
	private static boolean niceClose = true;
	
	public static void setAutoAck(boolean flag) {
		PlcServer.autoAck = flag;
	}
	
	public static boolean getAutoAck() {
		return PlcServer.autoAck;
	}
	
	public static void setNiceClose(boolean flag) {
		PlcServer.niceClose = flag;
	}
	
	public static boolean getNiceClose() {
		return PlcServer.niceClose;
	}
	
	
	public static void reset() {
		client.close(true);
		client = null;
	}
	

	public PlcServer(int port)
	{
		this.port = port;
	}

	public void run()
	{
		try
		{
			ss = new ServerSocket(port, 10);
			
			PrettyHex.bump("SERVIDOR ARRANCADO Y A LA ESCUCHA EN EL PUERTO " + port);
			
			while (true)
			{
				Socket s = ss.accept();
				
				

				if (client != null && client.isOnline()) {
					PrettyHex.bump("SE RECIBE UNA PETICIÓN DESDE EL PUERTO REMOTO [" + s.getPort() + "] CUANDO EL SOCKET YA ESTÁ ABIERTO. SE RECHAZA.");
					if (!niceClose) s.setSoLinger(true, 0);
					s.close();
				}
				else {
					PrettyHex.bump("SE ESTABLECE LA CONEXION DESDE " + s.getInetAddress().getHostAddress() + " PUERTO REMOTO " + s.getPort());
					client = new PlcServerThread(s);
					client.start();
				}
				
			}
		}
		catch (Exception e)
		{
			PrettyHex.bump("ERROR AL INICIAR EL SOCKET DE SERVIDOR");
			e.printStackTrace();
		}

	}

}
