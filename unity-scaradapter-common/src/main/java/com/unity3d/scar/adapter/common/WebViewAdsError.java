package com.unity3d.scar.adapter.common;

public class WebViewAdsError implements IUnityAdsError {
	protected String _description;
	private Enum _errorCategory;
	protected Object[] _errorArguments;

	public WebViewAdsError(Enum<?> errorCategory, String description, Object... errorArguments) {
		_errorCategory = errorCategory;
		_description = description;
		_errorArguments = errorArguments;
	}

	@Override
	public String getDomain() {
		return null;
	}

	@Override
	public String getDescription() {
		return _description;
	}

	@Override
	public int getCode() {
		return -1;
	}

	public Enum<?> getErrorCategory() {
		return _errorCategory;
	}

	public Object[] getErrorArguments() { return _errorArguments; }

}
