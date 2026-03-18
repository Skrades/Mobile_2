package com.example.application;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class InfoActivity extends Activity {
    private GLSurfaceView glSurfaceView;
    private PlanetRenderer renderer;
    private TextView infoText;
    private Water water;
    private float lastTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        Bundle extras = getIntent().getExtras();
        String planetName = extras.getString("planet_name");
        int textureRes = extras.getInt("texture_resource");
        float size = extras.getFloat("planet_size", 0.5f);
        String planetInfo = extras.getString("planet_info");

        setTitle(planetName);

        glSurfaceView = findViewById(R.id.planet_gl_surface);
        glSurfaceView.setEGLContextClientVersion(2);

        if ("Нептун".equals(planetName)) {
            infoText = findViewById(R.id.planet_info_text);
            infoText.setText(planetInfo);
            setTitle(planetName);
            water = new Water(this);
            glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
                @Override
                public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                    GLES20.glClearColor(0.0f, 0.0f, 0.1f, 1.0f);
                    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                    water.initShaders();
                }

                @Override
                public void onSurfaceChanged(GL10 gl, int width, int height) {
                    GLES20.glViewport(0, 0, width, height);
                }

                @Override
                public void onDrawFrame(GL10 gl) {
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                    long currentTime = System.nanoTime();

                    if (lastTime == -1) {
                        lastTime = currentTime;
                    } else {
                        float deltaTime = (currentTime - lastTime) / 1000000000.0f;


                        if (deltaTime > 0.1f) deltaTime = 0.1f;

                        water.update(deltaTime);
                        lastTime = currentTime;
                    }

                    float[] viewMatrix = new float[16];
                    float[] projMatrix = new float[16];

                    Matrix.setLookAtM(viewMatrix, 0,
                            0, 2, 5,
                            0, 0, 0,
                            0, 1, 0);

                    float ratio = (float) glSurfaceView.getWidth() / glSurfaceView.getHeight();
                    Matrix.perspectiveM(projMatrix, 0, 45, ratio, 0.1f, 10f);

                    water.draw(viewMatrix, projMatrix);
                }
            });
        } else {
            renderer = new PlanetRenderer(this, textureRes, size);
            glSurfaceView.setRenderer(renderer);
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

            infoText = findViewById(R.id.planet_info_text);
            infoText.setText(planetInfo);
        }

        Button backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
