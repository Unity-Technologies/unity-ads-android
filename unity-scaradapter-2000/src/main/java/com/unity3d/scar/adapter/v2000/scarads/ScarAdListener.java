package com.unity3d.scar.adapter.v2000.scarads;

import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;

public class ScarAdListener {
	protected IScarLoadListener _loadListener;

	public void setLoadListener(IScarLoadListener loadListener) {
		_loadListener = loadListener;
	}

}
