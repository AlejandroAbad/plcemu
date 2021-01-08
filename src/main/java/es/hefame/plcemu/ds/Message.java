package es.hefame.plcemu.ds;

import java.io.IOException;
import java.text.ParseException;


/**
 * Tipo genérico de mensaje intercambiado con el PLC.
 * De esta clase heredan las implementaciones concretas de los mensajes.
 * 
 * @author Alejandro_AC
 *
 */
public abstract class Message
{

	/**
	 * Indica el tipo del mensaje.
	 * <ul>
	 * <li>A = Mensaje de ACK</li>
	 * <li>C = Mensaje de Control</li>
	 * <li>R = Mensaje de Registro</li>
	 * </ul>
	 * 
	 * @author Alejandro_AC
	 *
	 */
	public enum MessageType
	{
		/**
		 * Tipo de mensaje ACK.
		 */
		A((byte) 'A'),

		/**
		 * Tipo de mensaje de Control.
		 */
		C((byte) 'C'),

		/**
		 * Tipo de mensaje de Registro.
		 */
		R((byte) 'R');

		/**
		 * Código del tipo del mensaje utilizado al codificar el mismo.
		 */
		public final byte code;

		private MessageType(byte code)
		{
			this.code = code;
		}

		/**
		 * Obtiene el tipo de mensaje en función del código del mismo.
		 * 
		 * @param code El código del tipo de mensaje que buscamos.
		 * @return El tipo de mensaje correspondiente al código de entrada o null si el código no corresponde a ningún tipo de mensaje.
		 */
		public static MessageType forName(byte code)
		{
			for (MessageType mType : MessageType.values())
			{
				if (mType.code == code) return mType;
			}
			return null;
		}
	}

	/**
	 * Caracter de inicio de trama.
	 */
	public final static byte	STX	= 0x02;

	/**
	 * Caracter de fin de trama.
	 */
	public final static byte	CR	= 0x0d;

	/**
	 * Identificador de registro.
	 */
	protected MessageType		type;

	/**
	 * Número de secuencia.
	 */
	protected int				seqNumber;

	/**
	 * Crea el mensaje con el tipo y número de secuencia.
	 * 
	 * @param type El tipo del mensaje.
	 * @param seqNumber El número de secuencia.
	 */
	protected Message(MessageType type, int seqNumber)
	{
		super();
		this.type = type;
		this.seqNumber = seqNumber;
	}

	/**
	 * Instancia el objeto mensaje a partir de la trama KNAPP.
	 * 
	 * @param raw la trama KNAPP.
	 * @throws Exception Si la trama KNAPP no es correcta
	 */
	protected Message(byte[] raw) throws ParseException
	{
		super();
		this.decode(raw);
	}

	/**
	 * Devuelve el tipo del mensaje.
	 * 
	 * @return the type
	 */
	public MessageType getType()
	{
		return type;
	}

	/**
	 * Devuelve el número de secuencia del mensaje.
	 * 
	 * @return El número de secuencia del mensaje.
	 */
	public int getSeqNumber()
	{
		return seqNumber;
	}

	/**
	 * Las subclases de Message deben implementar este método para determinar como se convierte el tipo específico de mensaje a una trama KNAPP.
	 * 
	 * @return El mensaje codificado en formato para el PLC.
	 * @throws IOException Si algo falla.
	 */
	public abstract byte[] encode() throws IOException;

	/**
	 * Las subclases de Message deben implementar este método para determinar como se convierte una trama KNAPP a el tipo específico de mensaje.
	 * El método debe modificar los campos internos de la clase. Este método se llama en el constructor <i>Message(byte[])</i> de la clase.
	 * 
	 * @param raw La trama KNAPP a decodificar.
	 * @throws IOException Si la trama KANPP no es correcta.
	 */
	protected abstract void decode(byte[] raw) throws ParseException;

	
	public abstract String toCSV();
	
	/**
	 * Convierte una cadena a un array de bytes con el tamaño especificado.
	 * Los bytes de relleno se añaden por la izquierda y se establecen al valor de <i>paddingValue</i>.
	 * 
	 * @param input La cadena de entrada a la que queremos añadir padding.
	 * @param paddingValue El valor de relleno
	 * @param length El tamaño en bytes del resultado
	 * @return Un array de bytes de tamaño <i>length</i>, con los bytes de la cadena <i>input</i> a la derecha y relleno por la izquierda con <i>paddingValue</i> hasta el tamaño total.
	 */
	public static byte[] toPaddedByteLeft(String input, byte paddingValue, int length)
	{

		byte[] output = new byte[length];
		int paddingSize = length - input.length();

		int i;
		for (i = 0; i < paddingSize; i++)
		{
			output[i] = paddingValue;
		}

		for (byte b : input.getBytes())
		{
			output[i++] = b;
		}

		return output;
	}

	/**
	 * Convierte una cadena a un array de bytes con el tamaño especificado.
	 * Los bytes de relleno se añaden por la derecha y se establecen al valor de <i>paddingValue</i>.
	 * 
	 * @param input La cadena de entrada a la que queremos añadir padding.
	 * @param paddingValue El valor de relleno
	 * @param length El tamaño en bytes del resultado
	 * @return Un array de bytes de tamaño <i>length</i>, con los bytes de la cadena <i>input</i> a la derecha y relleno por la izquierda con <i>paddingValue</i> hasta el tamaño total.
	 */
	public static byte[] toPaddedByteRight(String input, byte paddingValue, int length)
	{
		byte[] output = new byte[length];

		for (int i = 0; i < length; i++)
		{
			output[i] = paddingValue;
		}

		int i = 0;
		for (byte b : input.getBytes())
		{
			output[i++] = b;
		}

		return output;
	}

}
