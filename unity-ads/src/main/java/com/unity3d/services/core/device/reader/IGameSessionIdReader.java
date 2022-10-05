package com.unity3d.services.core.device.reader;

public interface IGameSessionIdReader {
	Long getGameSessionId();
	Long getGameSessionIdAndStore();
}
