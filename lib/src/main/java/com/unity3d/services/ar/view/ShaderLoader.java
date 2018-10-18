package com.unity3d.services.ar.view;

import android.opengl.GLES10;
import android.opengl.GLES20;

import com.unity3d.services.core.log.DeviceLog;

public class ShaderLoader {
	public static int load(String code, int type) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, code);
		GLES20.glCompileShader(shader);

		final int[] status = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);

		if (status[0] != GLES10.GL_TRUE) {
			DeviceLog.error("Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}

		return shader;
	}

	public static boolean checkGLError(String label) {
		int lastError = GLES20.GL_NO_ERROR;
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			DeviceLog.error(label + ": glError " + error);
			lastError = error;
		}

		return lastError != GLES20.GL_NO_ERROR;
	}
}
