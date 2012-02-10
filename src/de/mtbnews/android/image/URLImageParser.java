package de.mtbnews.android.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.View;
import de.mtbnews.android.util.IBC;

public class URLImageParser implements ImageGetter
{
	/**
	 * Einfacher Bildercache, der zu einer URL das {@link Drawable} speichert.
	 * Achtung: Bei nächsten GC-Lauf wird der Inhalt der Map entfernt.
	 */
	private static Map<String, Drawable> drawableCache = new WeakHashMap<String, Drawable>();

	private Context context;
	private View view;

	/***
	 * Construct the URLImageParser which will execute AsyncTask and refresh the
	 * container
	 * 
	 * @param t
	 * @param c
	 */
	public URLImageParser(View t, Context c)
	{
		this.context = c;
		this.view = t;
	}

	public Drawable getDrawable(String source)
	{
		URLDrawable urlDrawable = new URLDrawable();

		// get the actual source
		ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);

		asyncTask.execute(source);

		// return reference to URLDrawable where I will change with actual image
		// from
		// the src tag
		return urlDrawable;
	}

	public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>
	{
		URLDrawable urlDrawable;

		public ImageGetterAsyncTask(URLDrawable d)
		{
			this.urlDrawable = d;
		}

		@Override
		protected Drawable doInBackground(String... params)
		{
			String source = params[0];
			return fetchDrawable(source);
		}

		@Override
		protected void onPostExecute(Drawable result)
		{
			if (result != null)
			{

				// set the correct bound according to the result from HTTP call
				urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(),
						0 + result.getIntrinsicHeight());

				// change the reference of the current drawable to the result
				// from the HTTP call
				urlDrawable.drawable = result;

				// redraw the image by invalidating the container
				URLImageParser.this.view.invalidate();
			}
		}

		/***
		 * Get the Drawable from URL
		 * 
		 * @param urlString
		 * @return
		 */
		private Drawable fetchDrawable(String urlString)
		{
			try
			{
				// Bild bereits im Cache?
				final Drawable drawableFromCache = drawableCache.get(urlString);
				if (drawableFromCache != null)
					return drawableFromCache;

				InputStream is = fetch(urlString);
				Drawable drawable = null;
				try
				{
					drawable = Drawable.createFromStream(is, "src");
				}
				catch (OutOfMemoryError e)
				{
					Log.w(IBC.TAG, "OutOfMemory: Image too big: "
							+ e.getMessage(), e);
				}
				catch (Exception e)
				{
					Log
							.w(IBC.TAG, "unable to load image: "
									+ e.getMessage(), e);
				}
				if (drawable == null)
					Log.w(IBC.TAG, "drawable is null, url=" + urlString);
				else
					drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(),
							0 + drawable.getIntrinsicHeight());

				// Bild in den Cache einfügen
				drawableCache.put(urlString, drawable);
				return drawable;
			}
			catch (Exception e)
			{
				Log.w(IBC.TAG, e);
				return null;
			}
		}

		private InputStream fetch(String urlString)
				throws MalformedURLException, IOException
		{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet request = new HttpGet(urlString);
			HttpResponse response = httpClient.execute(request);
			return response.getEntity().getContent();
		}
	}
}
