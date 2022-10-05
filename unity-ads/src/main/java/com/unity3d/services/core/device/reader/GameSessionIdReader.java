package com.unity3d.services.core.device.reader;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.GAME_SESSION_ID_NORMALIZED_KEY;

import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.properties.ClientProperties;

import java.util.UUID;

public class GameSessionIdReader implements IGameSessionIdReader {

	private static final int GAME_SESSION_ID_LENGTH = 12;

	private static volatile GameSessionIdReader _instance;

	private Long gameSessionId;

	private GameSessionIdReader() {}

	public static GameSessionIdReader getInstance() {
		if (_instance == null) {
			synchronized (GameSessionIdReader.class) {
				if (_instance == null) {
					_instance = new GameSessionIdReader();
				}
			}
		}

		return _instance;
	}

	@Override
	public synchronized Long getGameSessionId() {
		if (gameSessionId == null) {
			generate();
		}
		return gameSessionId;
	}

	@Override
	public synchronized Long getGameSessionIdAndStore() {
		if (gameSessionId == null) {
			generate();
			store();
		}
		return gameSessionId;
	}

	private void generate() {
		UUID id = UUID.randomUUID();
		String numericUUID = Long.toString(id.getMostSignificantBits())
			+ Long.toString(id.getLeastSignificantBits());
		gameSessionId = Long.valueOf(numericUUID
			.replace("-", "")
			.substring(0, GAME_SESSION_ID_LENGTH));
	}


	private void store() {
		if (StorageManager.init(ClientProperties.getApplicationContext())) {
			Storage storage = StorageManager.getStorage(StorageManager.StorageType.PRIVATE);
			if (storage != null) {
				storage.set(GAME_SESSION_ID_NORMALIZED_KEY, gameSessionId);
				storage.writeStorage();
			}
		}
	}

}
