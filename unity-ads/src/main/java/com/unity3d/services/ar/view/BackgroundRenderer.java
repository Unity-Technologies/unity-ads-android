package com.unity3d.services.ar.view;

import android.annotation.TargetApi;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;

import com.google.ar.core.Frame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BackgroundRenderer {
	private static final int COORDS_PER_VERTEX = 3;
	private static final int TEXCOORDS_PER_VERTEX = 2;
	private static final int FLOAT_SIZE = 4;

	private FloatBuffer quadVertices;
	private FloatBuffer quadTexCoord;
	private FloatBuffer quadTexCoordTransformed;

	private int quadProgram;

	private int quadPositionParam;
	private int quadTexCoordParam;
	private int textureId = -1;

	BackgroundRenderer() {
	}

	int getTextureId() {
		return textureId;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	void createOnGlThread() {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		textureId = textures[0];
		int textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
		GLES20.glBindTexture(textureTarget, textureId);
		GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		int numVertices = 4;
		if (numVertices != QUAD_COORDS.length / COORDS_PER_VERTEX) {
			throw new RuntimeException("Unexpected number of vertices in BackgroundRenderer.");
		}

		ByteBuffer bbVertices = ByteBuffer.allocateDirect(QUAD_COORDS.length * FLOAT_SIZE);
		bbVertices.order(ByteOrder.nativeOrder());
		quadVertices = bbVertices.asFloatBuffer();
		quadVertices.put(QUAD_COORDS);
		quadVertices.position(0);

		ByteBuffer bbTexCoords =
				ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE);
		bbTexCoords.order(ByteOrder.nativeOrder());
		quadTexCoord = bbTexCoords.asFloatBuffer();
		quadTexCoord.put(QUAD_TEXCOORDS);
		quadTexCoord.position(0);

		ByteBuffer bbTexCoordsTransformed =
				ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE);
		bbTexCoordsTransformed.order(ByteOrder.nativeOrder());
		quadTexCoordTransformed = bbTexCoordsTransformed.asFloatBuffer();

		int vertexShader = ShaderLoader.load(VERTEX_SHADER, GLES20.GL_VERTEX_SHADER);
		int fragmentShader = ShaderLoader.load(FRAGMENT_SHADER, GLES20.GL_FRAGMENT_SHADER);

		quadProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(quadProgram, vertexShader);
		GLES20.glAttachShader(quadProgram, fragmentShader);
		GLES20.glLinkProgram(quadProgram);
		GLES20.glUseProgram(quadProgram);

		ShaderLoader.checkGLError("Program creation");

		quadPositionParam = GLES20.glGetAttribLocation(quadProgram, "a_Position");
		quadTexCoordParam = GLES20.glGetAttribLocation(quadProgram, "a_TexCoord");

		ShaderLoader.checkGLError("Program parameters");
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	void draw(Frame frame) {
		// If display rotation changed (also includes view size change), we need to re-query the uv
		// coordinates for the screen rect, as they may have changed as well.
		if (frame.hasDisplayGeometryChanged()) {
			frame.transformDisplayUvCoords(quadTexCoord, quadTexCoordTransformed);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthMask(false);

		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

		GLES20.glUseProgram(quadProgram);

		GLES20.glVertexAttribPointer(
				quadPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadVertices);

		GLES20.glVertexAttribPointer(
				quadTexCoordParam,
				TEXCOORDS_PER_VERTEX,
				GLES20.GL_FLOAT,
				false,
				0,
				quadTexCoordTransformed);

		GLES20.glEnableVertexAttribArray(quadPositionParam);
		GLES20.glEnableVertexAttribArray(quadTexCoordParam);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		GLES20.glDisableVertexAttribArray(quadPositionParam);
		GLES20.glDisableVertexAttribArray(quadTexCoordParam);

		GLES20.glDepthMask(true);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}

	private static final float[] QUAD_COORDS =
			new float[]{
					-1.0f, -1.0f, 0.0f, -1.0f, +1.0f, 0.0f, +1.0f, -1.0f, 0.0f, +1.0f, +1.0f, 0.0f,
			};

	private static final float[] QUAD_TEXCOORDS =
			new float[]{
					0.0f, 1.0f,
					0.0f, 0.0f,
					1.0f, 1.0f,
					1.0f, 0.0f,
			};

	private static final String VERTEX_SHADER =
			"attribute vec4 a_Position;\n" +
			"attribute vec2 a_TexCoord;\n" +
			"\n" +
			"varying vec2 v_TexCoord;\n" +
			"\n" +
			"void main() {\n" +
			"   gl_Position = a_Position;\n" +
			"   v_TexCoord = a_TexCoord;\n" +
			"}";

	private static final String FRAGMENT_SHADER =
			"#extension GL_OES_EGL_image_external : require\n" +
			"precision mediump float;\n" +
			"varying vec2 v_TexCoord;\n" +
			"uniform samplerExternalOES sTexture;\n" +
			"void main() {\n" +
			"    gl_FragColor = texture2D(sTexture, v_TexCoord);\n" +
			"}";
}
