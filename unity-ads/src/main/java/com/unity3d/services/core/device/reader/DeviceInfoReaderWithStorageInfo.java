package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.misc.IJsonStorageReader;
import com.unity3d.services.core.misc.JsonFlattener;
import com.unity3d.services.core.misc.JsonStorageAggregator;
import com.unity3d.services.core.misc.Utilities;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeviceInfoReaderWithStorageInfo implements IDeviceInfoReader {

	private final IDeviceInfoReader _deviceInfoReader;
	private final List<IJsonStorageReader> _storageReaders;

	private static final List<String> _includedKeys = Arrays.asList(
		"privacy",
		"gdpr",
		"framework",
		"adapter",
		"mediation",
		"unity",
		"pipl",
		"configuration",
		"user",
		"unifiedconfig"
	);

	private static final List<String> _blackListOfKeys = Arrays.asList(
		"ts",
		"exclude",
		"pii",
		"nonBehavioral",
		"nonbehavioral"
	);

	public DeviceInfoReaderWithStorageInfo(IDeviceInfoReader deviceInfoReader, IJsonStorageReader... storageReaders) {
		_deviceInfoReader = deviceInfoReader;
		_storageReaders = Arrays.asList(storageReaders);
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> deviceInfoData = _deviceInfoReader.getDeviceInfoData();
		if (deviceInfoData != null) {
			JsonStorageAggregator jsonStorageAggregator = new JsonStorageAggregator(_storageReaders);
			JSONObject aggregatedData = jsonStorageAggregator.getData();
			JsonFlattener jsonFlattener = new JsonFlattener(aggregatedData);
			JSONObject resultingJson = jsonFlattener.flattenJson(".", _includedKeys, Collections.singletonList("value"), _blackListOfKeys);
			deviceInfoData = Utilities.combineJsonIntoMap(deviceInfoData, resultingJson);
		}
		return deviceInfoData;
	}


}
