package com.unity3d.ads.device;

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

import com.unity3d.ads.log.DeviceLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class AdvertisingId {

	private static final String ADVERTISING_ID_SERVICE_NAME = "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService";

	private static AdvertisingId instance = null;
	private String advertisingIdentifier = null;
	private boolean limitedAdvertisingTracking = false;

	private static AdvertisingId getInstance() {
		if (instance == null) {
			instance = new AdvertisingId();
		}
		return instance;
	}

	public static void init(final Context context) {
		getInstance().fetchAdvertisingId(context);
	}

	public static String getAdvertisingTrackingId() {
		return getInstance().advertisingIdentifier;
	}

	public static boolean getLimitedAdTracking() {
		return getInstance().limitedAdvertisingTracking;
	}


	private void fetchAdvertisingId(Context context) {
		GoogleAdvertisingServiceConnection connection = new GoogleAdvertisingServiceConnection();
		Intent localIntent = new Intent("com.google.android.gms.ads.identifier.service.START");
		localIntent.setPackage("com.google.android.gms");
		boolean didBind = false;
		try {
			didBind = context.bindService(localIntent, connection, Context.BIND_AUTO_CREATE);
		} catch (Exception e) {
			DeviceLog.exception("Couldn't bind to identifier service intent", e);
		}
		try {
			if (didBind) {
				GoogleAdvertisingInfo advertisingInfo = GoogleAdvertisingInfo.GoogleAdvertisingInfoBinder.create(connection.getBinder());
				advertisingIdentifier = advertisingInfo.getId();
				limitedAdvertisingTracking = advertisingInfo.getEnabled(true);
			}
		} catch (Exception e) {
			DeviceLog.exception("Couldn't get advertising info", e);
		} finally {
			if (didBind) {
				context.unbindService(connection);
			}
		}
	}

	private interface GoogleAdvertisingInfo extends IInterface {

		String getId() throws RemoteException;

		boolean getEnabled(boolean paramBoolean) throws RemoteException;

		abstract class GoogleAdvertisingInfoBinder extends Binder implements GoogleAdvertisingInfo {

			public static GoogleAdvertisingInfo create(IBinder binder) {
				if (binder == null) {
					return null;
				}
				IInterface localIInterface = binder.queryLocalInterface(ADVERTISING_ID_SERVICE_NAME);
				if ((localIInterface != null) && ((localIInterface instanceof GoogleAdvertisingInfo))) {
					return (GoogleAdvertisingInfo) localIInterface;
				}
				return new GoogleAdvertisingInfoImplementation(binder);
			}

			@SuppressWarnings("NullableProblems")
			public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
				switch (code) {
					case 1:
						data.enforceInterface(ADVERTISING_ID_SERVICE_NAME);
						String str1 = getId();
						reply.writeNoException();
						reply.writeString(str1);
						return true;
					case 2:
						data.enforceInterface(ADVERTISING_ID_SERVICE_NAME);
						boolean bool1 = 0 != data.readInt();
						boolean bool2 = getEnabled(bool1);
						reply.writeNoException();
						reply.writeInt(bool2 ? 1 : 0);
						return true;
				}
				return super.onTransact(code, data, reply, flags);
			}

			private static class GoogleAdvertisingInfoImplementation implements GoogleAdvertisingInfo {

				private final IBinder _binder;

				GoogleAdvertisingInfoImplementation(IBinder binder) {
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
						localParcel1.writeInterfaceToken(ADVERTISING_ID_SERVICE_NAME);
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
						localParcel1.writeInterfaceToken(ADVERTISING_ID_SERVICE_NAME);
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

	private class GoogleAdvertisingServiceConnection implements ServiceConnection {

		boolean _consumed = false;
		private final BlockingQueue<IBinder> _binderQueue = new LinkedBlockingQueue<>();

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				_binderQueue.put(service);
			} catch (InterruptedException localInterruptedException) {
				DeviceLog.debug("Couldn't put service to binder que");
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