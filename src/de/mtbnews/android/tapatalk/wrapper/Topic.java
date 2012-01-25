package de.mtbnews.android.tapatalk.wrapper;

import java.util.Date;
import java.util.List;

public class Topic implements ListEntry
{

	private String id;

	private List<Post> posts;

	private String title;
	private Date date;
	private String content;
	private int postCount;

	/**
	 * @param id
	 * @param posts
	 * @param title
	 * @param date
	 * @param content
	 * @param postCount
	 */
	public Topic(String id, List<Post> posts, String title, Date date,
			String content, int postCount)
	{
		super();
		this.id = id;
		this.posts = posts;
		this.title = title;
		this.date = date;
		this.content = content;
		this.postCount = postCount;
	}

	/**
	 * @return the postCount
	 */
	public int getPostCount()
	{
		return postCount;
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
	 * @return the posts
	 */
	public List<Post> getPosts()
	{
		return posts;
	}

}
