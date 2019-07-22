package com.unity3d.services.ads.adunit;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.unity3d.services.ads.api.VideoPlayer;
import com.unity3d.services.ads.video.VideoPlayerView;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.ViewUtilities;

public class VideoPlayerHandler implements IAdUnitViewHandler {
	private RelativeLayout _videoContainer;
	private VideoPlayerView _videoView;

	public boolean create(AdUnitActivity activity) {
		DeviceLog.entered();
		if (_videoContainer == null) {
			_videoContainer = new RelativeLayout(activity);
		}

		if (_videoView == null) {
			_videoView = new VideoPlayerView(activity);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			_videoView.setLayoutParams(params);
			_videoContainer.addView(_videoView);
			VideoPlayer.setVideoPlayerView(_videoView);
		}
		return true;
	}

	public boolean destroy() {
		DeviceLog.entered();

		if (_videoView != null) {
			_videoView.stopVideoProgressTimer();
			_videoView.stopPlayback();
			ViewUtilities.removeViewFromParent(_videoView);

			if (_videoView.equals(VideoPlayer.getVideoPlayerView())) {
				VideoPlayer.setVideoPlayerView(null);
			}

			_videoView = null;
		}

		if (_videoContainer != null) {
			ViewUtilities.removeViewFromParent(_videoContainer);
			_videoContainer = null;
		}

		return true;
	}

	public View getView() {
		return _videoContainer;
	}

	public void onCreate(AdUnitActivity activity, Bundle savedInstanceState) {
		create(activity);
	}

	public void onStart(AdUnitActivity activity) {
	}

	public void onStop(AdUnitActivity activity) {
	}

	public void onResume(AdUnitActivity activity) {
	}

	public void onPause(AdUnitActivity activity) {
		destroy();
	}

	public void onDestroy(AdUnitActivity activity) {
	}
}
