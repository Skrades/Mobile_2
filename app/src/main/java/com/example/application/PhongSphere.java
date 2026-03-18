package com.example.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class PhongSphere {
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mNormalBuffer;
    private FloatBuffer mTexCoordBuffer;
    private int vertexCount = 0;

    private int mProgram = 0;
    private int maPositionHandle = -1;
    private int maNormalHandle = -1;
    private int maTexCoordHandle = -1;
    private int muMVPMatrixHandle = -1;
    private int muModelMatrixHandle = -1;
    private int muLightPosHandle = -1;
    private int muCameraPosHandle = -1;
    private int muTextureHandle = -1;

    private int[] textures = new int[1];
    private Context context;
    private int textureResource;

    private float[] modelMatrix = new float[16];
    private float[] modelViewMatrix = new float[16];
    private float[] modelViewProjectionMatrix = new float[16];

    private float rotationAngle = 0f;
    private float R;

    private float[] lightPos = {2.0f, 2.0f, 2.0f};
    private float[] cameraPos = {0f, 0f, 3f};

    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;\n" +
        "uniform mat4 uModelMatrix;\n" +
        "attribute vec3 aPosition;\n" +
        "attribute vec3 aNormal;\n" +
        "attribute vec2 aTexCoord;\n" +
        "varying vec3 vPosition;\n" +
        "varying vec3 vNormal;\n" +
        "varying vec2 vTexCoord;\n" +
        "void main() {\n" +
        "   vPosition = vec3(uModelMatrix * vec4(aPosition, 1.0));\n" +
        "   vNormal = normalize(mat3(uModelMatrix) * aNormal);\n" +
        "   vTexCoord = aTexCoord;\n" +
        "   gl_Position = uMVPMatrix * vec4(aPosition, 1.0);\n" +
        "}";

    private final String fragmentShaderCode =
            "precision mediump float;\n" +
            "uniform vec3 uLightPos;\n" +
            "uniform vec3 uCameraPos;\n" +
            "uniform sampler2D uTexture;\n" +
            "varying vec3 vPosition;\n" +
            "varying vec3 vNormal;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "   vec3 normal = normalize(vNormal);\n" +
            "   vec3 lightDir = normalize(uLightPos - vPosition);\n" +
            "   vec3 viewDir = normalize(uCameraPos - vPosition);\n" +
            "   vec3 reflectDir = reflect(-lightDir, normal);\n" +
            "   float ambientStrength = 0.3;\n" +
            "   vec3 ambient = ambientStrength * vec3(1.0, 1.0, 1.0);\n" +
            "   float diff = max(dot(normal, lightDir), 0.0);\n" +
            "   vec3 diffuse = diff * vec3(1.0, 1.0, 1.0);\n" +
            "   float specularStrength = 0.5;\n" +
            "   float shininess = 32.0;\n" +
            "   float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);\n" +
            "   vec3 specular = specularStrength * spec * vec3(1.0, 1.0, 1.0);\n" +
            "   vec3 result = (ambient + diffuse + specular);\n" +
            "   vec4 texColor = texture2D(uTexture, vTexCoord);\n" +
            "   gl_FragColor = vec4(result, 1.0) * texColor;\n" +
            "}";

    public PhongSphere(Context context, float R, int textureResource) {
        this.context = context;
        this.R = R;
        this.textureResource = textureResource;
        initSphere(R);
    }

    private void initSphere(float R) {
        int dtheta = 15, dphi = 15;
        float DTOR = (float) (Math.PI / 180.0f);

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(20000 * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();

        byteBuf = ByteBuffer.allocateDirect(20000 * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mNormalBuffer = byteBuf.asFloatBuffer();

        byteBuf = ByteBuffer.allocateDirect(20000 * 2 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mTexCoordBuffer = byteBuf.asFloatBuffer();

        for (int theta = -90; theta < 90; theta += dtheta) {
            for (int phi = 0; phi < 360; phi += dphi) {

                float theta1 = theta * DTOR;
                float theta2 = (theta + dtheta) * DTOR;
                float phi1 = phi * DTOR;
                float phi2 = (phi + dphi) * DTOR;

                addVertex(R, theta1, phi1, phi, theta);
                addVertex(R, theta1, phi2, phi + dphi, theta);
                addVertex(R, theta2, phi2, phi + dphi, theta + dtheta);

                addVertex(R, theta1, phi1, phi, theta);
                addVertex(R, theta2, phi2, phi + dphi, theta + dtheta);
                addVertex(R, theta2, phi1, phi, theta + dtheta);
            }
        }

        mVertexBuffer.position(0);
        mNormalBuffer.position(0);
        mTexCoordBuffer.position(0);
    }

    private void addVertex(float R, float theta, float phi, float phiDeg, float thetaDeg) {
        float x = (float) (R * Math.cos(theta) * Math.cos(phi));
        float y = (float) (R * Math.sin(theta));
        float z = (float) (R * Math.cos(theta) * Math.sin(phi));

        mVertexBuffer.put(x);
        mVertexBuffer.put(y);
        mVertexBuffer.put(z);

        mNormalBuffer.put(x / R);
        mNormalBuffer.put(y / R);
        mNormalBuffer.put(z / R);

        float u = phiDeg / 360.0f;
        float v = (thetaDeg + 90.0f) / 180.0f;
        v = 1.0f - v;

        mTexCoordBuffer.put(u);
        mTexCoordBuffer.put(v);

        vertexCount++;
    }

    public void initShaders(GL10 gl) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        muModelMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uModelMatrix");
        muLightPosHandle = GLES20.glGetUniformLocation(mProgram, "uLightPos");
        muCameraPosHandle = GLES20.glGetUniformLocation(mProgram, "uCameraPos");
        muTextureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");

        loadTexture();
    }

    private void loadTexture() {
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        try {
            InputStream is = context.getResources().openRawResource(textureResource);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void setRotationAngle(float angle) {
        this.rotationAngle = angle;
    }

    public void setLightPosition(float x, float y, float z) {
        lightPos[0] = x;
        lightPos[1] = y;
        lightPos[2] = z;
    }

    public void setCameraPosition(float x, float y, float z) {
        cameraPos[0] = x;
        cameraPos[1] = y;
        cameraPos[2] = z;
    }

    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        GLES20.glUseProgram(mProgram);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, rotationAngle, 0, 1, 0);

        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

        if (muMVPMatrixHandle >= 0) {
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
        }
        if (muModelMatrixHandle >= 0) {
            GLES20.glUniformMatrix4fv(muModelMatrixHandle, 1, false, modelMatrix, 0);
        }

        if (muLightPosHandle >= 0) {
            GLES20.glUniform3f(muLightPosHandle, lightPos[0], lightPos[1], lightPos[2]);
        }
        if (muCameraPosHandle >= 0) {
            GLES20.glUniform3f(muCameraPosHandle, cameraPos[0], cameraPos[1], cameraPos[2]);
        }

        if (muTextureHandle >= 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glUniform1i(muTextureHandle, 0);
        }

        if (maPositionHandle >= 0) {
            mVertexBuffer.position(0);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(maPositionHandle);
        }

        if (maNormalHandle >= 0) {
            mNormalBuffer.position(0);
            GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false, 0, mNormalBuffer);
            GLES20.glEnableVertexAttribArray(maNormalHandle);
        }

        if (maTexCoordHandle >= 0) {
            mTexCoordBuffer.position(0);
            GLES20.glVertexAttribPointer(maTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);
            GLES20.glEnableVertexAttribArray(maTexCoordHandle);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        if (maPositionHandle >= 0) {
            GLES20.glDisableVertexAttribArray(maPositionHandle);
        }
        if (maNormalHandle >= 0) {
            GLES20.glDisableVertexAttribArray(maNormalHandle);
        }
        if (maTexCoordHandle >= 0) {
            GLES20.glDisableVertexAttribArray(maTexCoordHandle);
        }
    }
}