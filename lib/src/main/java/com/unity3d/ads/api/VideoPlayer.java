package com.unity3d.ads.api;

import android.os.Build;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.video.VideoPlayerError;
import com.unity3d.ads.video.VideoPlayerEvent;
import com.unity3d.ads.video.VideoPlayerView;
import com.unity3d.ads.webview.WebViewEventCategory;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class VideoPlayer {
	private static VideoPlayerView _videoPlayerView;

	public static void setVideoPlayerView (VideoPlayerView videoPlayerView) {
		_videoPlayerView = videoPlayerView;
	}

	public static VideoPlayerView getVideoPlayerView() {
		return _videoPlayerView;
	}

	@WebViewExposed
	public static void setProgressEventInterval (final Integer milliseconds, final WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getVideoPlayerView() != null) {
					getVideoPlayerView().setProgressEventInterval(milliseconds);
				}
			}
		});

		if (getVideoPlayerView() != null) {
			callback.invoke();
		}
		else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void getProgressEventInterval (final WebViewCallback callback) {
		if (getVideoPlayerView() != null) {
			callback.invoke(getVideoPlayerView().getProgressEventInterval());
		}
		else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void prepare (final String url, final Double initialVolume, final WebViewCallback callback) {
		prepare(url, initialVolume, 0, callback);
	}

	@WebViewExposed
	public static void prepare (final String url, final Double initialVolume, final Integer timeout, final WebViewCallback callback) {
		DeviceLog.debug("Preparing video for playback: " + url);

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getVideoPlayerView() != null) {
					getVideoPlayerView().prepare(url, initialVolume.floatValue(), timeout.intValue());
				}
			}
		});

		if (getVideoPlayerView() != null) {
			callback.invoke(url);
		}
		else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void play (final WebViewCallback callback) {
		DeviceLog.debug("Starting playback of prepared video");

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getVideoPlayerView() != null) {
					getVideoPlayerView().play();
				}
			}
		});

		if (getVideoPlayerView() != null) {
			callback.invoke();
		}
		else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void pause (final WebViewCallback callback) {
		DeviceLog.debug("Pausing current video");

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getVideoPlayerView() != null) {
					getVideoPlayerView().pause();
				}
			}
		});

		if (getVideoPlayerView() != null) {
			callback.invoke();
		}
		else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void stop (final WebViewCallback callback) {
		DeviceLog.debug("Stopping current video");

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getVideoPlayerView() != null) {
					getVideoPlayerView().stop();
				}
			}
		});

		if (getVideoPlayerView() != null) {
			callback.invoke();
		}
		else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void seekTo (final Integer time, final WebViewCallback callback) {
		DeviceLog.debug("Seeking video to time: " + time);

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getVideoPlayerView() != null) {
					getVideoPlayerView().seekTo(time);
				}
			}
		});

		if (getVideoPlayerView() != null) {
			callback.invoke();
		} else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}

	}

	@WebViewExposed
	public static void getCurrentPosition (final WebViewCallback callback) {
		if (getVideoPlayerView() != null) {
			callback.invoke(getVideoPlayerView().getCurrentPosition());
		} else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void getVolume (final WebViewCallback callback) {
		if (getVideoPlayerView() != null) {
			callback.invoke(getVideoPlayerView().getVolume());
		}
		else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void setVolume (final Double volume, final WebViewCallback callback) {
		DeviceLog.debug("Setting video volume: " + volume);

		if (getVideoPlayerView() != null) {
			getVideoPlayerView().setVolume(volume.floatValue());
			callback.invoke(volume);
		}
		else {
			callback.error(VideoPlayerError.VIDEOVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void setInfoListenerEnabled (final boolean enabled, final WebViewCallback callback) {
		if (Build.VERSION.SDK_INT > 16) {
			if (getVideoPlayerView() != null) {
				getVideoPlayerView().setInfoListenerEnabled(enabled);
				callback.invoke(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.INFO, enabled);
			}
			else {
				callback.error(VideoPlayerError.VIDEOVIEW_NULL);
			}
		}
		else {
			callback.error(VideoPlayerError.API_LEVEL_ERROR, Build.VERSION.SDK_INT, enabled);
		}
	}
}
