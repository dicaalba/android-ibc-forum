package de.mtbnews.android.tapatalk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.text.TextUtils;
import de.mtbnews.android.tapatalk.TapatalkException.TapatalkErrorCode;
import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.tapatalk.wrapper.ListHolder;
import de.mtbnews.android.tapatalk.wrapper.Mailbox;
import de.mtbnews.android.tapatalk.wrapper.Message;
import de.mtbnews.android.tapatalk.wrapper.Post;
import de.mtbnews.android.tapatalk.wrapper.Search;
import de.mtbnews.android.tapatalk.wrapper.Topic;

/**
 * Tapatalk-compatible client.
 * 
 * @author Jan Dankert
 * 
 */
public class TapatalkClient
{
	private XMLRPCClient client;

	public boolean loggedIn;
	public long loginTime;

	/**
	 * @param connectorUrl
	 *            URL
	 */
	public TapatalkClient(String connectorUrl)
	{
		this.client = new XMLRPCClient(connectorUrl);
	}

	/**
	 * Benutzer-Login. Falls Login nicht klappt, wird eine
	 * {@link TapatalkException} geworfen.
	 * 
	 * @param username
	 * @param password
	 * @throws TapatalkException
	 */
	public void login(String username, String password)
			throws TapatalkException
	{
		this.loggedIn = false;
		this.loginTime = -1L;

		if (TextUtils.isEmpty(username))
			throw new TapatalkException("Username empty",
					TapatalkErrorCode.NO_USERNAME);

		final Object[] params = new Object[] { username.getBytes(),
				password.getBytes() };

		try
		{

			toMap(client.callEx("login", params));
			this.loggedIn = true;
			this.loginTime = System.currentTimeMillis();
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Login failed" + ": " + e.getMessage(),
					e, TapatalkErrorCode.XMLRPC_ERROR);
		}

	}

	public void logout() throws TapatalkException
	{

		try
		{
			@SuppressWarnings("unused")
			Object result = client.call("logout_user");
			this.loggedIn = false;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException(
					"Logout failed" + ": " + e.getMessage(), e,
					TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Load a topic.
	 * 
	 * @param topicId
	 *            topic-id
	 * @param start
	 *            start-position
	 * @param end
	 *            end-position
	 * @return list of topics
	 * @throws TapatalkException
	 */
	@SuppressWarnings("unchecked")
	public Topic getTopic(String topicId, int start, int end)
			throws TapatalkException
	{
		try
		{
			Object[] params = new Object[] { topicId, start, end };

			Map map = toMap(client.callEx("get_thread", params));

			String title = byteArrayToString(map.get("topic_title"));
			String id = (String) map.get("topic_id");
			int postCount = toInt(map.get("total_post_num"));

			List<Post> posts = new ArrayList<Post>();
			Topic topic = new Topic(id, posts, title, null, null, null,
					postCount);
			topic.forumId = (String) map.get("forum_id");

			for (Object o1 : (Object[]) map.get("posts"))
			{
				Map postMap = (Map) o1;
				Post post = new Post((Date) postMap.get("post_time"),
						byteArrayToString(postMap.get("post_title")),
						byteArrayToString(postMap.get("post_content")),
						byteArrayToString(postMap.get("post_author_name")));
				posts.add(post);
			}

			return topic;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load Topic " + topicId, e,
					TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	public List<Forum> getAllForum() throws TapatalkException
	{
		try
		{
			Object l = client.call("get_forum");
			Object[] arr = (Object[]) l;
			Map[] mapArr = castToMapArray(arr);

			List<Forum> forum = createSubForen(mapArr, "child");

			return forum;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load Forum structure", e,
					TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	private Map[] castToMapArray(Object[] arr)
	{
		Map[] mapArr = new Map[arr.length];
		int i = 0;
		for (Object object : arr)
		{
			mapArr[i] = (Map) arr[i++];
		}
		return mapArr;
	}

	public final static int TOPIC_STANDARD = 1;
	public final static int TOPIC_STICKY = 2;
	public final static int TOPIC_ANNOUNCEMENT = 3;

	@SuppressWarnings("unchecked")
	public Forum getForum(String forumId, int from, int to, int mode)
			throws TapatalkException
	{
		try
		{
			final String mode2;

			if (mode == TOPIC_ANNOUNCEMENT)
				mode2 = "ANN";
			else if (mode == TOPIC_STICKY)
				mode2 = "TOP";
			else
				mode2 = "";

			final Object[] params = new Object[] { forumId, from, to, mode2 };
			Map map = (Map) toMap(client.callEx("get_topic", params));

			final List<Topic> topics = new ArrayList<Topic>();
			final List<Post> posts = new ArrayList<Post>();

			String title = byteArrayToString(map.get("forum_name"));
			String id = (String) map.get("forum_id");

			final Forum forum = new Forum(id, topics, title, null, null);
			forum.topicCount = (Integer) map.get("total_topic_num");

			if (forum.topicCount > 0)
			{
				for (Object o1 : (Object[]) map.get("topics"))
				{
					Map topicMap = (Map) o1;
					Topic topic = new Topic(
							(String) topicMap.get("topic_id"),
							posts, //
							byteArrayToString(topicMap.get("topic_title")),//
							(Date) topicMap.get("last_reply_time"), //
							byteArrayToString(topicMap.get("short_content")),//
							byteArrayToString(topicMap.get("topic_author_name")),
							0);
					topic.unread = (Boolean) topicMap.get("new_post");
					topics.add(topic);
				}
			}

			return forum;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load Forum " + forumId
					+ ": " + e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	public ListHolder<Topic> getSubscribedTopics(int from, int to,
			boolean onlyUnread) throws TapatalkException
	{
		try
		{
			final Object[] params = new Object[] { from, to };
			Map map = toMap(client.callEx("get_subscribed_topic", params));

			int topicCount = (Integer) map.get("total_topic_num");

			final List<Topic> topics = new ArrayList<Topic>();

			for (Object o1 : (Object[]) map.get("topics"))
			{
				Map topicMap = (Map) o1;
				if (!onlyUnread || (Boolean) topicMap.get("new_post"))
				{

					Topic topic = new Topic(
							(String) topicMap.get("topic_id"),
							new ArrayList<Post>(), //
							byteArrayToString(topicMap.get("topic_title")),//
							(Date) topicMap.get("post_time"), //
							byteArrayToString(topicMap.get("short_content")),//
							byteArrayToString(topicMap.get("post_author_name")),
							0);
					topic.unread = (Boolean) topicMap.get("new_post");
					topics.add(topic);
				}
			}

			ListHolder<Topic> topicHolder = new ListHolder<Topic>(topics,
					topicCount, from, to);
			return topicHolder;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load subscribe topics"
					+ ": " + e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Forum> getSubscribedForum(boolean onlyUnread)
			throws TapatalkException
	{
		try
		{
			Map map = toMap(client.call("get_subscribed_forum"));

			@SuppressWarnings("unused")
			int forumCount = (Integer) map.get("total_forums_num");

			final List<Forum> forums = new ArrayList<Forum>();

			for (Object o1 : (Object[]) map.get("forums"))
			{
				Map map2 = (Map) o1;
				if (!onlyUnread || (Boolean) map2.get("new_post"))
				{
					String id = (String) map2.get("forum_id");
					String name = byteArrayToString(map2.get("forum_name"));
					Forum forum = new Forum(id, new ArrayList<Topic>(), name,
							null, null);
					forum.unread = (Boolean) map2.get("new_post");

					forums.add(forum);
				}
			}

			return forums;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load subscribe topics"
					+ ": " + e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Mark a forum as read
	 * 
	 * @param forumId
	 *            if <code>null</code>, all forums are marked read
	 * @throws TapatalkException
	 */
	public void markForumAsRead(String forumId) throws TapatalkException
	{
		try
		{
			if (forumId == null)
			{
				toMap(client.call("mark_all_as_read"));
			}
			else
			{
				Object[] params = new Object[] { forumId };
				toMap(client.callEx("mark_all_as_read", params));
			}

		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("XMLRPC-Error: " + e.getMessage(), e,
					TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Mark a forum as read
	 * 
	 * @param topicId
	 * @throws TapatalkException
	 */
	public void markTopicAsRead(String topicId) throws TapatalkException
	{
		try
		{
			// Object param = new String[] { topicId };
			// toMap(client.call("mark_topic_read", param));
			Object[] params = new String[] { topicId };
			toMap(client.callEx("mark_topic_read", params));

		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Marking topics read: "
					+ e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	public final static int SUBSCRIBE_NONE = -1;
	public final static int SUBSCRIBE_WITHOUT_EMAIL = 0;
	public final static int SUBSCRIBE_INSTANT_EMAIL = 1;
	public final static int SUBSCRIBE_DAILY_EMAIL = 2;
	public final static int SUBSCRIBE_WEEKLY_EMAIL = 3;

	/**
	 * Subscribes to a forum.
	 * 
	 * @param forumId
	 *            The forum-Id
	 * @param mode
	 *            One of the SUBSCRIBE_*-constants
	 * @throws TapatalkException
	 */
	public void subscribeForum(String forumId, int mode)
			throws TapatalkException
	{
		try
		{
			if (mode == SUBSCRIBE_NONE)
			{
				Object[] params = new Object[] { forumId };
				toMap(client.callEx("unsubscribe_forum", params));
			}
			else
			{
				Object[] params = new Object[] { forumId, mode };
				toMap(client.callEx("subscribe_forum", params));
			}

		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load subscribe topics"
					+ ": " + e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Subscribes to a topic.
	 * 
	 * @param topicId
	 *            The topic-id
	 * @param mode
	 *            One of the SUBSCRIBE_*-constants
	 * @throws TapatalkException
	 */
	public void subscribeTopic(String topicId, int mode)
			throws TapatalkException
	{
		try
		{
			if (mode == SUBSCRIBE_NONE)
			{
				Object[] params = new Object[] { topicId };
				toMap(client.callEx("unsubscribe_topic", params));
			}
			else
			{
				Object[] params = new Object[] { topicId, mode };
				toMap(client.callEx("subscribe_topic", params));
			}

		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not load subscribe topics"
					+ ": " + e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	public static final int SEARCHTYPE_QUERY = 1;
	public static final int SEARCHTYPE_LATEST = 2;
	public static final int SEARCHTYPE_PARTICIPATED = 3;
	public static final int SEARCHTYPE_UNREAD = 4;

	public Search searchTopics(int searchType, String query, String username,
			int start, int end, String searchId) throws TapatalkException
	{
		try
		{
			Object[] params = null;
			String method = null;

			switch (searchType)
			{
				case SEARCHTYPE_QUERY:
					method = "search_topic";
					if (searchId == null)
						params = new Object[] { query.getBytes(), start, end };
					else
						params = new Object[] { "".getBytes(), start, end,
								searchId };
					break;
				case SEARCHTYPE_LATEST:
					method = "get_latest_topic";
					if (searchId == null)
						params = new Object[] { start, end };
					else
						params = new Object[] { start, end, searchId };
					break;
				case SEARCHTYPE_PARTICIPATED:
					method = "get_participated_topic";
					if (searchId == null)
						params = new Object[] { username.getBytes(), start, end };
					else
						params = new Object[] { username.getBytes(), start,
								end, searchId };
					break;
				case SEARCHTYPE_UNREAD:
					method = "get_unread_topic";
					if (searchId == null)
						params = new Object[] { start, end };
					else
						params = new Object[] { start, end, searchId };
					break;
			}

			Map map = toMap(client.callEx(method, params));

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
						byteArrayToString(topicMap.get("short_content")),//
						byteArrayToString(topicMap.get("post_author_name")), 0);
				topic.unread = (Boolean) topicMap.get("new_post");
				topics.add(topic);
			}

			return search;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not search" + ": "
					+ e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Reads the list of all available mailboxes.
	 * 
	 * @param boxId
	 * @param start
	 * @param end
	 * @return
	 * @throws TapatalkException
	 */
	public List<Mailbox> getMailbox() throws TapatalkException
	{
		try
		{
			Map map = toMap(client.call("get_box_info"));

			final List<Mailbox> boxList = new ArrayList<Mailbox>();

			for (Object o1 : (Object[]) map.get("list"))
			{
				Map mapMap = toMap(o1);
				Mailbox box = new Mailbox((String) mapMap.get("box_id"),
						byteArrayToString(mapMap.get("box_name")),//
						(Integer) mapMap.get("msg_count"), //
						(Integer) mapMap.get("unread_count"));
				boxList.add(box);
				box.messages = new ArrayList<Message>();
			}

			return boxList;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not read mailbox" + ": "
					+ e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Gets all messsages in a box.
	 * 
	 * @param boxId
	 * @param start
	 * @param end
	 * @return
	 * @throws TapatalkException
	 */
	public Mailbox getBoxContent(String boxId, int start, int end)
			throws TapatalkException
	{
		try
		{
			Object[] params = new Object[] { boxId, start, end };
			Map map = toMap(client.callEx("get_box", params));

			Mailbox mailbox = new Mailbox(boxId, "", (Integer) map
					.get("total_message_count"), (Integer) map
					.get("total_unread_count"));

			final List<Message> messageList = new ArrayList<Message>();
			mailbox.messages = messageList;

			for (Object o1 : (Object[]) map.get("list"))
			{
				Map msgMap = toMap(o1);

				Object[] objects = (Object[]) msgMap.get("msg_to");
				String[] msgTo = new String[objects.length];
				for (int j = 0; j < objects.length; j++)
					msgTo[j] = byteArrayToString((toMap(objects[j])
							.get("username")));

				Message message = new Message((String) msgMap.get("msg_id"),
						((Integer) msgMap.get("msg_state")).equals(1), //
						(Date) msgMap.get("sent_date"),//
						byteArrayToString(msgMap.get("msg_from")),//
						msgTo,//
						byteArrayToString(msgMap.get("msg_subject")), //
						byteArrayToString(msgMap.get("short_content")));
				messageList.add(message);
			}

			return mailbox;
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not read mailbox " + boxId
					+ ": " + e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Reads a message
	 * 
	 * @param boxId
	 * @param messageId
	 * @param asHtml
	 * @return
	 * @throws TapatalkException
	 */
	public Message getMessage(String boxId, String messageId)
			throws TapatalkException
	{
		try
		{
			final Object[] params = new Object[] { messageId, boxId };

			Map<String, Object> mapMap = toMap(client.callEx("get_message",
					params));

			Object[] objects = (Object[]) mapMap.get("msg_to");
			String[] msgTo = new String[objects.length];
			for (int j = 0; j < objects.length; j++)
				msgTo[j] = byteArrayToString((toMap(objects[j]).get("username")));

			Message message = new Message(messageId, false, //
					(Date) mapMap.get("sent_date"),//
					byteArrayToString(mapMap.get("msg_from")),//
					msgTo,//
					byteArrayToString(mapMap.get("msg_subject")), //
					byteArrayToString(mapMap.get("text_body")));
			return message;

		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not read message " + boxId + ","
					+ messageId + ": " + e.getMessage(), e,
					TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Create a topic
	 * 
	 * @param forumId
	 * @param subject
	 * @param content
	 * @throws TapatalkException
	 */
	public void createTopic(String forumId, String subject, String content)
			throws TapatalkException
	{
		try
		{
			final Object[] params = new Object[] { forumId, subject.getBytes(),
					content.getBytes() };

			Map<?, ?> map = toMap(client.callEx("new_topic", params));

			@SuppressWarnings("unused")
			// the newly generated post ID for this new topic.
			String msgId = (String) map.get("post_id");
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not create the topic: "
					+ e.getMessage(), e, TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Create a post.
	 * 
	 * @param boxId
	 * @param messageId
	 * @param asHtml
	 * @return
	 * @throws TapatalkException
	 */
	public void createReply(String forumId, String topicId, String subject,
			String content) throws TapatalkException
	{
		try
		{
			final Object[] params = new Object[] { forumId, topicId,
					subject.getBytes(), content.getBytes() };

			Map<?, ?> map = toMap(client.callEx("reply_post", params));

			@SuppressWarnings("unused")
			String msgId = (String) map.get("topic_id");
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not reply", e,
					TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	/**
	 * Create a message.
	 * 
	 * @param boxId
	 * @param messageId
	 * @param asHtml
	 * @return
	 * @throws TapatalkException
	 */
	public void createMessage(String[] to, String subject, String content)
			throws TapatalkException
	{
		try
		{
			final Object[] params = new Object[] { to, subject.getBytes(),
					content.getBytes() };

			Object result = client.callEx("create_message", params);

			if (result instanceof Boolean)
			{
				// Im Erfolgsfall ist das result vom Typ java.lang.Boolean

				final Boolean ok = (Boolean) result;
				if (!ok)
				{
					throw new TapatalkException("sending message failed",
							TapatalkErrorCode.SEND_MESSAGE_FAILED);
				}
			}
			else
			{
				// Das result kann auch eine Map sein

				try
				{
					toMap(result);
				}
				catch (TapatalkException e)
				{
					throw new TapatalkException("sending message failed: "
							+ e.getMessage(),
							TapatalkErrorCode.SEND_MESSAGE_FAILED);
				}
			}
		}
		catch (XMLRPCException e)
		{
			throw new TapatalkException("Could not create the message", e,
					TapatalkErrorCode.XMLRPC_ERROR);
		}
	}

	@Deprecated
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

	/**
	 * Converting the object to a map. If object is no Map, it throws a
	 * {@link TapatalkException}. If Map contains a key "result" and the value
	 * is not 'true', a {@link TapatalkException} is thrown.
	 * 
	 * @param o
	 * @return
	 * @throws TapatalkException
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> toMap(Object o) throws TapatalkException
	{
		if (!(o instanceof Map))
		{
			throw new TapatalkException("no map: " + o.toString() + " ("
					+ o.getClass() + ")",
					TapatalkErrorCode.UNKNOWN_SERVER_RESPONSE);
		}
		Map<String, Object> map = (Map<String, Object>) o;

		Object object = map.get("result");
		if (object == null)
			return map;

		boolean ok = (Boolean) object;
		if (!ok)
			throw new TapatalkException(byteArrayToString(map
					.get("result_text")),
					TapatalkErrorCode.UNKNOWN_SERVER_RESPONSE);
		return map;
	}

	private List<Forum> createSubForen(Map[] mapArray, String childName)
	{

		final List<Forum> list = new ArrayList<Forum>();

		for (Map map : mapArray)
		{
			String name = byteArrayToString(map.get("forum_name"));
			String content = byteArrayToString(map.get("description"));
			String id = (String) map.get("forum_id");
			Forum forum = new Forum(id, new ArrayList<Topic>(), name, null,
					content);
			forum.url = (String) map.get("url");
			list.add(forum);
			forum.subOnly = (Boolean) map.get("sub_only");
			if (map.containsKey(childName))
				forum.subForen = createSubForen(castToMapArray((Object[]) map
						.get(childName)), childName);
		}
		return list;

	}
}
