package com.unity3d.services.monetization.core.api;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.monetization.placementcontent.core.PlacementContent;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.monetization.core.placementcontent.PlacementContentResultFactory;
import com.unity3d.services.monetization.core.utilities.JSONUtilities;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class PlacementContentsTest {
    @Test
    public void createPlacementContentShouldCreateAPlacementContentAndPlaceItIntoThePlacementContentsMap() throws Exception {
        WebViewCallback callback = mock(WebViewCallback.class);
        PlacementContents.createPlacementContent(Fixture.placementID, JSONUtilities.mapToJsonObject(Fixture.params), callback);

        verify(callback).invoke();
        PlacementContent placementContent = com.unity3d.services.monetization.core.placementcontent.PlacementContents.getPlacementContent(Fixture.placementID);
        assertNotNull("PlacementContent was null", placementContent);
        assertEquals("Types were different", placementContent.getType(), "SHOW_AD");
    }

    private static class Fixture {
        public static String placementID = "myPlacement";
        public static Map<String, Object> params = new HashMap<>();

        static {
            params.put("type", PlacementContentResultFactory.PlacementContentResultType.SHOW_AD);
            params.put("foo", "Bar");
        }
    }
}
