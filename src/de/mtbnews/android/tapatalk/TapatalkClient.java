package de.mtbnews.android.tapatalk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.util.Log;

import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.tapatalk.wrapper.Post;
import de.mtbnews.android.tapatalk.wrapper.Search;
import de.mtbnews.android.tapatalk.wrapper.Topic;

/**
 * Tapatalk-kompatibler Client.
 * 
 * @author dankert
 * 
 */
public class TapatalkClient
{
	private XMLRPCClient client;
	public boolean loggedIn;

	public TapatalkClient(String connectorUrl)
	{
		this.client = new XMLRPCClient(connectorUrl);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> login(String username, String password)
			throws TapatalkException
	{
		try
		{

			final Map<String, Object> map = (Map<String, Object>) toMap(getXMLRPCClient()
					.call("get_config"));
			Log.i("IBC Server Config", map.toString());
			for (String key : map.keySet())
				Log.d("IBC Server Config", key + "=" + map.get(key));
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Load Config failed", e);
		}

		final Object[] params = new Object[] { username.getBytes(),
				password.getBytes() };

		try
		{

			final Map<String, Object> map = (Map<String, Object>) toMap(getXMLRPCClient()
					.callEx("login", params));
			this.loggedIn = true;
			return map;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Login failed", e);
		}

	}

	public void logout() throws TapatalkException
	{

		try
		{
			@SuppressWarnings("unused")
			Object result = getXMLRPCClient().call("logout_user");
			this.loggedIn = false;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Logout failed", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Topic getTopic(String topicId, int start, int end)
			throws TapatalkException
	{
		try
		{
			Object[] params = new Object[] { topicId, start, end };

			Map map = toMap(client.callEx("get_thread", params));

			toMap(map);

			String title = byteArrayToString(map.get("topic_title"));
			String id = (String) map.get("topic_id");
			int postCount = toInt(map.get("total_post_num"));

			List<Post> posts = new ArrayList<Post>();
			Topic topic = new Topic(id, posts, title, null, null, postCount);

			for (Object o1 : (Object[]) map.get("posts"))
			{
				Map postMap = (Map) o1;
				Post post = new Post(new String((byte[]) postMap
						.get("post_title")), new String((byte[]) postMap
						.get("post_content")));
				posts.add(post);
			}

			return topic;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load Topic " + topicId, e);
		}
	}

	@SuppressWarnings("unchecked")
	public Forum getForum(String forumId) throws TapatalkException
	{
		try
		{
			Object o = client.call("get_topic", forumId);

			Map map = (Map) o;
			String title = new String((byte[]) map.get("forum_name"));
			String id = (String) map.get("forum_id");

			final List<Topic> topics = new ArrayList<Topic>();
			final List<Post> posts = new ArrayList<Post>();

			Forum forum = new Forum(id, topics, title, null, null);
			for (Object o1 : (Object[]) map.get("topics"))
			{
				Map topicMap = (Map) o1;
				Topic topic = new Topic((String) topicMap.get("topic_id"),
						posts, //
						byteArrayToString(topicMap.get("topic_title")),//
						(Date) topicMap.get("last_reply_time"), //
						new String((byte[]) topicMap.get("short_content")),//
						0);
				topics.add(topic);
			}

			return forum;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load Forum " + forumId, e);
		}
	}

	public Search searchTopics(String query, int start, int end, String searchId)
			throws TapatalkException
	{
		try
		{
			final Object[] params;

			if (searchId == null)
				params = new Object[] { query.getBytes(), start, end };
			else
				params = new Object[] { "".getBytes(), start, end, searchId };

			Map map = toMap(client.callEx("search_topic", params));

			Integer topicCount = (Integer) map.get("total_topic_num");
			String newSearchId = (String) map.get("search_id");
			final List<Topic> topics = new ArrayList<Topic>();
			Search search = new Search(topicCount, newSearchId, topics);

			final List<Post> posts = new ArrayList<Post>();

			for (Object o1 : (Object[]) map.get("topics"))
			{
				Map topicMap = toMap(o1);
				Topic topic = new Topic((String) topicMap.get("topic_id"),
						posts, //
						byteArrayToString(topicMap.get("topic_title")),//
						(Date) topicMap.get("post_time"), //
						new String((byte[]) topicMap.get("short_content")),//
						0);
				topics.add(topic);
			}

			return search;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not search", e);
		}
	}

	public Search getBoxContent(String boxId, int start, int end)
			throws TapatalkException
	{
		try
		{
			final Object[] params = new Object[] { boxId, start, end };

			Map map = toMap(client.callEx("get_box", params));

			Integer topicCount = (Integer) map.get("total_topic_num");
			String newSearchId = (String) map.get("search_id");
			final List<Topic> topics = new ArrayList<Topic>();
			Search search = new Search(topicCount, newSearchId, topics);

			final List<Post> posts = new ArrayList<Post>();

			for (Object o1 : (Object[]) map.get("topics"))
			{
				Map topicMap = toMap(o1);
				Topic topic = new Topic((String) topicMap.get("topic_id"),
						posts, //
						byteArrayToString(topicMap.get("topic_title")),//
						(Date) topicMap.get("post_time"), //
						new String((byte[]) topicMap.get("short_content")),//
						0);
				topics.add(topic);
			}

			return search;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not search", e);
		}
	}

	public Search getMessage(String query, int start, int end, String searchId)
			throws TapatalkException
	{
		try
		{
			final Object[] params;

			if (searchId == null)
				params = new Object[] { query.getBytes(), start, end };
			else
				params = new Object[] { "".getBytes(), start, end, searchId };

			Map map = toMap(client.callEx("search_topic", params));

			Integer topicCount = (Integer) map.get("total_topic_num");
			String newSearchId = (String) map.get("search_id");
			final List<Topic> topics = new ArrayList<Topic>();
			Search search = new Search(topicCount, newSearchId, topics);

			final List<Post> posts = new ArrayList<Post>();

			for (Object o1 : (Object[]) map.get("topics"))
			{
				Map topicMap = toMap(o1);
				Topic topic = new Topic((String) topicMap.get("topic_id"),
						posts, //
						byteArrayToString(topicMap.get("topic_title")),//
						(Date) topicMap.get("post_time"), //
						new String((byte[]) topicMap.get("short_content")),//
						0);
				topics.add(topic);
			}

			return search;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not search", e);
		}
	}

	public XMLRPCClient getXMLRPCClient()
	{
		return client;
	}

	private static String byteArrayToString(Object object)
	{
		byte[] byteArray = (byte[]) object;
		if (byteArray == null)
			return null;
		else
			return new String(byteArray);
	}

	private static int toInt(Object object)
	{
		Integer i = (Integer) object;
		if (i == null)
			return 0;
		else
			return i.intValue();
	}

	private Map toMap(Object o) throws TapatalkException
	{
		if (!(o instanceof Map))
		{
			throw new TapatalkException("no map: " + o.toString() + " ("
					+ o.getClass() + ")");
		}
		Map map = (Map) o;

		Object object = map.get("result");
		if (object == null)
			return map;

		boolean ok = (Boolean) object;
		if (!ok)
			throw new TapatalkException(byteArrayToString(map
					.get("result_text")));
		return map;
	}

}
