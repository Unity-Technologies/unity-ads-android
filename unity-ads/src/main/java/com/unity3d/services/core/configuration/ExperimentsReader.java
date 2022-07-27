package com.unity3d.services.core.configuration;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ExperimentsReader {
	private Experiments _localExperiments;
	private Experiments _remoteExperiments;

	private static final Set<String> NEXT_SESSION_FLAGS = new HashSet<>(Arrays.asList("tsi", "tsi_upii", "tsi_p", "tsi_nt", "tsi_prr", "tsi_prw"));

	public ExperimentsReader() {}

	public synchronized void updateLocalExperiments(Experiments localExperiments) {
		_localExperiments = localExperiments;
	}

	public synchronized void updateRemoteExperiments(Experiments remoteExperiments) {
		_remoteExperiments = remoteExperiments;
	}

	public synchronized Experiments getCurrentlyActiveExperiments() {
		if (_remoteExperiments == null && _localExperiments == null) return new Experiments();
		if (_remoteExperiments == null) {
			return _localExperiments;
		} else {
			if (_localExperiments == null) {
				// If we set remote before we have any local experiments, simply use defaults for local experiments
				_localExperiments = new Experiments();
			}
			// Use local "tsi*" + remote for other experiments
			JSONObject localActiveExp = getNextSessionFlags(_localExperiments.getExperimentData());
			JSONObject remoteActiveExp = getCurrentSessionFlags(_remoteExperiments.getExperimentData());
			try {
				return new Experiments(Utilities.mergeJsonObjects(localActiveExp, remoteActiveExp));
			} catch (JSONException e) {
				DeviceLog.error("Couldn't get active experiments, reverting to default experiments");
				return new Experiments();
			}
		}
	}

	private JSONObject getNextSessionFlags(JSONObject experimentData) {
		if (experimentData == null) return null;
		Map<String, String> nextSessionFlags = new HashMap<>();
		for (Iterator<String> it = experimentData.keys(); it.hasNext();) {
			String currentKey = it.next();
			if (NEXT_SESSION_FLAGS.contains(currentKey)) {
				nextSessionFlags.put(currentKey, String.valueOf(experimentData.optBoolean(currentKey)));
			}
		}
		return new JSONObject(nextSessionFlags);
	}

	private JSONObject getCurrentSessionFlags(JSONObject remoteExperiments) {
		if (remoteExperiments == null) return null;
		Map<String, String> currentSessionFlags = new HashMap<>();
		for (Iterator<String> it = remoteExperiments.keys(); it.hasNext();) {
			String currentKey = it.next();
			if (!NEXT_SESSION_FLAGS.contains(currentKey)) {
				currentSessionFlags.put(currentKey, String.valueOf(remoteExperiments.optBoolean(currentKey)));
			}
		}
		return new JSONObject(currentSessionFlags);
	}
}
