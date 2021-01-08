package es.hefame.plcemu.ds;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import es.hefame.hcore.converter.ByteArrayConverter;

public class AckMessage extends Message {

	/**
	 * Crea el mensaje con el tipo y número de secuencia.
	 * 
	 * @param type      El tipo del mensaje.
	 * @param seqNumber El número de secuencia.
	 */
	public AckMessage(int seqNumber) {
		super(MessageType.A, seqNumber);
	}

	/**
	 * Instancia el objeto mensaje a partir de la trama KNAPP.
	 * 
	 * @param raw la trama KNAPP.
	 * @throws Exception Si la trama KNAPP no es correcta
	 */
	public AckMessage(byte[] raw) throws ParseException {
		super(raw);
	}

	/**
	 * Genera el array de bytes correspondiente al mensaje ACK.
	 * 
	 * @return El mensaje ACK codificado en formato para el PLC.
	 * @throws IOException Si algo falla.
	 */
	public byte[] encode() throws IOException {
		ByteBuffer buffer = new ByteBuffer(7);
		buffer.write(STX);
		buffer.write(this.type.code);
		buffer.write(Message.toPaddedByteLeft(String.valueOf(seqNumber), (byte) '0', 4));
		buffer.write(CR);
		byte[] rt = Message.toPaddedByteRight(new String(buffer.getBytes()), (byte) 0x20, 110);
		return rt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.Message#decode(byte[])
	 */
	@Override
	protected void decode(byte[] raw) throws ParseException {
		/*
		 * STX TYPE SEQ CR 0 1 2-5 6
		 */
		if (raw.length < 7) {
			throw new ParseException(String.format("El tamaño de la trama es %s, se esperaba %s", raw.length, 7), 0);
		}
		if (raw[0] != Message.STX) {
			throw new ParseException(String.format("El carácter inicial de la trama no es correcto. Se recibió %s",
					ByteArrayConverter.toHexString(new byte[] { raw[0] }, true)), 0);
		}
		if (raw[6] != Message.CR) {
			throw new ParseException(String.format("El carácter final de la trama no es correcto. Se recibió %s",
					ByteArrayConverter.toHexString(new byte[] { raw[6] }, true)), 6);
		}

		this.type = Message.MessageType.forName(raw[1]);
		if (type == null) {
			throw new ParseException(String.format("El tipo de mensaje no es válido. Se recibió %s",
					ByteArrayConverter.toHexString(new byte[] { raw[1] }, true)), 1);
		}

		String seqN = new String(Arrays.copyOfRange(raw, 2, 6));
		try {
			this.seqNumber = Integer.valueOf(seqN);
		} catch (@SuppressWarnings("unused") NumberFormatException nfe) {
			throw new ParseException(
					String.format("El tipo de mensaje no es válido. Se recibió %s", Arrays.copyOfRange(raw, 2, 5)), 1);
		}

	}

	@Override
	public String toCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append("ACK;").append(this.seqNumber);
		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("ACKNOWLEDGE");
		sb.append("\t[SECUENCIA: (" + this.seqNumber + ") ]");

		return sb.toString();
	}

}
