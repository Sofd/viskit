package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;

public class Cube
{
    //vertices
    protected static float[][] vs = {
        { -1, -1, -1 },
        {  1, -1, -1 },
        {  1,  1, -1 },
        { -1,  1, -1 },
        {  1, -1,  1 },
        { -1, -1,  1 },
        { -1,  1,  1 },
        {  1,  1,  1 },
    };
    
    //texture coords
    protected static float[][] ts = {
        {  0,  0, 0 },
        {  1,  0, 0 },
        {  1,  1, 0 },
        {  0,  1, 0 },
        {  1,  0, 1 },
        {  0,  0, 1 },
        {  0,  1, 1 },
        {  1,  1, 1 }
    };
    
    //vertex indizes
    protected static int[][] vis = {
        { 0, 1, 2, 3 },
        { 1, 4, 7, 2 },
        { 4, 5, 6, 7 },
        { 5, 0, 3, 6 },
        { 3, 2, 7, 6 },
        { 5, 4, 1, 0 }
    };
    
    //normals
    protected static float [][] ns = {
        {  0.0f,  0.0f,  1.0f },
        {  1.0f,  0.0f,  0.0f },
        {  0.0f,  0.0f, -1.0f },
        { -1.0f,  0.0f,  0.0f },
        {  0.0f,  1.0f,  0.0f },
        {  0.0f, -1.0f,  0.0f },
    };
    
    protected GL2 gl;
    protected float xSize;
    protected float ySize;
    protected float zSize;
    
    public Cube(GL2 gl, double xSize, double ySize, double zSize)
    {
        this.gl = gl;
        this.xSize = (float)xSize;
        this.ySize = (float)ySize;
        this.zSize = (float)zSize;
    }
    
    public void show(float yLevel, float zLevelMin, float zLevelMax)
    {
        
        gl.glBegin(GL_QUADS);
            gl.glColor4f( 1, 1, 1, 1 );
            for ( int i = 0; i < 6; ++i )
            {
                gl.glNormal3fv(ns[i], 0);
                
                for ( int j = 0; j < 4; ++j )
                {
                    int vi = vis[i][j];
                    float tx = ts[vi][0];
                    float ty = ts[vi][1];
                    
                    if ( vi == 2 || vi == 3 || vi == 6 || vi == 7 )
                        ty *= yLevel;
                    
                    float tz = ts[vi][2];
                    if ( vi == 4 || vi == 5 || vi == 6 || vi == 7 )
                        tz *= zLevelMax;
                    else
                        tz = zLevelMin;
                    
                    gl.glTexCoord3f(tx, ty, tz);
                    
                    float vx = vs[vi][0]*xSize/2.0f;
                    float vy = vs[vi][1]*ySize/2.0f;
                    
                    if ( vi == 2 || vi == 3 || vi == 6 || vi == 7 )
                        vy = -vy + ySize * yLevel;
                    
                    float vz = vs[vi][2]*zSize/2.0f;
                    
                    if ( vi == 4 || vi == 5 || vi == 6 || vi == 7 )
                        vz = -vz + zSize * zLevelMax;
                    else
                        vz += zSize * zLevelMin;
                    
                    gl.glVertex3f(vx, vy, vz);
                }
            }
        gl.glEnd();
    }
}