package com.unity3d.services.core.device;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import com.unity3d.services.core.log.DeviceLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class OpenAdvertisingId {

	private static final String HW_OPEN_ADVERTISING_ID_SERVICE_NAME = "com.uodis.opendevice.aidl.OpenDeviceIdentifierService";
	private static final String HW_DEVICE_NAME = "HUAWEI";

	private static OpenAdvertisingId instance = null;
	private String openAdvertisingIdentifier = null;
	private boolean limitedOpenAdTracking = false;

	private static OpenAdvertisingId getInstance() {
		if (instance == null) {
			instance = new OpenAdvertisingId();
		}
		return instance;
	}

	public static void init(final Context context) {
		if(Build.MANUFACTURER.toUpperCase().equals(HW_DEVICE_NAME)) {
			getInstance().fetchOAId(context);
		}
	}

	public static String getOpenAdvertisingTrackingId() {
		return getInstance().openAdvertisingIdentifier;
	}

	public static boolean getLimitedOpenAdTracking() {
		return getInstance().limitedOpenAdTracking;
	}


	private void fetchOAId(Context context) {
		HWAdvertisingServiceConnection connection = new HWAdvertisingServiceConnection();
		Intent localIntent = new Intent("com.uodis.opendevice.OPENIDS_SERVICE");
		localIntent.setPackage("com.huawei.hwid");
		try {
			if (!context.bindService(localIntent, connection, Context.BIND_AUTO_CREATE)) {
				return;
			}
		} catch (Exception e) {
			DeviceLog.exception("Couldn't bind to identifier service intent", e);
			return;
		}
		try {
			HWAdvertisingInfo advertisingInfo = HWAdvertisingInfo.HWAdvertisingInfoBinder.create(connection.getBinder());
			openAdvertisingIdentifier = advertisingInfo.getId();
			limitedOpenAdTracking = advertisingInfo.getEnabled(true);
		} catch (Exception e) {
			DeviceLog.exception("Couldn't get openAdvertising info", e);
		} finally {
			context.unbindService(connection);
		}
	}

	private interface HWAdvertisingInfo extends IInterface {

		String getId() throws RemoteException;

		boolean getEnabled(boolean paramBoolean) throws RemoteException;

		abstract class HWAdvertisingInfoBinder extends Binder implements HWAdvertisingInfo {

			public static HWAdvertisingInfo create(IBinder binder) {
				if (binder == null) {
					return null;
				}
				IInterface localIInterface = binder.queryLocalInterface(HW_OPEN_ADVERTISING_ID_SERVICE_NAME);
				if ((localIInterface != null) && ((localIInterface instanceof HWAdvertisingInfo))) {
					return (HWAdvertisingInfo) localIInterface;
				}
				return new HWAdvertisingInfoImplementation(binder);
			}

			@SuppressWarnings("NullableProblems")

			public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
				switch (code) {
					case 1:
						data.enforceInterface(HW_OPEN_ADVERTISING_ID_SERVICE_NAME);
						String str1 = getId();
						reply.writeNoException();
						reply.writeString(str1);
						return true;
					case 2:
						data.enforceInterface(HW_OPEN_ADVERTISING_ID_SERVICE_NAME);
						boolean bool1 = 0 != data.readInt();
						boolean bool2 = getEnabled(bool1);
						reply.writeNoException();
						reply.writeInt(bool2 ? 1 : 0);
						return true;
				}
				return super.onTransact(code, data, reply, flags);
			}

			private static class HWAdvertisingInfoImplementation implements HWAdvertisingInfo {

				private final IBinder _binder;

				HWAdvertisingInfoImplementation(IBinder binder) {
					_binder = binder;
				}

				@Override
				public IBinder asBinder() {
					return _binder;
				}

				@Override
				public String getId() throws RemoteException {
					Parcel localParcel1 = Parcel.obtain();
					Parcel localParcel2 = Parcel.obtain();
					String str;
					try {
						localParcel1.writeInterfaceToken(HW_OPEN_ADVERTISING_ID_SERVICE_NAME);
						_binder.transact(1, localParcel1, localParcel2, 0);
						localParcel2.readException();
						str = localParcel2.readString();
					} finally {
						localParcel2.recycle();
						localParcel1.recycle();
					}
					return str;
				}

				@Override
				public boolean getEnabled(boolean paramBoolean) throws RemoteException {
					Parcel localParcel1 = Parcel.obtain();
					Parcel localParcel2 = Parcel.obtain();
					boolean bool;
					try {
						localParcel1.writeInterfaceToken(HW_OPEN_ADVERTISING_ID_SERVICE_NAME);
						localParcel1.writeInt(paramBoolean ? 1 : 0);
						_binder.transact(2, localParcel1, localParcel2, 0);
						localParcel2.readException();
						bool = 0 != localParcel2.readInt();
					} finally {
						localParcel2.recycle();
						localParcel1.recycle();
					}
					return bool;
				}
			}
		}
	}

	private class HWAdvertisingServiceConnection implements ServiceConnection {

		boolean _consumed = false;
		private final BlockingQueue<IBinder> _binderQueue = new LinkedBlockingQueue<>();

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				_binderQueue.put(service);
			} catch (InterruptedException localInterruptedException) {
				DeviceLog.debug("Couldn't put service to binder que");
				Thread.currentThread().interrupt();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		public IBinder getBinder() throws InterruptedException {
			if (_consumed) {
				throw new IllegalStateException();
			}
			_consumed = true;
			return _binderQueue.take();
		}
	}
}
