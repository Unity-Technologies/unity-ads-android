package com.unity3d.ads.video;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.VideoView;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import java.util.Timer;
import java.util.TimerTask;

public class VideoPlayerView extends VideoView {
	private String _videoUrl;
	private Timer _videoTimer;
	private Timer _prepareTimer;
	private int _progressEventInterval = 500;
	private MediaPlayer _mediaPlayer = null;
	private Float _volume = null;
	private boolean _infoListenerEnabled = true;

	public VideoPlayerView(Context context) {
		super(context);
	}

	private void startVideoProgressTimer () {
		_videoTimer = new Timer();
		_videoTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				boolean isPlaying = false;
				try {
					isPlaying = isPlaying();
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PROGRESS, getCurrentPosition());
				}
				catch (IllegalStateException e) {
					DeviceLog.exception("Exception while sending current position to webapp", e);
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.ILLEGAL_STATE, VideoPlayerEvent.PROGRESS, _videoUrl, isPlaying);
				}
			}
		}, _progressEventInterval, _progressEventInterval);

	}

	public void stopVideoProgressTimer () {
		if (_videoTimer != null) {
			_videoTimer.cancel();
			_videoTimer.purge();
			_videoTimer = null;
		}
	}

	private void startPrepareTimer (long delay) {
		_prepareTimer = new Timer();
		_prepareTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!isPlaying()) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PREPARE_TIMEOUT, _videoUrl);
					DeviceLog.error("Video player prepare timeout: " + _videoUrl);
				}
			}
		}, delay);
	}

	public void stopPrepareTimer () {
		if (_prepareTimer != null) {
			_prepareTimer.cancel();
			_prepareTimer.purge();
			_prepareTimer = null;
		}
	}

	public boolean prepare (final String url, final float initialVolume, final int timeout) {
		DeviceLog.entered();

		_videoUrl = url;

		setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				stopPrepareTimer();

				if (mp != null) {
					_mediaPlayer = mp;
				}

				setVolume(initialVolume);
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PREPARED, _videoUrl, mp.getDuration(), mp.getVideoWidth(), mp.getVideoHeight());
			}
		});

		setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				stopPrepareTimer();

				if (mp != null) {
					_mediaPlayer = mp;
				}

				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.GENERIC_ERROR, _videoUrl, what, extra);
				stopVideoProgressTimer();
				return true;
			}
		});

		setInfoListenerEnabled(_infoListenerEnabled);

		if(timeout > 0) {
			startPrepareTimer((long) timeout);
		}

		try {
			setVideoPath(_videoUrl);
		}
		catch (Exception e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PREPARE_ERROR, _videoUrl);
			DeviceLog.exception("Error preparing video: " + _videoUrl, e);
			return false;
		}

		return true;
	}

	public void play () {
		DeviceLog.entered();

		setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (mp != null) {
					_mediaPlayer = mp;
				}

				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.COMPLETED, _videoUrl);
				stopVideoProgressTimer();
			}
		});

		start();
		stopVideoProgressTimer();
		startVideoProgressTimer();

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PLAY, _videoUrl);
	}

	public void setInfoListenerEnabled (boolean enabled) {
		_infoListenerEnabled = enabled;
		if (Build.VERSION.SDK_INT > 16) {
			if (_infoListenerEnabled) {
				setOnInfoListener(new MediaPlayer.OnInfoListener() {
					@Override
					public boolean onInfo(MediaPlayer mp, int what, int extra) {
						WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.INFO, _videoUrl, what, extra);
						return true;
					}
				});
			} else {
				setOnInfoListener(null);
			}
		}
	}

	public void pause () {
		try {
			super.pause();
		}
		catch (Exception e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PAUSE_ERROR, _videoUrl);
			DeviceLog.exception("Error pausing video", e);
			return;
		}

		stopVideoProgressTimer();
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PAUSE, _videoUrl);
	}

	@Override
	public void seekTo(int msec) {
		try {
			super.seekTo(msec);
		}
		catch (Exception e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.SEEKTO_ERROR, _videoUrl);
			DeviceLog.exception("Error seeking video", e);
			return;
		}

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.SEEKTO, _videoUrl);
	}

	public void stop () {
		stopPlayback();
		stopVideoProgressTimer();
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.STOP, _videoUrl);
	}

	public float getVolume () {
		return _volume;
	}

	public void setVolume (Float volume) {
		try {
			_mediaPlayer.setVolume(volume, volume);
			_volume = volume;
		}
		catch (Exception e) {
			DeviceLog.exception("MediaPlayer generic error", e);
			return;
		}
	}

	public void setProgressEventInterval (int ms) {
		_progressEventInterval = ms;
		if(_videoTimer != null) {
			stopVideoProgressTimer();
			startVideoProgressTimer();
		}
	}

	public int getProgressEventInterval () {
		return _progressEventInterval;
	}
}
