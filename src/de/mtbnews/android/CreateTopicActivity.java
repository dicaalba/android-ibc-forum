package de.mtbnews.android;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.util.ServerAsyncTask;

public class CreateTopicActivity extends Activity
{
	private String forumId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.post);
		super.onCreate(savedInstanceState);

		forumId = getIntent().getStringExtra("forum_id");

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{
			private TapatalkClient client;

			@Override
			protected void callServer() throws IOException
			{
				client = ((IBCApplication) getApplication())
						.getTapatalkClient();
				// try
				// {
				// message = client.getMessage(boxId, messageId);
				// }
				// catch (TapatalkException e)
				// {
				// throw new RuntimeException(e);
				// }
			}

			protected void doOnSuccess()
			{
				// MessageActivity.this.setTitle(feed.getTitle());

				final TextView recipient = (TextView) findViewById(R.id.recipient);
				recipient.setText("");
				recipient.setVisibility(View.INVISIBLE);

				// TextView name = (TextView) findViewById(R.id.item_title);
				// name.setText(item.getTitle());

				final TextView subject = (TextView) findViewById(R.id.subject);
				subject.setText("");

				final TextView text = (TextView) findViewById(R.id.content);
				text.setText("");

				Button button = (Button) findViewById(R.id.send);
				button.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						try
						{
							client.createTopic(forumId, subject.getText()
									.toString(), text.getText().toString());
							Toast.makeText(CreateTopicActivity.this,
									R.string.sent_ok, Toast.LENGTH_LONG);
							CreateTopicActivity.this.finish();
						}
						catch (TapatalkException e)
						{
							new AlertDialog.Builder(CreateTopicActivity.this)
									.setTitle(R.string.sent_fail).setMessage(
											e.getMessage()).show();
						}
					}
				});

			}
		}.execute();

	}

}
