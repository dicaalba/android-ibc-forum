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
		setContentView(R.layout.post);
		super.onCreate(savedInstanceState);

		topicId = getIntent().getStringExtra("topic_id");
		forumId = getIntent().getStringExtra("forum_id");
		subject = getIntent().getStringExtra("subject");
		quote = getIntent().getStringExtra("quote");

		ReplyPostActivity.this.setTitle(R.string.reply);

		final TextView recipient = (TextView) findViewById(R.id.recipient);
		recipient.setVisibility(View.INVISIBLE);

		final TextView subject = (TextView) findViewById(R.id.subject);
		subject
				.setText(ReplyPostActivity.this.subject != null ? ReplyPostActivity.this.subject
						: "");

		final TextView text = (TextView) findViewById(R.id.content);
		text.setText(quote != null ? "[quote]" + quote + "[/quote]" : "");

		Button button = (Button) findViewById(R.id.send);
		button.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				new ServerAsyncTask(ReplyPostActivity.this, R.string.send)
				{

					@Override
					protected void callServer() throws TapatalkException
					{
						TapatalkClient client = ((IBCApplication) getApplication())
								.getTapatalkClient();

						client.createReply(forumId, topicId, subject.getText()
								.toString(), text.getText().toString());
					}

					protected void doOnSuccess()
					{
						Toast.makeText(ReplyPostActivity.this,
								R.string.sent_ok, Toast.LENGTH_LONG);
						ReplyPostActivity.this.finish();
					}
				}.execute();
			}
		});
	}
}
