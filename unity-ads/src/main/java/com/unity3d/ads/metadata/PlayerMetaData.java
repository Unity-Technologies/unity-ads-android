package com.unity3d.ads.metadata;

import android.content.Context;

public class PlayerMetaData extends MetaData {
	public static final String KEY_SERVER_ID = "server_id";

	public PlayerMetaData (Context context) {
		super(context);
		setCategory("player");
	}

	public void setServerId (String serverId) {
		set(KEY_SERVER_ID, serverId);
	}
}
