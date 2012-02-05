package de.mtbnews.android.tapatalk.wrapper;

import java.util.Date;

public class Message implements ListEntry
{
	public String id;
	private Date date;
	public String from;
	public String[] to;
	public String subject;
	public String content;
	public boolean unread;

	
	@Override
	public boolean isUnread()
	{
		return this.unread;
	}

	/**
	 * @param id
	 * @param unread
	 * @param date
	 * @param from
	 * @param to
	 * @param subject
	 * @param content
	 */
	public Message(String id, boolean unread, Date date, String from, String[] to,
			String subject, String content)
	{
		super();
		this.id = id;
		this.unread = unread;
		this.date = date;
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.content = content;
	}

	@Override
	public String getContent()
	{
		return content;
	}

	@Override
	public Date getDate()
	{
		return date;
	}

	@Override
	public String getName()
	{
		return from;
	}

	@Override
	public String getTitle()
	{
		return subject;
	}
}
