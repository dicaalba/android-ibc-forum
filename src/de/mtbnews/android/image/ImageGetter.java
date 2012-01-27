package de.mtbnews.android.image;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;

/**
 * @author dankert
 * 
 */
public class ImageGetter implements Html.ImageGetter
{

	public Drawable getDrawable(String source)
	{
		Drawable d = null;
		String imageSource;
		// if (!source.startsWith("http://www"))
		// {
		// imageSource = "http://www.minhembio.com" + source;
		// }
		// else
		// {
		imageSource = source;
		// }

		try
		{
			URL myFileUrl = new URL(imageSource);

			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			BitmapDrawable a = new BitmapDrawable(is);
			d = a.getCurrent();
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
			// d = null;
		}

		return d;
	}
}