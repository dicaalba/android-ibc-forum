/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * @author dankert
 * 
 */
public class PostActivity extends Activity
{
	public static final String ID = "id";
	public static final String CLIENT = "client";
	private String objectid;

	Map<String, String> data;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			@Override
			protected void callServer() throws IOException
			{

				XMLRPCClient client = new XMLRPCClient(IBC.IBC_FORUM_CONNECTOR_URL);
				// add 2 to 4
				Object[] params = new Object[] {
						prefs.getString("username", "").getBytes(),
						prefs.getString("password", "").getBytes() };

				try
				{
					Object sum = client.callEx("login", params);
					System.out.println(sum.getClass());
					System.out.println(sum);
					
					Object l = client.call("get_inbox_stat");
					System.out.println(l.toString() );
					
					Object i = client.call("get_box_info");
					System.out.println(i.toString() );
					
				}
				catch (XMLRPCException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

			protected void doOnSuccess()
			{
			}

		}.execute();

	}

}
