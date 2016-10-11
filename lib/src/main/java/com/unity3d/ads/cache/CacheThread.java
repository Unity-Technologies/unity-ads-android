package com.unity3d.ads.cache;

import com.unity3d.ads.log.DeviceLog;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

public class CacheThread extends Thread {
	public static final int MSG_DOWNLOAD = 1;
	private static final Object _readyLock = new Object();
	private static CacheThreadHandler _handler = null;
	private static boolean _ready = false;
	private static int _connectTimeout = 30000;
	private static int _readTimeout = 30000;
	private static int _progressInterval = 0;

	private static void init() {
		CacheThread thread = new CacheThread();
		thread.setName("UnityAdsCacheThread");
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

	public static synchronized void download(String source, String target) {
		if(!_ready) {
			init();
		}

		Bundle params = new Bundle();
		params.putString("source", source);
		params.putString("target", target);
		params.putInt("connectTimeout", _connectTimeout);
		params.putInt("readTimeout", _readTimeout);
		params.putInt("progressInterval", _progressInterval);

		Message msg = new Message();
		msg.what = MSG_DOWNLOAD;
		msg.setData(params);

		_handler.setCancelStatus(false);
		_handler.sendMessage(msg);
	}

	public static boolean isActive() {
		if(!_ready) {
			return false;
		}

		return _handler.isActive();
	}

	public static void cancel() {
		if(!_ready) {
			return;
		}

		_handler.removeMessages(MSG_DOWNLOAD);
		_handler.setCancelStatus(true);
	}

	public static int getProgressInterval() {
		return _progressInterval;
	}

	public static void setProgressInterval(int interval) {
		_progressInterval = interval;
	}

	public static int getConnectTimeout() {
		return _connectTimeout;
	}

	public static void setConnectTimeout (int connectTimeout) {
		_connectTimeout = connectTimeout;
	}

	public static int getReadTimeout() {
		return _readTimeout;
	}

	public static void setReadTimeout (int readTimeout) {
		_readTimeout = readTimeout;
	}

	@Override
	public void run() {
		Looper.prepare();
		_handler = new CacheThreadHandler();
		_ready = true;
		synchronized (_readyLock) {
			_readyLock.notify();
		}
		Looper.loop();
	}
}