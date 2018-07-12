package com.unity3d.ads.request;

import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Looper;
import android.os.Message;

import com.unity3d.ads.log.DeviceLog;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebRequestThread {

	private static boolean _ready = false;
	private static LinkedBlockingQueue<Runnable> _queue;
	private static CancelableThreadPoolExecutor _pool;
	private static int _corePoolSize = 1;
	private static int _maximumPoolSize = 1;
	private static long _keepAliveTime = 1000;
	private static final Object _readyLock = new Object();

	private static synchronized void init() {
		_queue = new LinkedBlockingQueue<>();
		_pool = new CancelableThreadPoolExecutor(_corePoolSize, _maximumPoolSize, _keepAliveTime, TimeUnit.MILLISECONDS, _queue);
		_pool.prestartAllCoreThreads();

		_queue.add(new Runnable() {
			@Override
			public void run() {
				_ready = true;

				synchronized(_readyLock) {
					_readyLock.notify();
				}
			}
		});

		while(!_ready) {
			try {
				synchronized(_readyLock) {
					_readyLock.wait();
				}
			} catch (InterruptedException e) {
				DeviceLog.debug("Couldn't synchronize thread");
				return;
			}
		}
	}

	public static synchronized void reset() {
		cancel();

		if (_pool != null) {
			_pool.shutdown();
			try {
				_pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
			}
			_queue.clear();
			_pool = null;
			_queue = null;
			_ready = false;
		}
	}

	public static synchronized void cancel () {
		if (_pool != null) {
			_pool.cancel();
			for(Runnable runnable: _queue) {
				if (runnable instanceof WebRequestRunnable)
					((WebRequestRunnable)runnable).setCancelStatus(true);
			}
			_queue.clear();
			_pool.purge();
		}
	}

	public static synchronized void request (String url, WebRequest.RequestType requestType, Map<String, List<String>> headers, Integer connectTimeout, Integer readTimeout, IWebRequestListener listener) {
		request(url, requestType, headers, null, connectTimeout, readTimeout, listener);
	}

	public static synchronized void request (String url, WebRequest.RequestType requestType, Map<String, List<String>> headers, String requestBody, Integer connectTimeout, Integer readTimeout, IWebRequestListener listener) {
		if(!_ready) {
			init();
		}

		if (url == null || url.length() < 3) {
			listener.onFailed(url, "Request is NULL or too short");
			return;
		}

		_queue.add(new WebRequestRunnable(url, requestType.name(), requestBody, connectTimeout, readTimeout, headers, listener));
	}

	public static synchronized void setConcurrentRequestCount(int count) {
		_corePoolSize = count;
		_maximumPoolSize = _corePoolSize;

		if (_pool != null) {
			_pool.setCorePoolSize(_corePoolSize);
			_pool.setMaximumPoolSize(_maximumPoolSize);
		}
	}

	public static synchronized void setMaximumPoolSize(int count) {
		_maximumPoolSize = count;

		if (_pool != null) {
			_pool.setMaximumPoolSize(_maximumPoolSize);
		}
	}

	public static synchronized void setKeepAliveTime(long milliseconds) {
		_keepAliveTime = milliseconds;

		if (_pool != null) {
			_pool.setKeepAliveTime(_keepAliveTime, TimeUnit.MILLISECONDS);
		}
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