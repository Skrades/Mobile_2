package com.example.application;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer  {
    Rect rect;
    Cube cube;
    Sphere sun,merc,ven,earth,moon,mars,jup,sat,uran,nep;
    BlackHole blackHole;
    float[] r = new float[10];
    float[] rr = new float[9];
    private float lastTime = 0;
    private int selectedPlanet = 0;

    private final String[] planetNames = {
            "Солнце", "Меркурий", "Венера", "Земля", "Луна",
            "Марс", "Юпитер", "Сатурн", "Уран", "Нептун"
    };

    private float cameraAngleX = 0f;
    private float cameraAngleY = 30f;

    public MyRenderer(Context c){
        rect = new Rect(c);
        cube = new Cube();
        sun = new Sphere(c, 0.3f, R.drawable.sun);
        merc = new Sphere(c, 0.03f, R.drawable.merc);
        ven = new Sphere(c, 0.03f, R.drawable.ven);
        earth = new Sphere(c, 0.05f, R.drawable.earth);
        moon = new Sphere(c, 0.02f, R.drawable.moon);
        mars = new Sphere(c, 0.05f, R.drawable.mars);
        jup = new Sphere(c, 0.1f, R.drawable.jup);
        sat = new Sphere(c, 0.08f, R.drawable.sat);
        uran = new Sphere(c, 0.07f, R.drawable.uran);
        nep = new Sphere(c, 0.07f, R.drawable.nep);

        Random rand = new Random();
        for (int i = 0; i < r.length; i++) {
            r[i] = (float)rand.nextInt(360);
        }

        blackHole = new BlackHole(c);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        rect.loadGLTexture(gl);
        sun.loadGLTexture(gl);
        merc.loadGLTexture(gl);
        ven.loadGLTexture(gl);
        earth.loadGLTexture(gl);
        moon.loadGLTexture(gl);
        mars.loadGLTexture(gl);
        jup.loadGLTexture(gl);
        sat.loadGLTexture(gl);
        uran.loadGLTexture(gl);
        nep.loadGLTexture(gl);
        blackHole.loadTexture(gl);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 7);
    }

    private void drawSelectionCube(GL10 gl, float radius) {
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.2f);

        gl.glScalef(radius, radius, radius);

        cube.draw(gl);

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glDisable(GL10.GL_BLEND);
    }

    public void selectNext() {
        selectedPlanet = (selectedPlanet + 1) % 10;
    }

    public void selectPrev() {
        selectedPlanet = (selectedPlanet - 1 + 10) % 10;
    }

    public String getSelectedPlanetName() {
        return planetNames[selectedPlanet];
    }

    public void moveCamera(float deltaX, float deltaY) {
        cameraAngleX += deltaX * 0.5f;
        cameraAngleY += deltaY * 0.5f;

        if (cameraAngleY > 90) cameraAngleY = 90;
        if (cameraAngleY < -90) cameraAngleY = -90;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        long currentTime = System.nanoTime();

        if (lastTime == -1) {
            lastTime = currentTime;
        } else {
            float deltaTime = (currentTime - lastTime) / 1000000000.0f;
            if (deltaTime > 0.1f) deltaTime = 0.1f;
            blackHole.update(deltaTime);
            lastTime = currentTime;
        }

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glTranslatef(0, 0, -6f);
        gl.glScalef(3f, 6f, 0f);
        rect.draw(gl);

        gl.glLoadIdentity();
        gl.glTranslatef(0,0,-3f);
        blackHole.draw(gl);

        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -3f);
        gl.glRotatef(cameraAngleY, 1, 0, 0);
        gl.glRotatef(cameraAngleX, 0, 1, 0);

        gl.glPushMatrix();
        gl.glRotatef(r[0],0,1,0);
        sun.draw(gl);
        if (selectedPlanet == 0) {
            drawSelectionCube(gl, 0.3f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[1],0,1,0);
        gl.glTranslatef(0.5f, 0f, 0f);
        gl.glRotatef(rr[0],0,1,0);
        merc.draw(gl);
        if (selectedPlanet == 1) {
            drawSelectionCube(gl, 0.03f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[2],0,1,0);
        gl.glTranslatef(0.6f, 0f, 0f);
        gl.glRotatef(rr[1],0,1,0);
        ven.draw(gl);
        if (selectedPlanet == 2) {
            drawSelectionCube(gl, 0.03f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[3],0,1,0);
        gl.glTranslatef(0.8f, 0f, 0f);
        gl.glRotatef(rr[2],0,1,0);
        earth.draw(gl);
        if (selectedPlanet == 3) {
            drawSelectionCube(gl, 0.05f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[3],0,1,0);
        gl.glTranslatef(0.8f, 0f, 0f);
        gl.glRotatef(r[4],0,0,1);
        gl.glTranslatef(0.1f, 0f, 0f);
        gl.glRotatef(rr[3],0,1,0);
        moon.draw(gl);
        if (selectedPlanet == 4) {
            drawSelectionCube(gl, 0.02f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[5],0,1,0);
        gl.glTranslatef(1.1f, 0f, 0f);
        gl.glRotatef(rr[4],0,1,0);
        mars.draw(gl);
        if (selectedPlanet == 5) {
            drawSelectionCube(gl, 0.05f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[6],0,1,0);
        gl.glTranslatef(1.5f, 0f, 0f);
        gl.glRotatef(rr[5],0,1,0);
        jup.draw(gl);
        if (selectedPlanet == 6) {
            drawSelectionCube(gl, 0.1f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[7],0,1,0);
        gl.glTranslatef(1.7f, 0f, 0f);
        gl.glRotatef(rr[6],0,1,0);
        sat.draw(gl);
        if (selectedPlanet == 7) {
            drawSelectionCube(gl, 0.08f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[8],0,1,0);
        gl.glTranslatef(1.9f, 0f, 0f);
        gl.glRotatef(rr[7],0,1,0);
        uran.draw(gl);
        if (selectedPlanet == 8) {
            drawSelectionCube(gl, 0.07f);
        }
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(r[9],0,1,0);
        gl.glTranslatef(2.1f, 0f, 0f);
        gl.glRotatef(rr[8],0,1,0);
        nep.draw(gl);
        if (selectedPlanet == 9) {
            drawSelectionCube(gl, 0.07f);
        }
        gl.glPopMatrix();

        r[0] = (r[0] > 360) ? 0 : r[0] + 0.5f;
        r[1] = (r[1] > 360) ? 0 : r[1] + 1f;
        r[2] = (r[2] > 360) ? 0 : r[2] + 0.85f;
        r[3] = (r[3] > 360) ? 0 : r[3] + 0.75f; r[4] = (r[4] > 360) ? 0 : r[4] + 1.3f;
        r[5] = (r[5] > 360) ? 0 : r[5] + 0.65f;
        r[6] = (r[6] > 360) ? 0 : r[6] + 0.45f;
        r[7] = (r[7] > 360) ? 0 : r[7] + 0.4f;
        r[8] = (r[8] > 360) ? 0 : r[8] + 0.375f;
        r[9] = (r[9] > 360) ? 0 : r[9] + 0.35f;

        rr[0] = (rr[0] > 360) ? 0 : rr[0] + 0.5f;
        rr[1] = (rr[1] > 360) ? 0 : rr[1] + 0.5f;
        rr[2] = (rr[2] > 360) ? 0 : rr[2] + 0.5f;
        rr[3] = (rr[3] > 360) ? 0 : rr[3] + 0.5f; rr[4] = (rr[4] > 360) ? 0 : rr[4] + 0.5f;
        rr[5] = (rr[5] > 360) ? 0 : rr[5] + 0.5f;
        rr[6] = (rr[6] > 360) ? 0 : rr[6] + 0.5f;
        rr[7] = (rr[7] > 360) ? 0 : rr[7] + 0.5f;
        rr[8] = (rr[8] > 360) ? 0 : rr[8] + 0.5f;
    }

}
