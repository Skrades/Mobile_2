package com.example.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class BlackHole {
    private FloatBuffer mVertexBuffer;
    private ByteBuffer mIndexBuffer;
    private FloatBuffer mTextureBuffer;
    int[] textures = new int[1];
    Context c;
    static public int[] texture_name={
            R.drawable.blackhole
    };
    private float x = 0f;
    private float y = 0f;
    private float z = -1f;
    private float speedX = 0.03f;
    private float speedY = 0.03f;
    private float rotation = 0f;
    private float size = 0.3f;
    private float alpha = 0.6f;
    private float leftBound = -3f;
    private float rightBound = 3f;
    private float bottomBound = -6f;
    private float topBound = 6f;
    private Random random = new Random();

    public BlackHole(Context context) {
        c = context;

        randomizePosition();
        randomizeDirection();

        float[] vertices = {
                -1.0f, -1.0f, 0,
                1.0f, -1.0f, 0,
                1.0f, 1.0f, 0,
                -1.0f, 1.0f, 0
        };
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        byte[] indices = {
                0, 1, 3, 1, 2, 3
        };
        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);

        float[] texCoords = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f
        };

        byteBuf = ByteBuffer.allocateDirect(texCoords.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mTextureBuffer = byteBuf.asFloatBuffer();

        mTextureBuffer.put(texCoords);
        mTextureBuffer.position(0);
    }

    public void loadTexture(GL10 gl) {
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        try {
            InputStream is = c.getResources().openRawResource(texture_name[0]);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(float deltaTime) {
        x += speedX;
        y += speedY;
        rotation += deltaTime * 50f;

        if (x < leftBound || x > rightBound || y < bottomBound || y > topBound) {
            randomizePosition();
            randomizeDirection();
        }
    }

    private void randomizeDirection() {
        float targetX = leftBound + random.nextFloat() * (rightBound - leftBound);
        float targetY = bottomBound + random.nextFloat() * (topBound - bottomBound);

        float dx = targetX - x;
        float dy = targetY - y;
        float length = (float) Math.sqrt(dx*dx + dy*dy);

        float baseSpeed = 0.02f + random.nextFloat() * 0.03f;
        speedX = (dx / length) * baseSpeed;
        speedY = (dy / length) * baseSpeed;
    }

    private void randomizePosition() {
        int side = random.nextInt(4);
        switch (side) {
            case 0:
                x = leftBound;
                y = bottomBound + random.nextFloat() * (topBound - bottomBound);
                break;
            case 1:
                x = rightBound;
                y = bottomBound + random.nextFloat() * (topBound - bottomBound);
                break;
            case 2:
                x = leftBound + random.nextFloat() * (rightBound - leftBound);
                y = bottomBound;
                break;
            case 3:
                x = leftBound + random.nextFloat() * (rightBound - leftBound);
                y = topBound;
                break;
        }
    }

    public void draw(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();

        gl.glTranslatef(x, y, z);
        gl.glRotatef(rotation, 0, 0, 1);
        gl.glScalef(size, size, size);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);

        gl.glDepthMask(false);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glTexCoordPointer(2,GL10.GL_FLOAT,0,mTextureBuffer);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE,
                mIndexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glDepthMask(true);
        gl.glDisable(GL10.GL_BLEND);

        gl.glPopMatrix();
    }
}