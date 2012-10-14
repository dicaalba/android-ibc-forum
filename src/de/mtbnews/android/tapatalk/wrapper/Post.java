package de.mtbnews.android.tapatalk.wrapper;

import java.util.Date;

public class Post implements ListEntry
{

	private Date time;
	private String title;
	private String content;
	private String name;

	public boolean unread;

	@Override
	public boolean isUnread()
	{
		return this.unread;
	}

	/**
	 * @param time
	 * @param title
	 * @param content
	 * @param name
	 */
	public Post(Date time, String title, String content, String name)
	{
		super();
		this.time = time;
		this.title = title;
		this.content = content;
		this.name = name;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @return the content
	 */
	public String getContent()
	{
		return content;
	}

	@Override
	public Date getDate()
	{
		return time;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

}
