package com.unity3d.ads.request;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CancelableThreadPoolExecutor extends ThreadPoolExecutor {

	private final List<Runnable> _activeRunnable;

	public CancelableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, LinkedBlockingQueue<Runnable> queue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit,queue);
		_activeRunnable = new LinkedList<>();
	}

	@Override
	protected synchronized void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		_activeRunnable.add(r);
	}

	@Override
	protected synchronized void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		_activeRunnable.remove(r);
	}

	public synchronized void cancel() {
		for(Runnable r : _activeRunnable) {
			if (r instanceof WebRequestRunnable) {
				((WebRequestRunnable)r).setCancelStatus(true);
			}
		}
	}
}
