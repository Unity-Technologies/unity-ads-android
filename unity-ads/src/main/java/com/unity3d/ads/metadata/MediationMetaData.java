package com.unity3d.ads.metadata;

import android.content.Context;

public class MediationMetaData extends MetaData {
	public final static String KEY_ORDINAL = "ordinal";
	public final static String KEY_MISSED_IMPRESSION_ORDINAL = "missedImpressionOrdinal";
	public final static String KEY_NAME = "name";
	public final static String KEY_VERSION = "version";

	public MediationMetaData(Context context) {
		super(context);
		setCategory("mediation");
	}

	public void setOrdinal(int ordinal) {
		set(KEY_ORDINAL, ordinal);
	}

	public void setMissedImpressionOrdinal(int ordinal) {
		set(KEY_MISSED_IMPRESSION_ORDINAL, ordinal);
	}

	public void setName (String mediationNetworkName) {
		set(KEY_NAME, mediationNetworkName);
	}

	public void setVersion (String mediationSdkVersion) {
		set(KEY_VERSION, mediationSdkVersion);
	}
}
