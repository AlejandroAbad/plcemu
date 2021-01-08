package es.hefame.plcemu.ds;

import java.util.LinkedList;
import java.util.List;

import es.hefame.plcemu.util.RotatingQueue;

public class ControlQueue extends RotatingQueue<ControlMessage>
{

	private static ControlQueue	instance;

	@SuppressWarnings("unused")
	private static final long	serialVersionUID	= 2372557270687700140L;
	
	static {
		instance = new ControlQueue();
		add(new ControlMessage(444, 0, "CUBETA DE PRUEBA 1", 20));
		add(new ControlMessage(666, 0, "CUBETA DE PRUEBA 2", 30));
	}

	public ControlQueue()
	{
		super(20);
	}

	public static void add(ControlMessage ctrl)
	{
		if (instance == null) instance = new ControlQueue();
		instance.insertElement(ctrl);
	}

	public static List<ControlMessage> list()
	{
		if (instance == null) return new LinkedList<ControlMessage>();
		return instance.getElements();
	}

	public static ControlMessage get(int index)
	{
		if (instance == null) return null;
		return instance.getElement(index);
	}

	public static int length()
	{
		if (instance == null) return 0;
		return instance.size();
	}

}
