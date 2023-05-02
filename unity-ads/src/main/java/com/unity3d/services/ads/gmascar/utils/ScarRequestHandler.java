package com.unity3d.services.ads.gmascar.utils;

import static com.unity3d.services.ads.gmascar.utils.ScarConstants.IDFI_KEY;
import static com.unity3d.services.ads.gmascar.utils.ScarConstants.TOKEN_ID_KEY;

import com.unity3d.services.ads.gmascar.models.BiddingSignals;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.network.core.HttpClient;
import com.unity3d.services.core.network.model.HttpRequest;

import com.unity3d.services.core.network.model.RequestType;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScarRequestHandler {
	private final HttpClient httpClient = Utilities.getService(HttpClient.class);

	public ScarRequestHandler() {
	}

	public void makeUploadRequest(String tokenIdentifier, BiddingSignals signals, String url) throws Exception {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("Content-Type", Collections.singletonList("application/json"));

		Map<String, String> body = new HashMap<>();
		body.put(IDFI_KEY, Device.getIdfi());
		body.put(TOKEN_ID_KEY, tokenIdentifier);
		body.putAll(signals.getMap());

		HttpRequest request = new HttpRequest(
			url,
			"",
			RequestType.POST,
			new JSONObject(body).toString().getBytes(StandardCharsets.UTF_8),
			headers
		);
		httpClient.executeBlocking(request);
	}
}
