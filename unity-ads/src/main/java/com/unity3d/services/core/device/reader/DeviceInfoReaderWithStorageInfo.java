package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.misc.IJsonStorageReader;
import com.unity3d.services.core.misc.JsonFlattener;
import com.unity3d.services.core.misc.JsonFlattenerRules;
import com.unity3d.services.core.misc.JsonStorageAggregator;
import com.unity3d.services.core.misc.Utilities;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DeviceInfoReaderWithStorageInfo implements IDeviceInfoReader {

	private final IDeviceInfoReader _deviceInfoReader;
	private final List<IJsonStorageReader> _storageReaders;
	private final JsonFlattenerRules _jsonFlattenerRules;

	public DeviceInfoReaderWithStorageInfo(IDeviceInfoReader deviceInfoReader, JsonFlattenerRules jsonFlattenerRules, IJsonStorageReader... storageReaders) {
		_deviceInfoReader = deviceInfoReader;
		_jsonFlattenerRules = jsonFlattenerRules;
		_storageReaders = Arrays.asList(storageReaders);
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> deviceInfoData = _deviceInfoReader.getDeviceInfoData();
		if (deviceInfoData != null) {
			JsonStorageAggregator jsonStorageAggregator = new JsonStorageAggregator(_storageReaders);
			JSONObject aggregatedData = jsonStorageAggregator.getData();
			JsonFlattener jsonFlattener = new JsonFlattener(aggregatedData);
			JSONObject resultingJson = jsonFlattener.flattenJson(".", _jsonFlattenerRules);
			deviceInfoData = Utilities.combineJsonIntoMap(deviceInfoData, resultingJson);
		}
		return deviceInfoData;
	}


}
