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
	public static enum TapatalkErrorCode
	{
		NO_USERNAME, LOGIN_FAILED, XMLRPC_ERROR, UNKNOWN_SERVER_RESPONSE, SEND_MESSAGE_FAILED;
	}

	private TapatalkErrorCode errorCode;

	/**
	 * @param detailMessage
	 * @param throwable
	 * @param errorCode
	 *            TODO
	 */
	public TapatalkException(String detailMessage, Throwable throwable,
			TapatalkErrorCode errorCode)
	{

		super(detailMessage, throwable);
		this.errorCode = errorCode;
	}

	/**
	 * @param detailMessage
	 */
	public TapatalkException(String detailMessage, TapatalkErrorCode errorCode)
	{
		super(detailMessage);
		this.errorCode = errorCode;
	}

	/**
	 * @return
	 */
	public TapatalkErrorCode getErrorCode()
	{
		return errorCode;
	}
}
