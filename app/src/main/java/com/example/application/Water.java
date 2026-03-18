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

public class Water {
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mNormalBuffer;
    private FloatBuffer mTexCoordBuffer;
    private int vertexCount = 0;

    private int mProgram = 0;
    private int[] textures = new int[1];
    private int textureResource;
    private Context context;

    private float[] modelMatrix = new float[16];
    private float[] modelViewProjectionMatrix = new float[16];

    private float time = 0f;
    private int sectors = 64;
    private int stacks = 32;
    private float radius = 1.5f;

    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;\n" +
        "uniform float uTime;\n" +
        "attribute vec3 aPosition;\n" +
        "attribute vec3 aNormal;\n" +
        "attribute vec2 aTexCoord;\n" +
        "varying vec2 vTexCoord;\n" +
        "varying vec3 vPosition;\n" +
        "varying float vHeight;\n" +
        "void main() {\n" +
        "   vec3 normal = normalize(aNormal);\n" +
        "   \n" +
        "   float wave = sin(aTexCoord.x * 12.0 + uTime * 1.2) * cos(aTexCoord.y * 10.0 + uTime * 1.0) * 0.015;\n" +
        "   float wave2 = sin(aTexCoord.x * 30.0 - uTime * 2.5) * sin(aTexCoord.y * 30.0 + uTime * 2.0) * 0.005;\n" +
        "   \n" +
        "   float offset = wave + wave2;\n" +
        "   vec3 newPosition = aPosition + normal * offset;\n" +
        "   \n" +
        "   vTexCoord = aTexCoord;\n" +
        "   vPosition = newPosition;\n" +
        "   vHeight = offset * 20.0 + 0.5;\n" +
        "   gl_Position = uMVPMatrix * vec4(newPosition, 1.0);\n" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;\n" +
        "uniform sampler2D uTexture;\n" +
        "uniform float uTime;\n" +
        "varying vec2 vTexCoord;\n" +
        "varying float vHeight;\n" +
        "void main() {\n" +
        "   vec2 texCoord = vTexCoord;\n" +
        "   texCoord.x += uTime * 0.02;\n" +
        "   texCoord.y += uTime * 0.01;\n" +
        "   \n" +
        "   vec4 texColor = texture2D(uTexture, texCoord);\n" +
        "   \n" +
        "   vec3 deepWater = vec3(0.0, 0.15, 0.4);\n" +
        "   vec3 shallowWater = vec3(0.1, 0.4, 0.7);\n" +
        "   vec3 waterColor = mix(deepWater, shallowWater, vHeight);\n" +
        "   \n" +
        "   float highlight = sin(vTexCoord.x * 20.0 + uTime * 2.0) * 0.3 + 0.7;\n" +
        "   highlight = highlight * 0.15;\n" +
        "   \n" +
        "   vec3 finalColor = waterColor * texColor.rgb + highlight;\n" +
        "   gl_FragColor = vec4(finalColor, 1.0);\n" +
        "}";

    public Water(Context context) {
        this.context = context;
        this.textureResource = R.drawable.water;
        initSphere();
    }

