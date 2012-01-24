package de.mtbnews.android.tapatalk.wrapper;

import java.util.Date;

public class Post implements ListEntry
{

	private String title;
	private String content;

	/**
	 * @param title
	 * @param content
	 */
	public Post(String title, String content)
	{
		super();
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
		return null;
	}

}
