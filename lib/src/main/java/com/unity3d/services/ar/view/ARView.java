package com.unity3d.services.ar.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.SparseArray;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.unity3d.services.ar.AREvent;
import com.unity3d.services.ar.ARUtils;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class ARView extends GLSurfaceView implements GLSurfaceView.Renderer {
	private static final long FRAME_UPDATE_TIMEOUT = 500;

	private Session _session = null;

	private boolean _sessionRunning;
	private boolean _sessionWasRunning;
	private boolean _showCameraFrame;
	private boolean _drawNextCameraFrame;
	private long _timeOfLastDrawnCameraFrame;

	private float _arNear = 0.01f;
	private float _arFar = 10000.0f;

	private BackgroundRenderer _backgroundRenderer = new BackgroundRenderer();

	private DisplayRotationHelper _displayRotationHelper;

	private SparseArray<Plane> _detectedPlanes = new SparseArray<>();
	private Map<String, Anchor> _anchors = new HashMap<>();

	private boolean _shouldSendResize = false;

	// These are here so they don't create garbage every frame
	float[] _projectionMatrixArray = new float[16];
	float[] _viewMatrixArray = new float[16];
	float[] _orientationArray = new float[4];
	float[] _planeVertices = new float[12];
	JSONObject _frameInfo = new JSONObject();
	JSONArray _position = new JSONArray();
	JSONArray _orientation = new JSONArray();
	JSONArray _viewMatrix = new JSONArray();
	JSONArray _projectionMatrix = new JSONArray();
	JSONObject _lightEstimate = new JSONObject();
	float[] _planeMatrix = new float[16];

	public ARView(Context context) {
		super(context);

		if (Build.VERSION.SDK_INT > 11) {
			setPreserveEGLContextOnPause(true);
		}
		setEGLContextClientVersion(2);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		_displayRotationHelper = new DisplayRotationHelper(context);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (_session != null && _sessionWasRunning) {
			try {
				_session.resume();
				_sessionRunning = true;
			} catch (Exception e) {
				DeviceLog.error("Error resuming AR session: " + e.getMessage());
			}
		}

		_displayRotationHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (_session != null && _sessionRunning) {
			try {
				_sessionWasRunning = true;
				_sessionRunning = false;
				_session.pause();
			} catch (Exception e) {
				DeviceLog.error("Error pausing AR session: " + e.getMessage());
			}
		}

		_displayRotationHelper.onPause();
	}

	private void sendToWebView(AREvent eventType, Object... params) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		// Don't send events before webview is properly initialized
		if (webViewApp == null || !webViewApp.isWebAppLoaded()) {
			return;
		}

		webViewApp.sendEvent(WebViewEventCategory.AR, eventType, params);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		_backgroundRenderer.createOnGlThread();
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		_displayRotationHelper.onSurfaceChanged(width, height);

		sendToWebView(AREvent.AR_WINDOW_RESIZED, (float) width, (float) height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		if (_shouldSendResize) {
			// Fix for Samsung devices (S7-S9, manually send AR_WINDOW_RESIZED)
			WebViewApp webViewApp = WebViewApp.getCurrentApp();
			if (webViewApp != null) {
				float width = (float) webViewApp.getWebView().getWidth();
				float height = (float) webViewApp.getWebView().getHeight();
				if (width > 0 && height > 0) {
					sendToWebView(AREvent.AR_WINDOW_RESIZED, width, height);
				}
			}

			_shouldSendResize = false;
		}

		if (_sessionRunning) {
			_session.setCameraTextureName(_backgroundRenderer.getTextureId());
			_displayRotationHelper.updateSessionIfNeeded(_session);
			Frame frame;
			Camera camera;
			try {
				frame = _session.update();
				camera = frame.getCamera();
			} catch (CameraNotAvailableException e) {
				sendToWebView(AREvent.AR_ERROR, e.getMessage());
				return;
			} catch (NotYetAvailableException e) {
				sendToWebView(AREvent.AR_ERROR, e.getMessage());
				return;
			}

			if (!_showCameraFrame) {
				return;
			}
			_backgroundRenderer.draw(frame);

			long currentTime = System.currentTimeMillis();
			if (_timeOfLastDrawnCameraFrame == 0) {
				_timeOfLastDrawnCameraFrame = currentTime;
			}
			long timeSinceLastDrawnCameraFrame = currentTime - _timeOfLastDrawnCameraFrame;
			if (timeSinceLastDrawnCameraFrame < FRAME_UPDATE_TIMEOUT && !_drawNextCameraFrame) {
				return;
			}

			_timeOfLastDrawnCameraFrame = currentTime;
			_drawNextCameraFrame = false;

			camera.getProjectionMatrix(_projectionMatrixArray, 0, _arNear, _arFar);
			camera.getViewMatrix(_viewMatrixArray, 0);

			final float lightIntensity = frame.getLightEstimate().getPixelIntensity();

			Pose pose = camera.getDisplayOrientedPose();
			pose.getRotationQuaternion(_orientationArray, 0);

			try {
				_position.put(0, pose.tx());
				_position.put(1, pose.ty());
				_position.put(2, pose.tz());
				_frameInfo.put("position", _position);

				for (int i = 0; i < 4; i++) {
					_orientation.put(i, _orientationArray[i]);
				}
				_frameInfo.put("orientation", _orientation);

				for (int i = 0; i < 16; i++) {
					_viewMatrix.put(i, _viewMatrixArray[i]);
				}
				_frameInfo.put("viewMatrix", _viewMatrix);

				for (int i = 0; i < 16; i++) {
					_projectionMatrix.put(i, _projectionMatrixArray[i]);
				}
				_frameInfo.put("projectionMatrix", _projectionMatrix);

				_lightEstimate.put("ambientIntensity", lightIntensity);
				_lightEstimate.put("state", frame.getLightEstimate().getState().toString());
				_frameInfo.put("lightEstimate", _lightEstimate);

			} catch (JSONException ignored) {}

			sendToWebView(AREvent.AR_FRAME_UPDATED, _frameInfo.toString());

			Collection<Plane> updatedTrackables = frame.getUpdatedTrackables(Plane.class);
			updatePlanes(updatedTrackables);

			DeviceLog.debug(frame.toString());
		}
	}

	private void updatePlanes(Collection<Plane> planes) {
		if (planes.isEmpty()) {
			return;
		}

		JSONArray addedPlanesArray = new JSONArray();
		JSONArray updatedPlanesArray = new JSONArray();
		JSONArray removedPlanesArray = new JSONArray();
		for (Plane plane : planes) {
			try {
				JSONObject p = new JSONObject();
				plane.getCenterPose().toMatrix(_planeMatrix, 0);
				JSONArray planeMatrixArray = new JSONArray(_planeMatrix);
				p.put("modelMatrix", planeMatrixArray);
				p.put("identifier", Integer.toHexString(plane.hashCode()));
				JSONArray planeExtentArray = new JSONArray();
				planeExtentArray.put(plane.getExtentX());
				planeExtentArray.put(plane.getExtentZ());
				p.put("extent", planeExtentArray);
				getPlaneVertices(plane, _planeVertices);
				JSONArray planeVerticesArray = new JSONArray(_planeVertices);
				p.put("vertices", planeVerticesArray);
				p.put("alignment", plane.getType().ordinal());
				if (plane.getSubsumedBy() == null && _detectedPlanes.get(plane.hashCode()) == null) {
					_detectedPlanes.append(plane.hashCode(), plane);
					addedPlanesArray.put(p);
				} else {
					if (plane.getSubsumedBy() != null) {
						_detectedPlanes.delete(plane.hashCode());
						removedPlanesArray.put(plane);
					} else {
						updatedPlanesArray.put(p);
					}
				}
			} catch (JSONException e) {
				DeviceLog.error("Error updating AR planes: " + e.getMessage());
			}
		}

		if (addedPlanesArray.length() > 0) {
			sendToWebView(AREvent.AR_PLANES_ADDED, addedPlanesArray.toString());
		}

		if (updatedPlanesArray.length() > 0) {
			sendToWebView(AREvent.AR_PLANES_UPDATED, updatedPlanesArray.toString());
		}

		if (removedPlanesArray.length() > 0) {
			sendToWebView(AREvent.AR_PLANES_REMOVED, removedPlanesArray.toString());
		}
	}

	private static void getPlaneVertices(Plane plane, float[] planeVertices) {
		planeVertices[0] = plane.getExtentX() / 2;
		planeVertices[1] = 0;
		planeVertices[2] = plane.getExtentZ() / 2;

		planeVertices[3] = -plane.getExtentX() / 2;
		planeVertices[4] = 0;
		planeVertices[5] = plane.getExtentZ() / 2;

		planeVertices[6] = -plane.getExtentX() / 2;
		planeVertices[7] = 0;
		planeVertices[8] = -plane.getExtentZ() / 2;

		planeVertices[9] = plane.getExtentX() / 2;
		planeVertices[10] = 0;
		planeVertices[11] = -plane.getExtentZ() / 2;
	}

	public void restartSession(JSONObject properties) throws JSONException {
		if (_session == null) {
			try {
				_session = new Session(getContext());
				_shouldSendResize = true;
			} catch (Exception e) {
				DeviceLog.debug("Error creating ARCore session");
				return;
			}
		}

		JSONObject configProps = properties.getJSONObject("configuration");
		Config config = ARUtils.createConfiguration(configProps, _session);
		_session.configure(config);
		_session.resume();
		_sessionRunning = true;

		_displayRotationHelper.onResume();
	}

	public void pauseSession() {
		if (_sessionRunning) {
			_session.pause();
		}
	}

	public void setDrawNextCameraFrame() {
		_drawNextCameraFrame = true;
	}

	public boolean getShowCameraFrame() {
		return _showCameraFrame;
	}

	public void setShowCameraFrame(boolean showCameraFrame) {
		_showCameraFrame = showCameraFrame;
	}

	public float getArNear() {
		return _arNear;
	}

	public void setArNear(float arNear) {
		_arNear = arNear;
	}

	public float getArFar() {
		return _arFar;
	}

	public void setArFar(float arFar) {
		_arFar = arFar;
	}

	public void addAnchor(String identifier, String matrix) {
		if (_session == null) {
			DeviceLog.warning("Session is null. Not adding anchor.");
			return;
		}

		String[] floats = matrix.split(",");
		if (floats.length != 16) {
			DeviceLog.warning("Matrix doesn't have 16 elements. Not adding anchor.");
			return;
		}

		float[] anchorMatrix = new float[16];
		for (int i = 0; i < 16; i++) {
			try {
				anchorMatrix[i] = Float.parseFloat(floats[i]);
			} catch (NumberFormatException ignored) {
				DeviceLog.warning("Cannot parse matrix. Not adding anchor.");
				return;
			}
		}

		float quaternion[] = new float[4];
		matrix4x4ToQuaternion(anchorMatrix, quaternion);
		float translation[] = new float[3];
		matrix4x4ToTranslation(anchorMatrix, translation);

		Pose pose = new Pose(translation, quaternion);
		Anchor a = _session.createAnchor(pose);
		_anchors.put(identifier, a);
	}

	public void removeAnchor(String identifier) {
		if (_anchors.containsKey(identifier)) {
			Anchor a = _anchors.get(identifier);
			a.detach();
			_anchors.remove(identifier);
		} else {
			DeviceLog.warning("Anchor with identifier: " + identifier + " doesn't exist.");
		}
	}

	// Algorithm taken from:
    // https://d3cw3dd2w32x2b.cloudfront.net/wp-content/uploads/2015/01/matrix-to-quat.pdf
	private static void matrix4x4ToQuaternion(float[] m, float[] q) {
		float t;
		if (m[10] < 0) {
			if (m[0] > m[5]) {
				t = 1 + m[0] - m[5] - m[10];
				q[0] = t;
				q[1] = m[1] + m[4];
				q[2] = m[8] + m[2];
				q[3] = m[6] - m[9];
			} else {
				t = 1 - m[0] + m[5] - m[10];
				q[0] = m[1] + m[4];
				q[1] = t;
				q[2] = m[6] + m[9];
				q[3] = m[8] - m[2];
			}
		} else {
			if (m[0] < -m[5]) {
				t = 1 - m[0] - m[5] + m[10];
				q[0] = m[8] + m[2];
				q[1] = m[6] + m[9];
				q[2] = t;
				q[3] = m[1] - m[4];
			} else {
				t = 1 + m[0] + m[5] + m[10];
				q[0] = m[6] - m[9];
				q[1] = m[8] - m[2];
				q[2] = m[1] - m[4];
				q[3] = t;
			}
		}

		q[0] *= 0.5f / Math.sqrt(t);
		q[1] *= 0.5f / Math.sqrt(t);
		q[2] *= 0.5f / Math.sqrt(t);
		q[3] *= 0.5f / Math.sqrt(t);
	}

	private static void matrix4x4ToTranslation(float[] m, float[] t) {
		t[0] = m[3];
		t[1] = m[7];
		t[2] = m[11];
	}
}

