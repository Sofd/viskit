package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.model.*;

public class Cube
{
    // vertices
    protected static float[][] vs =
    {
            {
                    -1, -1, -1
            },
            {
                    1, -1, -1
            },
            {
                    1, 1, -1
            },
            {
                    -1, 1, -1
            },
            {
                    1, -1, 1
            },
            {
                    -1, -1, 1
            },
            {
                    -1, 1, 1
            },
            {
                    1, 1, 1
            },
    };

    // texture coords
    protected static float[][] ts =
    {
            {
                    0, 0, 0
            },
            {
                    1, 0, 0
            },
            {
                    1, 1, 0
            },
            {
                    0, 1, 0
            },
            {
                    1, 0, 1
            },
            {
                    0, 0, 1
            },
            {
                    0, 1, 1
            },
            {
                    1, 1, 1
            }
    };

    // vertex indizes
    protected static int[][] vis =
    {
            {
                    0, 1, 2, 3
            },
            {
                    1, 4, 7, 2
            },
            {
                    4, 5, 6, 7
            },
            {
                    5, 0, 3, 6
            },
            {
                    3, 2, 7, 6
            },
            {
                    5, 4, 1, 0
            }
    };

    // normals
    protected static float[][] ns =
    {
            {
                    0.0f, 0.0f, -1.0f
            },
            {
                    1.0f, 0.0f, 0.0f
            },
            {
                    0.0f, 0.0f, 1.0f
            },
            {
                    -1.0f, 0.0f, 0.0f
            },
            {
                    0.0f, 1.0f, 0.0f
            },
            {
                    0.0f, -1.0f, 0.0f
            },
    };

    protected GL2 gl;
    protected float xSize;
    protected float ySize;
    protected float zSize;

    public Cube( GL2 gl, double xSize, double ySize, double zSize )
    {
        this.gl = gl;
        this.xSize = (float)xSize;
        this.ySize = (float)ySize;
        this.zSize = (float)zSize;
    }

    public void show( VolumeConstraint contraint )
    {
        gl.glBegin( GL_QUADS );
        gl.glColor4f( 1, 1, 1, 1 );
        for ( int i = 0; i < 6; ++i )
        {
            gl.glNormal3fv( ns[ i ], 0 );

            for ( int j = 0; j < 4; ++j )
            {
                int vi = vis[ i ][ j ];
                
                float tx = ts[ vi ][ 0 ];
                
                if ( vi == 1 || vi == 2 || vi == 4 || vi == 7 )
                    tx *= contraint.getX().getMax();
                else
                    tx = contraint.getX().getMin();
                
                float ty = ts[ vi ][ 1 ];

                if ( vi == 2 || vi == 3 || vi == 6 || vi == 7 )
                    ty *= contraint.getY().getMax();
                else
                    ty = contraint.getY().getMin();

                float tz = ts[ vi ][ 2 ];
                if ( vi == 4 || vi == 5 || vi == 6 || vi == 7 )
                    tz *= contraint.getZ().getMax();
                else
                    tz = contraint.getZ().getMin();

                gl.glTexCoord3f( tx, 1 - ty, tz );

                float vx = vs[ vi ][ 0 ] * xSize / 2.0f;
                
                if ( vi == 1 || vi == 2 || vi == 4 || vi == 7 )
                    vx = -vx + xSize * contraint.getX().getMax();
                else
                    vx += xSize * contraint.getX().getMin();
                
                float vy = vs[ vi ][ 1 ] * ySize / 2.0f;

                if ( vi == 2 || vi == 3 || vi == 6 || vi == 7 )
                    vy = -vy + ySize * contraint.getY().getMax();
                else
                    vy += ySize * contraint.getY().getMin();
                
                float vz = vs[ vi ][ 2 ] * zSize / 2.0f;

                if ( vi == 4 || vi == 5 || vi == 6 || vi == 7 )
                    vz = -vz + zSize * contraint.getZ().getMax();
                else
                    vz += zSize * contraint.getZ().getMin();

                gl.glVertex3f( vx, vy, vz );
            }
        }
        gl.glEnd();
    }
}