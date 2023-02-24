package com.unity3d.services.ads.gmascar.managers;

public enum SCARBiddingManagerType {
	DISABLED(Constants.DIS),
	EAGER(Constants.EAG),
	LAZY(Constants.LAZ),
	HYBRID(Constants.HYB);

	private final String name;

	SCARBiddingManagerType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public static SCARBiddingManagerType fromName(final String name) {
		switch(name) {
			case Constants.EAG:
				return EAGER;
			case Constants.LAZ:
				return LAZY;
			case Constants.HYB:
				return HYBRID;
			case Constants.DIS:
			default:
				return DISABLED;
		}
	}

	private static class Constants {
		private static final String LAZ = "laz";
		private static final String EAG = "eag";
		private static final String HYB = "hyb";
		private static final String DIS = "dis";
	}
}
