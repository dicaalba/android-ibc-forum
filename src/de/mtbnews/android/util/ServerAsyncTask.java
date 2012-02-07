/**
 * 
 */
package de.mtbnews.android.util;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import de.mtbnews.android.tapatalk.TapatalkException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Ein asynchroner Task für den Zugriff auf den OpenRat-CMS-Server. Der Aufruf
 * des Servers muss in der zu überschreibenden Methode {@link #callServer()}
 * durchgeführt werden.<br>
 * <br>
 * <br>
 * Während der Serverabfrage wird ein {@link ProgressDialog} angezeigt. Falls
 * die Abfrage nicht erfolgreich ist, wird automatisch ein {@link AlertDialog}
 * mit einer Fehlermeldung angezeigt.<br>
 * <br>
 * <br>
 * Durch überschreiben von {@link #doOnError(IOException)} kann selber auf einen
 * Fehler reagiert werden. Durch Überschreiben von {@link #doOnSuccess()} kann
 * eine Aktion nach erfolgreicher Serveranfrage ausgeführt werden. <br>
 * 
 * @author dankert
 * 
 */
public abstract class ServerAsyncTask extends AsyncTask<Void, Void, Void>
{
	private ProgressDialog progressDialog;
	private Context context;
	private AlertDialog alertDialog;
	private Exception error;

	// private static final Semaphore lock2 = new Semaphore(1,true);

	/**
	 * @param context
	 *            Context des Aufrufers
	 * @param message
	 *            Resource-Id für den Text im {@link ProgressDialog}.
	 */
	public ServerAsyncTask(Context context, int message)
	{
		this.context = context;

		this.progressDialog = new ProgressDialog(context);
		// progressDialog.setTitle(R.string.loading);
		progressDialog.setMessage(context.getResources().getString(message));
	}

	@Override
	final protected void onPreExecute()
	{
		progressDialog.setCancelable(true);
		progressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						ServerAsyncTask.this.cancel(true);
					}
				});

		progressDialog.show();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	final protected void onPostExecute(Void result)
	{
		progressDialog.dismiss();

		if (error != null)
		{
			doOnError(error);
		}
		else
		{
			doOnSuccess();
		}
	}

	/**
	 * Wird aufgerufen, falls die Serveranfrage nicht durchgeführt werden
	 * konnte. Läuft im UI-Thread.
	 * 
	 * @param error
	 *            Exception, die aufgetreten ist.
	 */
	protected void doOnError(Exception error)
	{
		progressDialog.dismiss();

		final Builder builder = new AlertDialog.Builder(this.context);
		alertDialog = builder.setCancelable(true).create();
		final int causeRId = ExceptionUtils.getResourceStringId(error);
		String msg = // this.context.getResources().getString(R.string.reason)
		// + ":\n\n" +
		error.getMessage();

		Throwable t = error;
		while (t.getCause() != null)
		{
			t = t.getCause();
			msg += ": " + t.getMessage();
		}

		alertDialog.setTitle(causeRId);
		alertDialog.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		alertDialog.setMessage(msg);
		alertDialog.show();

	}

	/**
	 * Wird aufgerufen, falls die Serveranfrage erfolgreich durchgeführt werden
	 * konnte. Läuft im UI-Thread.
	 */
	protected void doOnSuccess()
	{
	}

	/**
	 * Startet die Serveranfrage und fängt auftretene Fehler.
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	final protected Void doInBackground(Void... params)
	{
		try
		{
			callServer();
		}
		catch (IOException e)
		{
			Log.w(this.getClass().getName(), e.getMessage(), e);
			error = e;
		}
		catch (TapatalkException e)
		{
			Log.w(this.getClass().getName(), e.getMessage(), e);
			error = e;
		}
		catch (RuntimeException e)
		{
			// Hier den Dialogo schließen, da es sonst zu
			// "has leaked window"-Fehlern kommt.
			progressDialog.dismiss();

			Log.w(this.getClass().getName(), e.getMessage(), e);
			throw e; // Und weiterwerfen, da den Fehler hier niemand behandeln
			// kann. Android schließt somit die Activity.
		}

		return null;
	}

	@Override
	protected void onCancelled()
	{
		// Hier den Dialogo schließen, da es sonst zu
		// "has leaked window"-Fehlern kommt.
		progressDialog.dismiss();
	}

	/**
	 * Ausführen der Serveranfrage. Auftretene {@link IOException} sollte
	 * weitergeworfen werden, da daraus ein {@link AlertDialog} erzeugt wird.
	 * 
	 * @throws IOException
	 *             Vom Server erzeugte Fehler
	 */
	protected abstract void callServer() throws IOException, TapatalkException;

	/**
	 * 
	 * @param params
	 */
	public final void executeSynchronized(Void... params)
	{
		super.execute(params);
	}

}
