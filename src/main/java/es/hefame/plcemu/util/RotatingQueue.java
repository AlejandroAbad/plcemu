package es.hefame.plcemu.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Rotating queue of fixed size.
 */
public class RotatingQueue<T>
{
	private List<T>	queue;
	private int		cursor;
	private int		size;

	public RotatingQueue(int capacity)
	{
		size = capacity;
		queue = new ArrayList<T>(capacity);
		cursor = capacity - 1;
	}

	/**
	 * Inserts an element to the head of the queue, pushing all other elements
	 * one position forward.
	 *
	 * @param element
	 */
	public synchronized void insertElement(T element)
	{
		advancePointer();

		if (queue.size() == cursor)
		{
			queue.add(element);
		}
		else
		{
			queue.set(cursor, element);
		}
	}

	public synchronized T getElement(int index)
	{
		// Normalize index to size of queue
		index = index % size;

		// Translate wanted index to queue index
		int queueIndex = cursor - index;

		// If negative, add size
		if (queueIndex < 0)
		{
			queueIndex += size;
		}

		// Check if element already exists in queue
		if (queueIndex < queue.size())
		{
			return queue.get(queueIndex);
		}
		else
		{
			return null;
		}
	}

	public int size()
	{
		return size;
	}

	public synchronized List<T> getElements()
	{
		List<T> list = new LinkedList<T>();

		Iterator<T> it = queue.iterator();
		while (it.hasNext())
		{
			list.add(it.next());
		}

		return list;
	}

	private void advancePointer()
	{
		cursor++;
		cursor = cursor % size;
	}

}
