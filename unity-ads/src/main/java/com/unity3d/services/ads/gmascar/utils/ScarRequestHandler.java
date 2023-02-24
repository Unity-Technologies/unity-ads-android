package com.unity3d.services.ads.gmascar.utils;

import static com.unity3d.services.ads.gmascar.utils.ScarConstants.IDFI_KEY;
import static com.unity3d.services.ads.gmascar.utils.ScarConstants.TOKEN_ID_KEY;

import com.unity3d.services.ads.gmascar.models.BiddingSignals;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.request.WebRequest;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScarRequestHandler {

	public ScarRequestHandler() {};

	public void makeUploadRequest(String tokenIdentifier, BiddingSignals signals, String url) throws Exception {
			Map<String, List<String>> headers = new HashMap<>();
			headers.put("Content-Type",
				Collections.singletonList("application/json"));

			final WebRequest request = new WebRequest(
				url,
				"POST",
				headers);

			Map<String, String> body = new HashMap<>();
			body.put(IDFI_KEY, Device.getIdfi());
			body.put(TOKEN_ID_KEY, tokenIdentifier);
			body.putAll(signals.getMap());
			request.setBody(new JSONObject(body).toString());

			request.makeRequest();
	}
}
