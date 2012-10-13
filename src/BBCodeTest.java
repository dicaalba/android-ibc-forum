import de.mtbnews.android.adapter.BBCodeConverter;
import junit.framework.TestCase;

public class BBCodeTest extends TestCase
{
	public void testQuote() throws Exception
	{

		String in = "[quotE=Schrott]Hallo[/Quote]Hallo [B]Du[/b].";
		String out = "<blockquote>Hallo</blockquote>Hallo <b>Du</b>.";
		testInOut(in, out);
	}

	public void testQuoteSimple() throws Exception
	{

		String in = "[quotE]Hallo[/Quote]Hallo [B]Du[/b].";
		String out = "<blockquote>Hallo</blockquote>Hallo <b>Du</b>.";
		testInOut(in, out);
	}

	public void testQuoteDouble() throws Exception
	{

		String in = "[quotE=Schrott]Hallo[/Quote]Hallo [B]Du[/b] [B]da[/b].[quotE=Schrott]Hallo[/Quote]Hallo [B]Sie[/b] [B]da[/b].";
		String out = "<blockquote>Hallo</blockquote>Hallo <b>Du</b> <b>da</b>.<blockquote>Hallo</blockquote>Hallo <b>Sie</b> <b>da</b>.";
		testInOut(in, out);
	}

	public void testLink() throws Exception
	{

		String in = "[url]http://example.com/image.png[/url].";
		String out = "<a href=\"http://example.com/image.png\">http://example.com/image.png</a>.";
		testInOut(in, out);
		
		in = "[url=http://example.com/image.png]haha[/url].";
		out = "<a href=\"http://example.com/image.png\">haha</a>.";
		testInOut(in, out);
	}

	public void testImage() throws Exception
	{

		String in = "[img]http://example.com/image.png[/imG].";
		String out = "<a href=\"http://example.com/image.png\">Bild anzeigen</a>.";
		testInOut(in, out);
	}

	
	private void testInOut(String in, String out) throws Exception
	{
		BBCodeConverter bbCodeConverter = new BBCodeConverter();
		String processed = bbCodeConverter.process(in);
		assertEquals(out, processed);
	}
}
