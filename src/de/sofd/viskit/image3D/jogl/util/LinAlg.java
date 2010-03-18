package de.sofd.viskit.image3D.jogl.util;

import java.awt.geom.AffineTransform;

/**
 *
 * @author olaf
 */
public class LinAlg {

    // float[]s representing matrices are column-major, as OpenGL functions expect it

    // this is a least-effort port straight from my C linalg.cc
    // Beware: Very ugly. No typedefs in Java...
    // TODO: define the same operations on FloatBuffers too. Or just use some 3rd party lib...

    public static void fillZeros(float[] arr) {
        for (int i=0; i<arr.length; i++) {
            arr[i] = 0;
        }
    }

    public static void fillIdentity(float[] m) {
        fillZeros(m);
        m[0] = 1;
        m[5] = 1;
        m[10] = 1;
        m[15] = 1;
    }


    /**
     * res := a * (rotation matrix defined by angle, x, y, z)
     */
    public static void fillRotation(float[] a,
                                    float      angle,
                                    float      x,
                                    float      y,
                                    float      z,
                                    float[] res) {
        // (straight from http://www.opengl.org/sdk/docs/man/xhtml/glRotate.xml)
        float aRad = angle * (float)Math.PI / 180;
        float c = (float) Math.cos(aRad);
        float s = (float) Math.sin(aRad);

        float[] rm = new float[16];

        rm[0]  = x*x*(1-c)+c;
        rm[1]  = y*x*(1-c)+z*s;
        rm[2]  = x*z*(1-c)-y*s;
        rm[3]  = 0;

        rm[4]  = x*y*(1-c)-z*s;
        rm[5]  = y*y*(1-c)+c;
        rm[6]  = y*z*(1-c)+x*s;
        rm[7]  = 0;

        rm[8]  = x*z*(1-c)+y*s;
        rm[9]  = y*z*(1-c)-x*s;
        rm[10] = z*z*(1-c)+c;
        rm[11] = 0;

        rm[12] = 0;
        rm[13] = 0;
        rm[14] = 0;
        rm[15] = 1;

        fillMultiplication(a, rm, res);
    }


    public static void fillTranslation(float[] a,
                                       float   tx,
                                       float   ty,
                                       float   tz,
                                       float[] res) {
        float[] tm = new float[16];
        fillIdentity(tm);
        tm[12] = tx;
        tm[13] = ty;
        tm[14] = tz;
        fillMultiplication(a, tm, res);
    }

    public static void fillMultiplication(float[] a, float[] b, float[] res) {
        float[] a2 = a;
        if (a2 == res) {
            a2 = copyArr(a, null);
        }
        float[] b2 = b;
        if (b2 == res) {
            b2 = copyArr(b, null);
        }
        for (int rr = 0; rr < 4; rr++) {
            for (int rc = 0; rc < 4; rc++) {
                int ri = rc * 4 + rr;
                res[ri] = 0;
                for (int i = 0; i < 4; i++) {
                    res[ri] += a2[i * 4 + rr] * b2[rc * 4 + i];
                }
            }
        }
    }


    public static float[] copyArr(float[] src, float[] dest) {
        if (dest == null) {
            dest = new float[src.length];
        }
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }


    public static float[] cross(float[] a, float[] b, float[] dest) {
        if (dest == null) {
            dest = new float[a.length];
        }
        dest[0] = -a[2] * b[1] + a[1] * b[2];
        dest[1] = a[2] * b[0] - a[0] * b[2];
        dest[2] = -a[1] * b[0] + a[0] * b[1];
        return dest;
    }


    public static float[] multiply(float s, float[] v, float[] dest) {
        if (dest == null) {
            dest = new float[v.length];
        }
        dest[0] = s * v[0];
        dest[1] = s * v[1];
        dest[2] = s * v[2];
        return dest;
    }


    public static float length(float[] v) {
        return (float) Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
    }


    public static float[] norm(float[] v, float[] dest) {
        if (dest == null) {
            dest = new float[v.length];
        }
        float l = length(v);
        multiply(1.0F/l, v, dest);
        return dest;
    }

    public static float[] matrixJ2DtoJOGL(AffineTransform at) {
        double[] values = new double[6];
        at.getMatrix(values);

        float[] rm = new float[16];

        rm[0]  = (float) values[0];
        rm[1]  = (float) values[1];
        rm[2]  = 0;
        rm[3]  = 0;

        rm[4]  = (float) values[2];
        rm[5]  = (float) values[3];
        rm[6]  = 0;
        rm[7]  = 0;

        rm[8]  = 0;
        rm[9]  = 0;
        rm[10] = 1;
        rm[11] = 0;

        rm[12]  = (float) values[4];
        rm[13]  = (float) values[5];
        rm[14] = 0;
        rm[15] = 1;

        return rm;
    }

    public static AffineTransform matrixJOGLtoJ2D(float[] m) {
        return new AffineTransform(m[0], m[1], m[4], m[5], m[8], m[9]);
    }

}
