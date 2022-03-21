package com.unity3d.services.ads.token;

public interface INativeTokenGenerator {
	void generateToken(final INativeTokenGeneratorListener callback);
}
