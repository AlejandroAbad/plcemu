package es.hefame.plcemu;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.server.HttpService;
import es.hefame.plcemu.server.PlcServer;
import es.hefame.plcemu.server.rest.LogViewerController;
import es.hefame.plcemu.server.rest.SendRegistryController;
import es.hefame.plcemu.util.Log;

public class Server
{
	
	private static int port = 0;
	
	public static int getPort() {
		return port;
	}

	public Server()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String... strings)
	{
		if ( strings.length < 0) {
			System.err.println("Debe pasar como parámetro el puerto donde escuchará el emulador");
			System.exit(1);
		}
		
		try {
			port = Integer.parseInt(strings[0]);
		}
		catch(NumberFormatException e)
		{
			System.err.println("No ha introducido un número de puerto válido");
			System.exit(2);
		}
		
		if (port < 1 || port > 65535) {
			System.err.println("El puerto debe estar entre 1 y 65535");
			System.exit(3);
		}
		
		
		// Log.init("C:\\TMP\\plcemu." + port + ".log");
		Log.init("./plcemu." + port + ".log");
		
		PlcServer s = new PlcServer(port);
		s.start();

		Map<String, HttpController> routes = new HashMap<String, HttpController>();
		routes.put("/r", new SendRegistryController());
		routes.put("/log", new LogViewerController());

		try
		{
			HttpService restService = new HttpService(port + 1, 5, routes);
			restService.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
