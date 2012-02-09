/**
 * 
 */
package de.mtbnews.android.util;

import de.mtbnews.android.R;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException.TapatalkErrorCode;

/**
 * @author dankert
 * 
 */
public final class Utils
{

	public static final int getResId(TapatalkErrorCode errorCode)
	{
		switch (errorCode)
		{
			case LOGIN_FAILED:
				return R.string.login_failed;
			case NO_USERNAME:
				return R.string.nousername;
			case SEND_MESSAGE_FAILED:
				return R.string.sent_fail;
			case XMLRPC_ERROR:
				return R.string.xmlrpc_error;
			case UNKNOWN_SERVER_RESPONSE:
			default:
				return R.string.error_common_server;
		}
	}

	/**
	 * Stellt fest, ob das Login abgelaufen ist. Dies ist der Fall, wenn
	 * Benutzer nicht angemeldet ist oder das letzte Login zu alt ist.
	 * 
	 * @param client
	 * @return
	 */
	public static final boolean loginExceeded(TapatalkClient client)
	{
		return !client.loggedIn
				|| System.currentTimeMillis() - client.loginTime > IBC.LOGIN_TIMEOUT * 60 * 1000;
	}
}
