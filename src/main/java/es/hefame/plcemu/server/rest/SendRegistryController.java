package es.hefame.plcemu.server.rest;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.http.exchange.IHttpRequest;
import es.hefame.plcemu.ds.ControlMessage;
import es.hefame.plcemu.ds.ControlQueue;
import es.hefame.plcemu.Server;
import es.hefame.plcemu.server.PlcServer;
import es.hefame.plcemu.util.Log;

public class SendRegistryController extends HttpController
{

	public SendRegistryController()
	{

	}

	@Override
	protected void get(HttpConnection exchange) throws HException, IOException
	{
		// super.get(exchange);

		
		IHttpRequest request = exchange.request;
		
		
		String function = request.getURIField(1);

		try {
		Log.i("Petición HTTP: " + request.getURI().toString());
		} catch (NullPointerException e) {
			Log.i(e.getMessage());
		}
		
		
		if (function == null)
		{
			queryControl(exchange);
		}
		else
		{
			switch (function)
			{
				case "confirma":
					confirm(exchange);
					break;
					
				case "pregunta":
					confirm(exchange, 99);
					break;
					
				case "rechaza":
					confirm(exchange, 17);
					break;
					
				case "ack":
					sendAck(exchange);
					break;
					
				case "disableAutoAck":
					PlcServer.setAutoAck(false);
					Log.i("Desactivado el AUTO ACK");
					redirectHomePage(exchange);
					break;
					
				case "enableAutoAck":
					PlcServer.setAutoAck(true);
					Log.i("Activado el AUTO ACK");
					redirectHomePage(exchange);
					break;
					
				case "disableNiceClose":
					PlcServer.setNiceClose(false);
					Log.i("Desactivado el NICE CLOSING");
					redirectHomePage(exchange);
					break;
					
				case "enableNiceClose":
					PlcServer.setNiceClose(true);
					Log.i("Activado el NICE CLOSING");
					redirectHomePage(exchange);
					break;
					
				case "clearLog":
					Log.reset();
					Log.i("Log reseteado");
					redirectHomePage(exchange);
					break;
					
				case "resetConnection":
					PlcServer.reset();
					Log.i(">>>>>  SE FUERZA EL REINICIO DEL SOCKET  <<<<<");
					redirectHomePage(exchange);
					break;
					
				default:
					queryControl(exchange);
					break;
			}
		}
	}

	private void redirectHomePage(HttpConnection exchange) throws IOException
	{
		exchange.response.setHeader("Location", "/r");
		exchange.response.send("<html><body><a href=\"/r\">Volver</a></body></html>", 302, "text/html");
		
		//queryControl(exchange);
	}
	
	private void queryControl(HttpConnection exchange) throws IOException
	{

		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Lista cubetas</title><body>");
		
		
		
		sb.append("<h3>Estado del cliente: ");
		
		
		if (PlcServer.client != null && PlcServer.client.isOnline()) {
			Socket s = PlcServer.client.s;
			sb.append("Conectado desde la IP ").append(s.getInetAddress().getHostAddress()).append(" puerto remoto ").append(s.getPort());
		} else {
			sb.append("No hay cliente conectado");
		}
		
		
		sb.append("</h3> - <a href='/r/resetConnection'>Reiniciar el socket del emulador (simula reinicio PLC)</a><br>");
		
		if (PlcServer.getAutoAck()) {
			sb.append("- Est&aacute <b>activado</b> el env&iacute;o autom&aacute;tico de ACKs. <a href='/r/disableAutoAck'>[Deshabilitar]</a>");
		} else {
			sb.append("- Est&aacute <b>desactivado</b> el env&iacute;o autom&aacute;tico de ACKs. <a href='/r/enableAutoAck'>[Habilitar]</a>");
		}
		
		sb.append("<br>- <a href='/r/clearLog'>Limpiar log del emulador</a> (tiene " + ((new File("/usr/local/plcemu/plcemu." + Server.getPort() + ".log")).length()  / 1024) + "KB)");
		
		sb.append("<br>- Cuando se reciban conexiones cuando ya hay una conexi&oacute;n activa, el emulador: ");
		if (PlcServer.getNiceClose()) {
			sb.append("<b>Realizar&aacute; un cierre ordenado del nuevo socket [Trama FIN].</b> <a href='/r/disableNiceClose'>[Cambiar a reseteo]</a>");
		} else {
			sb.append("<b>Resetear&aacute; la nueva conexi&oacute;n [Trama RST].</b> <a href='/r/enableNiceClose'>[Cambiar a cierre ordenado]</a>");
		}
		
		
		
		
		sb.append("<h3>Cubetas en el buffer</h3><table border='1' width='100%' cellpadding='2' cellspacing='0'><tr><th>ID</th><th>SECUENCIA</th><th>ESTACION</th><th>CUBETA</th><th>MUELLE</th><th></th></tr>");

		for (int i = 0; i < ControlQueue.length(); i++)
		{
			ControlMessage cm = ControlQueue.get(i);
			if (cm == null) continue;
			sb.append("<tr><td>");
			sb.append(i);
			sb.append("</td><td>");
			sb.append(cm.getSeqNumber());
			sb.append("</td><td>");
			sb.append(cm.getStationNumber());
			sb.append("</td><td>");
			sb.append(new String(cm.getBoxNumber()));
			sb.append("</td><td>");
			sb.append(cm.getDockNumber());
			sb.append("</td><td>");
			sb.append("<a href='/r/confirma/" + i + "'>Confirma</a> - ");
			sb.append("<a href='/r/rechaza/" + i + "'>Rechaza</a> - ");
			sb.append("<a href='/r/pregunta/" + i + "'>Pregunta</a> - ");
			sb.append("<a href='/r/ack/" + cm.getSeqNumber() + "'>ACK</a>");
			sb.append("</td></tr>");
		}

		sb.append("</table></body></html>");

		exchange.response.send(sb.toString(), 200, "text/html");

	}

