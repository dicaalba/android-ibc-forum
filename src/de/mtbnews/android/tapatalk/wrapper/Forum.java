package de.mtbnews.android.tapatalk.wrapper;

import java.util.Date;
import java.util.List;

public class Forum implements ListEntry
{

	private String id;

	private List<Topic> topics;
	public int topicCount;

	private String title;
	private Date date;
	private String content;

	public List<Forum> subForen;
	public boolean subOnly;
	public boolean unread;
	public String url;
	
	@Override
	public boolean isUnread()
	{
		return this.unread;
	}

	/**
	 * @param id
	 * @param topics
	 * @param title
	 * @param date
	 * @param content
	 * @param postCount
	 */
	public Forum(String id, List<Topic> topics, String title, Date date,
			String content)
	{
		super();
		this.id = id;
		this.topics = topics;
		this.title = title;
		this.date = date;
		this.content = content;
	}

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @return the date
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * @return the content
	 */
	public String getContent()
	{
		return content;
	}

	/**
	 * @return the topics
	 */
	public List<Topic> getTopics()
	{
		return topics;
	}

	/**
	 * Forum hat keinen Namen
	 * 
	 * @see de.mtbnews.android.tapatalk.wrapper.ListEntry#getName()
	 */
	@Override
	public String getName()
	{
		return null;
	}

}
