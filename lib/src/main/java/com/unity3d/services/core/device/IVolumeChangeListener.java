package com.unity3d.services.core.device;

public interface IVolumeChangeListener {
	void onVolumeChanged(int volume);
	int getStreamType();
}
