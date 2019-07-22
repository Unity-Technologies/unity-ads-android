package com.unity3d.ads.test.unit.services.ads.load;

import com.unity3d.services.ads.load.ILoadBridge;

import java.util.ArrayList;
import java.util.Map;

public class LoadBridgeMock implements ILoadBridge {

	public ArrayList<Map<String, Integer>> callList = new ArrayList<>();

	public Runnable loadPlacementsBlock;

	public void loadPlacements(Map<String, Integer> placements) {
		this.callList.add(placements);
		if (loadPlacementsBlock != null) {
			loadPlacementsBlock.run();
		}
	}

}
