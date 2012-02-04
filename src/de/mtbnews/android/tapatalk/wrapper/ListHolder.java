package de.mtbnews.android.tapatalk.wrapper;

import java.util.List;

public class ListHolder<T>
{
	private List<T> children;
	public int totalCount;

	public int from;
	public int to;

	/**
	 * @param children
	 * @param totalCount
	 * @param from
	 * @param to
	 */
	public ListHolder(List<T> children, int totalCount, int from, int to)
	{
		super();
		this.children= children;
		this.totalCount = totalCount;
		this.from = from;
		this.to = to;
	}

	public List<T> getChildren()
	{
		return children;
	}

}
