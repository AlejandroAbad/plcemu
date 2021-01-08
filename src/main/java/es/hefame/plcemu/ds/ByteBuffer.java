package es.hefame.plcemu.ds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ByteBuffer {

	private List<Byte> buffer;

	public ByteBuffer(int size) {
		buffer = new ArrayList<>(size);
	}

	public void write(byte b) {
		buffer.add(b);

	}

	public void write(byte[] bytes) {
		for (byte b : bytes) {
			buffer.add(b);
		}
	}

	public byte[] getBytes() {

		byte[] buff = new byte[buffer.size()];

		Iterator<Byte> it = buffer.iterator();
		int index = 0;

		while (it.hasNext()) {
			buff[index++] = it.next().byteValue();
		}
		return buff;

	}

}
