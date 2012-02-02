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

public class ReplyPostActivity extends Activity
{
	private String forumId;
	private String topicId;
	private String subject;
	private String quote;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (((IBCApplication) getApplication()).ibcTheme)
			setTheme(R.style.IBC);

		setContentView(R.layout.post);
		super.onCreate(savedInstanceState);

		topicId = getIntent().getStringExtra("topic_id");
		forumId = getIntent().getStringExtra("forum_id");
		subject = getIntent().getStringExtra("subject");
		quote = getIntent().getStringExtra("quote");

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
				ReplyPostActivity.this.setTitle(R.string.reply);

				final TextView recipient = (TextView) findViewById(R.id.recipient);
				recipient.setVisibility(View.INVISIBLE);

				final TextView subject = (TextView) findViewById(R.id.subject);
				subject
						.setText(ReplyPostActivity.this.subject != null ? ReplyPostActivity.this.subject
								: "");

				final TextView text = (TextView) findViewById(R.id.content);
				text.setText(quote != null ? "[quote]"+quote+"[/quote]" : "");

				Button button = (Button) findViewById(R.id.send);
				button.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						try
						{
							client.createReply(forumId, topicId, subject
									.getText().toString(), text.getText()
									.toString());
							Toast.makeText(ReplyPostActivity.this,
									R.string.sent_ok, Toast.LENGTH_LONG);
							ReplyPostActivity.this.finish();
						}
						catch (TapatalkException e)
						{
							new AlertDialog.Builder(ReplyPostActivity.this)
									.setTitle(R.string.sent_fail).setMessage(
											e.getMessage()).show();
						}
					}
				});

			}
		}.execute();

	}

}
