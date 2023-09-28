package com.unity3d.services.core.device.reader;

import static android.view.Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION;
import static android.view.Display.HdrCapabilities.HDR_TYPE_HDR10;
import static android.view.Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS;
import static android.view.Display.HdrCapabilities.HDR_TYPE_HLG;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

import com.unity3d.services.core.configuration.ExperimentsReader;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HdrInfoReader implements IHdrInfoReader {

	private final SDKMetricsSender _sdkMetricsSender = Utilities.getService(SDKMetricsSender.class);
	private static final AtomicBoolean _hdrMetricsCaptured = new AtomicBoolean(false);

	private static volatile HdrInfoReader _instance;

	private HdrInfoReader() {}

	public static HdrInfoReader getInstance() {
		if (_instance == null) {
			synchronized (HdrInfoReader.class) {
				if (_instance == null) {
					_instance = new HdrInfoReader();
				}
			}
		}

		return _instance;
	}

	@Override
	public void captureHDRCapabilityMetrics(Activity activity, ExperimentsReader experimentsReader) {
		if (activity == null) return;

		if (!experimentsReader.getCurrentlyActiveExperiments().isCaptureHDRCapabilitiesEnabled()) return;

		if (_hdrMetricsCaptured.compareAndSet(false, true)) {

			List<Metric> hdrMetrics = new ArrayList<>(5);

			int hasDolbyVision = 0;
			int hasHDR10 = 0;
			int hasHDR10Plus = 0;
			int hasHLG = 0;
			int isScreenHDR = 0;

			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
				Display display = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				Display.HdrCapabilities hdrCapabilities = display.getHdrCapabilities();

				int[] types = hdrCapabilities.getSupportedHdrTypes();
				for (int type : types) {
					switch (type) {
						case HDR_TYPE_DOLBY_VISION:
							hasDolbyVision = 1;
							break;
						case HDR_TYPE_HDR10:
							hasHDR10 = 1;
							break;
						case HDR_TYPE_HLG:
							hasHLG = 1;
							break;
						case HDR_TYPE_HDR10_PLUS:
							hasHDR10Plus = 1;
							break;
					}
				}

				long maxAverage = Math.round(hdrCapabilities.getDesiredMaxAverageLuminance());
				long maxLum = Math.round(hdrCapabilities.getDesiredMaxLuminance());
				long minLum = Math.round(hdrCapabilities.getDesiredMinLuminance());

				hdrMetrics.add(new Metric("native_device_hdr_lum_max_average", maxAverage));
				hdrMetrics.add(new Metric("native_device_hdr_lum_max", maxLum));
				hdrMetrics.add(new Metric("native_device_hdr_lum_min", minLum));

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
					Configuration configuration = activity.getResources().getConfiguration();
					isScreenHDR = configuration.isScreenHdr() ? 1 : 0;
				}
			}

			hdrMetrics.add(new Metric("native_device_hdr_dolby_vision", hasDolbyVision));
			hdrMetrics.add(new Metric("native_device_hdr_hdr10", hasHDR10));
			hdrMetrics.add(new Metric("native_device_hdr_hdr10_plus", hasHDR10Plus));
			hdrMetrics.add(new Metric("native_device_hdr_hlg", hasHLG));
			hdrMetrics.add(new Metric("native_device_hdr_screen_hdr", isScreenHDR));

			_sdkMetricsSender.sendMetrics(hdrMetrics);
		}
	}

}