    private void initSphere() {
        float sectorStep = 2 * (float)Math.PI / sectors;
        float stackStep = (float)Math.PI / stacks;

        int approxVertexCount = (sectors + 1) * (stacks + 1) * 6;

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(approxVertexCount * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();

        byteBuf = ByteBuffer.allocateDirect(approxVertexCount * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mNormalBuffer = byteBuf.asFloatBuffer();

        byteBuf = ByteBuffer.allocateDirect(approxVertexCount * 2 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mTexCoordBuffer = byteBuf.asFloatBuffer();

        for (int i = 0; i < stacks; i++) {
            float stackAngle1 = (float)Math.PI/2 - i * stackStep;
            float stackAngle2 = (float)Math.PI/2 - (i + 1) * stackStep;

            float sin1 = (float)Math.sin(stackAngle1);
            float cos1 = (float)Math.cos(stackAngle1);
            float sin2 = (float)Math.sin(stackAngle2);
            float cos2 = (float)Math.cos(stackAngle2);

            for (int j = 0; j < sectors; j++) {
                float sectorAngle1 = j * sectorStep;
                float sectorAngle2 = (j + 1) * sectorStep;

                float sin11 = (float)Math.sin(sectorAngle1);
                float cos11 = (float)Math.cos(sectorAngle1);
                float sin12 = (float)Math.sin(sectorAngle2);
                float cos12 = (float)Math.cos(sectorAngle2);

                float x1 = radius * cos1 * cos11;
                float y1 = radius * sin1;
                float z1 = radius * cos1 * sin11;

                float x2 = radius * cos1 * cos12;
                float y2 = radius * sin1;
                float z2 = radius * cos1 * sin12;

                float x3 = radius * cos2 * cos11;
                float y3 = radius * sin2;
                float z3 = radius * cos2 * sin11;

                float x4 = radius * cos2 * cos12;
                float y4 = radius * sin2;
                float z4 = radius * cos2 * sin12;

                float u1 = (float)j / sectors;
                float u2 = (float)(j + 1) / sectors;
                float v1 = (float)i / stacks;
                float v2 = (float)(i + 1) / stacks;

                addVertex(x1, y1, z1, x1/radius, y1/radius, z1/radius, u1, v1);
                addVertex(x2, y2, z2, x2/radius, y2/radius, z2/radius, u2, v1);
                addVertex(x3, y3, z3, x3/radius, y3/radius, z3/radius, u1, v2);

                addVertex(x2, y2, z2, x2/radius, y2/radius, z2/radius, u2, v1);
                addVertex(x4, y4, z4, x4/radius, y4/radius, z4/radius, u2, v2);
                addVertex(x3, y3, z3, x3/radius, y3/radius, z3/radius, u1, v2);
            }
        }

        mVertexBuffer.position(0);
        mNormalBuffer.position(0);
        mTexCoordBuffer.position(0);
    }

    private void addVertex(float x, float y, float z, float nx, float ny, float nz, float u, float v) {
        mVertexBuffer.put(x);
        mVertexBuffer.put(y);
        mVertexBuffer.put(z);

        mNormalBuffer.put(nx);
        mNormalBuffer.put(ny);
        mNormalBuffer.put(nz);

        mTexCoordBuffer.put(u);
        mTexCoordBuffer.put(v);

        vertexCount++;
    }

    public void initShaders() {
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        loadTexture();
    }

    private void loadTexture() {
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

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

    public void update(float deltaTime) {
        time += deltaTime;
    }

    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        if (mProgram == 0) return;

        GLES20.glUseProgram(mProgram);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0, 0, 0);
        Matrix.scaleM(modelMatrix, 0, 1.2f, 1.2f, 1.2f);

        float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, temp, 0);

        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        if (mvpMatrixHandle >= 0) {
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
        }

        int timeHandle = GLES20.glGetUniformLocation(mProgram, "uTime");
        if (timeHandle >= 0) {
            GLES20.glUniform1f(timeHandle, time);
        }

        int textureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
        if (textureHandle >= 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glUniform1i(textureHandle, 0);
        }

        int posHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int normHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        int texHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");

        if (posHandle >= 0) {
            mVertexBuffer.position(0);
            GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(posHandle);
        }

        if (normHandle >= 0) {
            mNormalBuffer.position(0);
            GLES20.glVertexAttribPointer(normHandle, 3, GLES20.GL_FLOAT, false, 0, mNormalBuffer);
            GLES20.glEnableVertexAttribArray(normHandle);
        }

        if (texHandle >= 0) {
            mTexCoordBuffer.position(0);
            GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);
            GLES20.glEnableVertexAttribArray(texHandle);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        if (posHandle >= 0) {
            GLES20.glDisableVertexAttribArray(posHandle);
        }
        if (normHandle >= 0) {
            GLES20.glDisableVertexAttribArray(normHandle);
        }
        if (texHandle >= 0) {
            GLES20.glDisableVertexAttribArray(texHandle);
        }
    }
}