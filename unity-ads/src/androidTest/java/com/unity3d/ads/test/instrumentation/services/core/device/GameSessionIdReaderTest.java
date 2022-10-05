package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.GAME_SESSION_ID_NORMALIZED_KEY;

import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.device.reader.GameSessionIdReader;
import com.unity3d.services.core.properties.ClientProperties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GameSessionIdReaderTest {

	@Before
	public void setup() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
	}

	@Test
	public void testGameSessionIdReader() {
		Long gameSessionId = GameSessionIdReader.getInstance().getGameSessionIdAndStore();

		// validate we get a gameSessionId
		Assert.assertNotNull(gameSessionId);
		Assert.assertEquals(12, (Long.toString(gameSessionId)).length()) ;
		// validate we store the value
		StorageManager.init(InstrumentationRegistry.getInstrumentation().getTargetContext());
		StorageManager.initStorage(StorageManager.StorageType.PRIVATE);
		Storage storage = StorageManager.getStorage(StorageManager.StorageType.PRIVATE);
		Long storedId = null;
		if (storage != null) {
			storedId = (Long) storage.get(GAME_SESSION_ID_NORMALIZED_KEY);
		}
		Assert.assertEquals(gameSessionId, storedId);
		// validate on consecutive requests we get the same value
		Long gameSessionId2 = GameSessionIdReader.getInstance().getGameSessionId();
		Assert.assertEquals(gameSessionId, gameSessionId2);
		// validate even on storage change we get same value
		if (storage != null) {
			storage.set(GAME_SESSION_ID_NORMALIZED_KEY, "newValueFromWebview");
			storage.writeStorage();
		}
		Long gameSessionId3 =  GameSessionIdReader.getInstance().getGameSessionId();
		Assert.assertEquals(gameSessionId, gameSessionId3);
	}


}
