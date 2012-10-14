/**
 * 
 */
package de.mtbnews.android.util;

/**
 * 
 * @author dankert
 * 
 */
public class IBCException extends Exception
{

	private int errorResId;

	/**
	 * @param errorResId
	 *            Resource-Id der Fehlermeldung
	 * @param detailMessage
	 * @param cause
	 */
	public IBCException(int errorResId, String detailMessage, Throwable cause)
	{
		super(detailMessage, cause);
		this.errorResId = errorResId;

	}

	/**
	 * @return
	 */
	public int getErrorResId()
	{
		return errorResId;
	}
}
