/**
 * 
 */
package de.mtbnews.android.tapatalk;

/**
 * @author dankert
 * 
 */
public class TapatalkException extends Exception
{

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public TapatalkException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	/**
	 * @param detailMessage
	 */
	public TapatalkException(String detailMessage)
	{
		super(detailMessage);
	}

}
