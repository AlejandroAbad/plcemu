package es.hefame.plcemu.ds;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import jhefame.core.C;

public class RegisterMessage extends Message
{

	private int		stationNumber;
	private byte[]	boxNumber;
	private int		dockNumber;
	private int		weight;

	/**
	 * Instancia el objeto mensaje a partir de la trama KNAPP.
	 * 
	 * @param raw la trama KNAPP.
	 * @throws Exception Si la trama KNAPP no es correcta
	 */
	public RegisterMessage(byte[] raw) throws ParseException
	{
		super(raw);
	}

	public RegisterMessage(int seqNumber, int stationNumber, String boxNumber, int controlData, int weight)
	{
		super(MessageType.R, seqNumber);
		this.stationNumber = stationNumber;
		this.boxNumber = Message.toPaddedByteRight(boxNumber, (byte) 0x20, 28);
		this.dockNumber = controlData;
		this.weight = weight;
	}

	/**
	 * @return the stationNumber
	 */
	public int getStationNumber()
	{
		return stationNumber;
	}

	/**
	 * @return the boxNumber
	 */
	public byte[] getBoxNumber()
	{
		return boxNumber;
	}

	/**
	 * @return the controlData
	 */
	public int getDockNumber()
	{
		return dockNumber;
	}

	/**
	 * Genera el array de bytes correspondiente al mensaje ACK.
	 * 
	 * @return El mensaje ACK codificado en formato para el PLC.
	 * @throws IOException Si algo falla.
	 */
	public byte[] encode() throws IOException
	{
		ByteArrayBuffer buffer = new ByteArrayBuffer(47);
		buffer.write(STX);
		buffer.write(this.type.code);
		buffer.write(Message.toPaddedByteLeft(String.valueOf(seqNumber), (byte) '0', 4));
		buffer.write(Message.toPaddedByteLeft(String.valueOf(stationNumber), (byte) '0', 4));
		buffer.write(this.boxNumber);
		buffer.write(Message.toPaddedByteLeft(String.valueOf(dockNumber), (byte) '0', 2));
		buffer.write(Message.toPaddedByteLeft(String.valueOf(weight), (byte) '0', 6));
		buffer.write(CR);
		byte[] rt = Message.toPaddedByteRight(new String(buffer.getRawData()), (byte) 0x20, 110);
		buffer.close();
		return rt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.Message#decode(byte[])
	 */
	@Override
	protected void decode(byte[] raw) throws ParseException
	{
		if (raw.length < 47) { throw new ParseException(String.format("El tamaño de la trama es %s, se esperaba %s", raw.length, 7), 0); }
		if (raw[0] != Message.STX) { throw new ParseException(String.format("El carácter inicial de la trama no es correcto. Se recibió %s", C.bytes.toHexString(new byte[] { raw[0] }, true)), 0); }
		if (raw[46] != Message.CR) { throw new ParseException(String.format("El carácter final de la trama no es correcto. Se recibió %s", C.bytes.toHexString(new byte[] { raw[6] }, true)), 6); }

		this.type = Message.MessageType.forName(raw[1]);
		if (type == null) { throw new ParseException(String.format("El tipo de mensaje no es válido. Se recibió %s", C.bytes.toHexString(new byte[] { raw[1] }, true)), 1); }

		String seqN = new String(Arrays.copyOfRange(raw, 2, 6));
		try
		{
			this.seqNumber = Integer.valueOf(seqN);
		}
		catch (@SuppressWarnings("unused") NumberFormatException nfe)
		{
			throw new ParseException(String.format("El numero de secuencia no es válido. Se recibió %s", Arrays.copyOfRange(raw, 2, 5)), 2);
		}

		String stNumber = new String(Arrays.copyOfRange(raw, 6, 10));
		try
		{
			this.stationNumber = Integer.valueOf(stNumber);
		}
		catch (@SuppressWarnings("unused") NumberFormatException nfe)
		{
			throw new ParseException(String.format("El número de estación no es válido. Se recibió %s", Arrays.copyOfRange(raw, 6, 10)), 6);
		}

		this.boxNumber = Arrays.copyOfRange(raw, 10, 38);

		String dockNumber = new String(Arrays.copyOfRange(raw, 38, 40));
		try
		{
			this.dockNumber = Integer.valueOf(dockNumber);
		}
		catch (@SuppressWarnings("unused") NumberFormatException nfe)
		{
			throw new ParseException(String.format("El número de muelle no es válido. Se recibió %s", Arrays.copyOfRange(raw, 38, 40)), 38);
		}

		String weight = new String(Arrays.copyOfRange(raw, 40, 46));
		try
		{
			this.weight = Integer.valueOf(weight);
		}
		catch (@SuppressWarnings("unused") NumberFormatException nfe)
		{
			throw new ParseException(String.format("El valor del peso no es válido. Se recibió %s", Arrays.copyOfRange(raw, 38, 40)), 38);
		}	
	}
	
	@Override
	public String toCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append("REGISTER;").append(this.seqNumber).append(";").append(this.stationNumber).append(";").append(new String(this.boxNumber)).append(";").append(this.dockNumber).append(";").append(this.weight);
		return sb.toString();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("MENSAJE DE REGISTRO");
		sb.append(" \tSECUENCIA: (" + this.seqNumber + "),");
		sb.append(" \tESTACION: (" + this.stationNumber + "),");
		sb.append(" \tCUBETA: (" + new String(boxNumber) + "),");
		sb.append(" \tMUELLE: (" + this.dockNumber + "),");
		sb.append(" \tPESO: (" + this.weight + ") ]");

		return sb.toString();
	}

}
