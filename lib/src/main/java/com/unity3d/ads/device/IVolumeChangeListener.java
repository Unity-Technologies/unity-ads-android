package com.unity3d.ads.device;

public interface IVolumeChangeListener {
	void onVolumeChanged(int volume);
	int getStreamType();
}