	private void confirm(HttpConnection exchange) throws IOException
	{
		this.confirm(exchange, -1);
	}

	private void confirm(HttpConnection exchange, int dock) throws IOException
	{

		StringBuilder sb = new StringBuilder();
		sb.append("<pre>");
		String txtid = exchange.request.getURIField(2);
		int id;
		if (txtid != null)
		{
			try
			{
				id = Integer.parseInt(txtid);

				ControlMessage cm = ControlQueue.get(id);
				if (cm != null)
				{
					if ((PlcServer.client != null && PlcServer.client.isOnline()))
					{
						if (dock == -1) dock = cm.getDockNumber();
						PlcServer.client.sendRegisterMessage(cm, dock);
						sb.append("Se notifica cubeta &lt;" + new String(cm.getBoxNumber()) + "&gt; por el muelle &lt;" + dock + "&gt;" + "<br><a href='/r'>Patr&aacute;s</a></pre>");
						exchange.response.send(sb.toString(), 200, "text/html");
						return;
					}
					else
					{
						sb.append("No hay cliente conectado para mandarle nada" + "<br><a href='/r'>Patr&aacute;s</a></pre>");
						exchange.response.send(sb.toString(), 200, "text/html");
						return;
					}
				}
				else
				{
					sb.append("El ID de mensaje " + id + " no se encuentra" + "<br><a href='/r'>Patr&aacute;s</a></pre>");
					exchange.response.send(sb.toString(), 200, "text/html");
					return;
				}

			}
			catch (NumberFormatException e)
			{
				sb.append("ID de mensaje debe ser numerico: " + e.getMessage() + "<br><a href='/r'>Patr&aacute;s</a></pre>");
				exchange.response.send(sb.toString(), 200, "text/html");
				return;
			}

		}
		else
		{
			sb.append("No se especifica ID de mensaje" + "<br><a href='/r'>Patr&aacute;s</a></pre>");
			exchange.response.send(sb.toString(), 200, "text/html");
			return;

		}
	}
	
	private void sendAck(HttpConnection exchange) throws IOException
	{

		StringBuilder sb = new StringBuilder();
		sb.append("<pre>");
		String seq = exchange.request.getURIField(2);
		int id;
		if (seq != null)
		{
			try
			{
				id = Integer.parseInt(seq);

				if ((PlcServer.client != null && PlcServer.client.isOnline()))
				{
					PlcServer.client.sendAck(id);
					sb.append("Se manda ACK con secuencia &lt;" + id + "&gt;<br><a href='/r'>Patr&aacute;s</a></pre>");
					exchange.response.send(sb.toString(), 200, "text/html");
					return;
				}
				else
				{
					sb.append("No hay cliente conectado para mandarle nada" + "<br><a href='/r'>Patr&aacute;s</a></pre>");
					exchange.response.send(sb.toString(), 200, "text/html");
					return;
				}
				

			}
			catch (NumberFormatException e)
			{
				sb.append("El número de secuencia debe ser numerico: " + e.getMessage() + "<br><a href='/r'>Patr&aacute;s</a></pre>");
				exchange.response.send(sb.toString(), 200, "text/html");
				return;
			}

		}
		else
		{
			sb.append("No se especifica la secuencia del ACK" + "<br><a href='/r'>Patr&aacute;s</a></pre>");
			exchange.response.send(sb.toString(), 200, "text/html");
			return;

		}
	}

}
