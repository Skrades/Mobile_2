package com.example.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Sphere {
    private FloatBuffer mVertexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer normalBuffer;
    int[] textures = new int[1];
    Context c;
    int texture_name;
    int vertexCount = 0;

    public Sphere(Context context, float R, int texture) {
        c = context;
        texture_name = texture;

        int dtheta = 15, dphi = 15;
        float DTOR = (float) (Math.PI / 180.0f);

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(25000 * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();

        byteBuf = ByteBuffer.allocateDirect(25000 * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        normalBuffer = byteBuf.asFloatBuffer();

        byteBuf = ByteBuffer.allocateDirect(25000 * 2 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();

        for (int theta = -90; theta < 90; theta += dtheta) {
            for (int phi = 0; phi < 360; phi += dphi) {
                float theta1 = theta * DTOR;
                float theta2 = (theta + dtheta) * DTOR;
                float phi1 = phi * DTOR;
                float phi2 = (phi + dphi) * DTOR;

                addVertex(R, theta1, phi1);
                addVertex(R, theta1, phi2);
                addVertex(R, theta2, phi2);

                addVertex(R, theta1, phi1);
                addVertex(R, theta2, phi2);
                addVertex(R, theta2, phi1);
            }
        }

        mVertexBuffer.position(0);
        textureBuffer.position(0);
    }

    private void addVertex(float R, float theta, float phi) {
        float x = (float) (R * Math.cos(theta) * Math.cos(phi));
        float y = (float) (R * Math.sin(theta));
        float z = (float) (R * Math.cos(theta) * Math.sin(phi));

        mVertexBuffer.put(x);
        mVertexBuffer.put(y);
        mVertexBuffer.put(z);

        float length = (float) Math.sqrt(x*x + y*y + z*z);
        normalBuffer.put(x / length);
        normalBuffer.put(y / length);
        normalBuffer.put(z / length);

        float u = phi / (float)(2 * Math.PI);
        float v = (theta + (float)Math.PI/2) / (float)Math.PI;

        v = 1.0f - v;

        textureBuffer.put(u);
        textureBuffer.put(v);

        vertexCount++;
    }

    public void loadGLTexture(GL10 gl) {
        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        try {
            InputStream is = c.getResources().openRawResource(texture_name);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

        gl.glEnable(GL10.GL_TEXTURE_2D);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertexCount);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }
}
