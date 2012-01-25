package de.mtbnews.android.tapatalk.wrapper;

import java.util.Date;

public class Post implements ListEntry
{

	private Date time;
	private String title;
	private String content;

	/**
	 * @param title
	 * @param content
	 */
	public Post(Date time, String title, String content)
	{
		super();
		this.time = time;
		this.title = title;
		this.content = content;
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

}
