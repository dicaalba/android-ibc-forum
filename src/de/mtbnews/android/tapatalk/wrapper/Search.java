package de.mtbnews.android.tapatalk.wrapper;

import java.util.List;

public class Search
{

	public int topicCount;
	public String searchId;

	private List<Topic> topics;

	/**
	 * @param topicCount
	 * @param searchId
	 * @param topics
	 */
	public Search(int topicCount, String searchId, List<Topic> topics)
	{
		super();
		this.topicCount = topicCount;
		this.searchId = searchId;
		this.topics = topics;
	}

	/**
	 * @return the topics
	 */
	public List<Topic> getTopics()
	{
		return topics;
	}

}
