package es.hefame.plcemu.server.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.plcemu.Server;
import es.hefame.plcemu.util.Log;

public class LogViewerController extends HttpController {

	@Override
	public void get(HttpConnection exchange)  throws HException, IOException {
		
		try {
			
			byte[] encoded = Files.readAllBytes(Paths.get(Log.getPath()));

			StringBuilder sb = new StringBuilder();
			
			sb.append("<html><head><title>Lista cubetas</title><body><h1>Contenido del log</h1><pre>");
			sb.append(   (new String(encoded)).replace("<", "&lt;").replace(">", "&gt;")   );			
			sb.append("</pre></body></html>");

			exchange.response.send(sb.toString(), 200, "text/html");
			
		} catch (IOException e) {
			exchange.response.send("No se encuentra el fichero de log /usr/local/plcemu/plcemu." + Server.getPort() + ".log", 500, "text/html");
		}
		
	}
	
}
