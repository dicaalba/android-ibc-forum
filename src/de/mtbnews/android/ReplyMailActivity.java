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
import de.mtbnews.android.tapatalk.wrapper.Message;
import de.mtbnews.android.util.ServerAsyncTask;

public class ReplyMailActivity extends Activity
{
	private String boxId;
	private String messageId;
	private TapatalkClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		client = ((IBCApplication) getApplication()).getTapatalkClient();

		setTheme(((IBCApplication) getApplication()).themeResId);
		setContentView(R.layout.post);

		final TextView recipient = (TextView) findViewById(R.id.recipient);
		final TextView subject = (TextView) findViewById(R.id.subject);
		final TextView text = (TextView) findViewById(R.id.content);

		if (getIntent().hasExtra("box_id"))
		{

			setTitle(R.string.reply);
			boxId = getIntent().getStringExtra("box_id");
			messageId = getIntent().getStringExtra("message_id");

			new ServerAsyncTask(this, R.string.waitingfor_mailbox)
			{
				private Message message;

				@Override
				protected void callServer() throws TapatalkException
				{
					message = client.getMessage(boxId, messageId);
				}

				protected void doOnSuccess()
				{
					// MessageActivity.this.setTitle(feed.getTitle());

					recipient.setText(message.from);

					// TextView name = (TextView) findViewById(R.id.item_title);
					// name.setText(item.getTitle());

					subject.setText(message.subject.startsWith("Re: ") ? ""
							: "Re: " + message.subject);

					text.setText("[quote]" + message.getContent()
							+ "[/quote]\n\n");

				}
			}.execute();
		}
		else
		{
			setTitle(R.string.new_message);
		}

		final Button button = (Button) findViewById(R.id.send);
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new ServerAsyncTask(ReplyMailActivity.this,
						R.string.waitingfor_sending)
				{
					@Override
					protected void callServer() throws IOException,
							TapatalkException
					{
						client.createMessage(new String[] { recipient.getText()
								.toString() }, subject.getText().toString(),
								text.getText().toString());
					}

					protected void doOnSuccess()
					{
						Toast.makeText(ReplyMailActivity.this,
								R.string.sent_ok, Toast.LENGTH_LONG);
						ReplyMailActivity.this.finish();
					}
				}.execute();
			}
		});

	}
}
