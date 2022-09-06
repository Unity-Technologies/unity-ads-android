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
	private IExperiments _localExperiments;
	private IExperiments _remoteExperiments;


	public ExperimentsReader() {}

	public synchronized void updateLocalExperiments(IExperiments localExperiments) {
		_localExperiments = localExperiments;
	}

	public synchronized void updateRemoteExperiments(IExperiments remoteExperiments) {
		_remoteExperiments = remoteExperiments;
	}

	public synchronized IExperiments getCurrentlyActiveExperiments() {
		if (_remoteExperiments == null && _localExperiments == null) return new Experiments();
		if (_remoteExperiments == null) {
			return _localExperiments;
		} else {
			if (_localExperiments == null) {
				// If we set remote before we have any local experiments, simply use defaults for local experiments
				_localExperiments = new Experiments();
			}
			JSONObject localActiveExp = _localExperiments.getNextSessionExperiments();
			JSONObject remoteActiveExp = _remoteExperiments.getCurrentSessionExperiments();
			try {
				return new Experiments(Utilities.mergeJsonObjects(localActiveExp, remoteActiveExp));
			} catch (JSONException e) {
				DeviceLog.error("Couldn't get active experiments, reverting to default experiments");
				return new Experiments();
			}
		}
	}
}
