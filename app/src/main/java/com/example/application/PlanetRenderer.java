package com.example.application;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlanetRenderer implements GLSurfaceView.Renderer {
    private PhongSphere planet;
    private float rotationAngle = 0.0f;
    private float rotationSpeed = 1.0f;

    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    private float camX = 0f, camY = 0f, camZ = 3f;
    private float lightX = 2f, lightY = 2f, lightZ = 2f;

    public PlanetRenderer(Context context, int textureResource, float size) {
        planet = new PhongSphere(context, 1f, textureResource);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        planet.initShaders(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.perspectiveM(projectionMatrix, 0, 45, ratio, 0.1f, 10f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(viewMatrix, 0,
                camX, camY, camZ,
                0, 0, 0,
                0, 1, 0);

        planet.setRotationAngle(rotationAngle);
        planet.setLightPosition(lightX, lightY, lightZ);
        planet.setCameraPosition(camX, camY, camZ);

        planet.draw(viewMatrix, projectionMatrix);

        rotationAngle += rotationSpeed;
        if (rotationAngle > 360) rotationAngle -= 360;
    }
}