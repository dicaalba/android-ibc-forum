package de.mtbnews.android.adapter;

import java.util.regex.Pattern;

import android.util.Log;
import de.mtbnews.android.util.IBC;

/**
 * @author Jan Dankert
 */
public class BBCodeConverter
{

	/**
	 * @param string
	 * @return HTML-formated message
	 */
	public String process(String string)
	{
		string = string.replaceAll("(\r\n|\n\r|\n|\r)", "<br />");

		string = processTag(string, "\\[color=['\"]?(.*?[^'\"])['\"]?\\](.*?)\\[/color\\]",
				"<span style='color:$1'>$2</span>");

		string = processTag(string, "\\[quote[^\\[]*\\](.*?)\\[/quote\\]", "<blockquote>$1</blockquote>");

		string = processTag(string, "\\[list=['\"]?(.*?[^'\"])['\"]?\\](.*?)\\[/list\\]", "<ul>$2</ul>");

		// str = str.replaceAll("(\r\n|\n\r|\n|\r)", "<br>");

		// [color]
		// [size]
		string = processTag(string, "\\[size=['\"]?([0-9]|[1-2][0-9])['\"]?\\](.*?)\\[/size\\]",
				"<span style='font-size:$1px'>$2</span>");

		string = processTag(string, "\\[mention=['\"]?([0-9]+)['\"]?\\](.*?)\\[/mention\\]", "<a href=\""
				+ IBC.IBC_FORUM_URL + "member.php?u=$1\">&rarr;$2</a>");

		// [b][u][i]
		string = processTag(string, "\\[b\\](.*?)\\[/b\\]", "<b>$1</b>");
		string = processTag(string, "\\[u\\](.*?)\\[/u\\]", "<u>$1</u>");
		string = processTag(string, "\\[i\\](.*?)\\[/i\\]", "<i>$1</i>");

		// Smileys (very poor implementation)
		string = processTag(string, ":daumen:", "+1");
		string = processTag(string, ":love:", "&hearts;");
		string = processTag(string, "[*]", "&bull;");

		// [img]
		string = processTag(string, "\\[img\\](.*?)\\[/img\\]", "<a href=\"$1\">Bild anzeigen</a>");

		// [url]
		string = processTag(string, "\\[url\\](.*?)\\[/url\\]", "<a href=\"$1\">$1</a>");
		string = processTag(string, "\\[url=['\"]?(.*?[^'\"])['\"]?\\](.*?)\\[/url\\]", "<a href=\"$1\">$2</a>");
		string = processTag(string, "\\[yt.*\\](.*?)\\[/yt\\]",
				"<a href=\"http://www.youtube.com/watch?v=$2\">Video bei Youtube anzeigen</a>");

		// [email]
		string = processTag(string, "\\[email\\](.*?)\\[/email\\]", "<a href='mailto:$1'>$1</a>");

		return string;
	}

	private static String processTag(String text, String pattern, String replaceWith)
	{
		try
		{
			return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).replaceAll(replaceWith);
		}
		catch (Exception e)
		{
			Log.d(IBC.TAG, "Error while processing '" + pattern + "': " + e.getMessage(), e);
			return text;
		}
	}

}
