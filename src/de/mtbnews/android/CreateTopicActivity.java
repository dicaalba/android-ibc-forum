package de.mtbnews.android;

import java.io.IOException;

import android.app.Activity;
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

		final TextView recipient = (TextView) findViewById(R.id.recipient);
		recipient.setText("");
		recipient.setVisibility(View.INVISIBLE);

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
				new ServerAsyncTask(CreateTopicActivity.this,
						R.string.send)
				{

					@Override
					protected void callServer() throws IOException,
							TapatalkException
					{
						TapatalkClient client = ((IBCApplication) getApplication())
								.getTapatalkClient();
						client.createTopic(forumId, subject.getText()
								.toString(), text.getText().toString());
					}

					protected void doOnSuccess()
					{
						Toast.makeText(CreateTopicActivity.this,
								R.string.sent_ok, Toast.LENGTH_LONG);
						CreateTopicActivity.this.finish();
					}
				}.execute();
			}
		});

	}

}
