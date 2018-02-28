package com.unity3d.ads.adunit;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class AdUnitRelativeLayout extends RelativeLayout {
	private final ArrayList<AdUnitMotionEvent> _motionEvents = new ArrayList<>();
	private int _maxEvents = 10000;
	private boolean _shouldCapture = false;

	public AdUnitRelativeLayout(Context context) {
		super(context);
	}

	@TargetApi(14)
	public boolean onInterceptTouchEvent(MotionEvent e) {
		super.onInterceptTouchEvent(e);

		if (_shouldCapture) {
			if (_motionEvents.size() < _maxEvents) {
				boolean isObscured = (e.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0;
				synchronized (_motionEvents) {
					_motionEvents.add(new AdUnitMotionEvent(e.getActionMasked(), isObscured, e.getToolType(0), e.getSource(), e.getDeviceId(), e.getX(0), e.getY(0), e.getEventTime(), e.getPressure(0), e.getSize(0)));
				}
			}
		}

		return false;
	}

	public void startCapture(int maxEvents) {
		_maxEvents = maxEvents;
		_shouldCapture = true;
	}

	public void endCapture() {
		_shouldCapture = false;
	}

	public void clearCapture() {
		synchronized (_motionEvents) {
			_motionEvents.clear();
		}
	}

	public int getMaxEventCount () {
		return _maxEvents;
	}

	public int getCurrentEventCount () {
		synchronized (_motionEvents) {
			return _motionEvents.size();
		}
	}

	public SparseArray<SparseArray<AdUnitMotionEvent>> getEvents(SparseArray<ArrayList<Integer>> requestedInfos) {
		SparseIntArray countTable = new SparseIntArray();
		SparseArray<SparseArray<AdUnitMotionEvent>> returnData = new SparseArray<>();

		synchronized (_motionEvents) {
			for (AdUnitMotionEvent currentEvent : _motionEvents) {
				ArrayList<Integer> currentRequestedInfos = requestedInfos.get(currentEvent.getAction());
				if (currentRequestedInfos != null) {
					int currentRequestedInfoIndex = currentRequestedInfos.get(0);

					if (countTable.get(currentEvent.getAction(), 0) == currentRequestedInfoIndex) {
						if (returnData.get(currentEvent.getAction()) == null) {
							returnData.put(currentEvent.getAction(), new SparseArray<AdUnitMotionEvent>());
						}

						returnData.get(currentEvent.getAction()).put(currentRequestedInfoIndex, currentEvent);
						currentRequestedInfos.remove(0);
					}

					countTable.put(currentEvent.getAction(), countTable.get(currentEvent.getAction()) + 1);
				}
			}
		}

		return returnData;
	}

	public SparseIntArray getEventCount(ArrayList<Integer> eventTypes) {
		SparseIntArray returnArray = new SparseIntArray();

		synchronized (_motionEvents) {
			for (AdUnitMotionEvent currentEvent : _motionEvents) {
				for (Integer currentType : eventTypes) {
					if (currentEvent.getAction() == currentType) {
						returnArray.put(currentType, returnArray.get(currentType, 0) + 1);
						break;
					}
				}
			}
		}

		return returnArray;
	}
}
