package com.unity3d.ads.request;

import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Looper;
import android.os.Message;

import com.unity3d.ads.log.DeviceLog;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public class WebRequestThread extends Thread {

	protected static final int MSG_REQUEST = 1;
	private static WebRequestHandler _handler;
	private static boolean _ready = false;
	private static final Object _readyLock = new Object();

	private static void init() {
		WebRequestThread thread = new WebRequestThread();
		thread.setName("UnityAdsWebRequestThread");
		thread.start();

		while(!_ready) {
			try {
				synchronized(_readyLock) {
					_readyLock.wait();
				}
			} catch (InterruptedException e) {
				DeviceLog.debug("Couldn't synchronize thread");
			}
		}
	}

	@Override
	public void run() {
		Looper.prepare();

		if (_handler == null) {
			_handler = new WebRequestHandler();
		}

		_ready = true;

		synchronized(_readyLock) {
			_readyLock.notify();
		}

		Looper.loop();
	}

	public static void cancel () {
		if (_handler != null) {
			_handler.removeMessages(MSG_REQUEST);
			_handler.setCancelStatus(true);
		}
	}

	public static synchronized void request (String url, WebRequest.RequestType requestType, Map<String, List<String>> headers, Integer connectTimeout, Integer readTimeout, IWebRequestListener listener) {
		request(url, requestType, headers, null, connectTimeout, readTimeout, listener);
	}

	public static synchronized void request (String url, WebRequest.RequestType requestType, Map<String, List<String>> headers, String requestBody, Integer connectTimeout, Integer readTimeout, IWebRequestListener listener) {
		request(MSG_REQUEST, url, requestType, headers, requestBody, connectTimeout, readTimeout, listener, new WebRequestResultReceiver(_handler, listener));
	}

	public static synchronized void request (int msgWhat, String url, WebRequest.RequestType requestType, Map<String, List<String>> headers, String requestBody, Integer connectTimeout, Integer readTimeout, IWebRequestListener listener, WebRequestResultReceiver receiver) {
		if(!_ready) {
			init();
		}

		if (url == null || url.length() < 3) {
			listener.onFailed(url, "Request is NULL or too short");
			return;
		}

		Bundle params = new Bundle();
		params.putString("url", url);
		params.putString("type", requestType.name());
		params.putString("body", requestBody);
		params.putParcelable("receiver", receiver);
		params.putInt("connectTimeout", connectTimeout);
		params.putInt("readTimeout", readTimeout);

		if (headers != null) {
			for (String s : headers.keySet()) {
				String[] h = new String[headers.get(s).size()];
				params.putStringArray(s, headers.get(s).toArray(h));
			}
		}

		Message msg = new Message();
		msg.what = msgWhat;
		msg.setData(params);

		_handler.setCancelStatus(false);
		_handler.sendMessage(msg);
	}

	public static synchronized boolean resolve (final String host, final IResolveHostListener listener) {
		if (host == null || host.length() < 3) {
			listener.onFailed(host, ResolveHostError.INVALID_HOST, "Host is NULL");
			return false;
		}
		else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					final ConditionVariable cv = new ConditionVariable();
					Thread t = null;
					try {
						t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									InetAddress address = InetAddress.getByName(host);
									String strAddress = address.getHostAddress();
									listener.onResolve(host, strAddress);
								} catch(UnknownHostException e) {
									DeviceLog.exception("Unknown host", e);
									listener.onFailed(host, ResolveHostError.UNKNOWN_HOST, e.getMessage());
								}
								cv.open();
							}
						});
						t.start();
					}
					catch (Exception e) {
						DeviceLog.exception("Exception while resolving host", e);
						listener.onFailed(host, ResolveHostError.UNEXPECTED_EXCEPTION, e.getMessage());
					}

					boolean success = cv.block(20000);
					if (!success && t != null) {
						t.interrupt();
						listener.onFailed(host, ResolveHostError.TIMEOUT, "Timeout");
					}
				}
			}).start();
		}

		return true;
	}
}