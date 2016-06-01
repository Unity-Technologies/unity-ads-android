package com.unity3d.ads.webview.bridge;

import android.os.Parcel;
import android.os.Parcelable;

import com.unity3d.ads.log.DeviceLog;

import java.util.ArrayList;
import java.util.Arrays;

public class WebViewCallback implements Parcelable {
	private boolean _invoked;
	private int _invocationId;
	private String _callbackId;

	public WebViewCallback(String callbackId, int invocationId) {
		_callbackId = callbackId;
		_invocationId = invocationId;
	}

	public WebViewCallback(Parcel in) {
		_callbackId = in.readString();
		_invoked = in.readByte() != 0;
		_invocationId = in.readInt();
	}

	public void invoke(Object... params) {
		invoke(CallbackStatus.OK, null, params);
	}

	private void invoke (CallbackStatus status, Enum error, Object... params) {
		if (_invoked || _callbackId == null || _callbackId.length() == 0) return;

		_invoked = true;

		ArrayList<Object> paramList = new ArrayList<>();
		paramList.addAll(Arrays.asList(params));
		paramList.add(0, _callbackId);

		Invocation invocation = Invocation.getInvocationById(_invocationId);

		if (invocation == null) {
			DeviceLog.error("Couldn't get batch with id: " + getInvocationId());
			return;
		}

		invocation.setInvocationResponse(status, error, paramList.toArray());
	}

	public void error(Enum error, Object... params) {
		invoke(CallbackStatus.ERROR, error, params);
	}

	public int getInvocationId () {
		return _invocationId;
	}

	public String getCallbackId () {
		return _callbackId;
	}

	@Override
	public int describeContents() {
		return 45678;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(_callbackId);
		dest.writeByte((byte)(_invoked ? 1 : 0));
		dest.writeInt(_invocationId);
	}

	public static final Parcelable.Creator<WebViewCallback> CREATOR  = new Parcelable.Creator<WebViewCallback>() {

		@Override
		public WebViewCallback createFromParcel(Parcel in) {
			return new WebViewCallback(in);
		}

		@Override
		public WebViewCallback[] newArray(int size) {
			return new WebViewCallback[size];
		}
	};
}
