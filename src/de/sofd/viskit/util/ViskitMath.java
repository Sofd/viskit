package de.sofd.viskit.util;

import java.io.*;

//Hilfsklasse für mathematische Berechnungen
public class ViskitMath {
    public static void matMul(float w[], float M[], float v[]) {
        for (int i = 0; i < 4; ++i) {
            w[i] = vecDot4f(M, i * 4, v, 0);
        }
    }

    public static float vecDot4f(float v1[], int vi1, float v2[], int vi2) {
        return v1[vi1 + 0] * v2[vi2 + 0] + v1[vi1 + 1] * v2[vi2 + 1] + v1[vi1 + 2] * v2[vi2 + 2] + v1[vi1 + 3] * v2[vi2 + 3];
    }

    public static void vecPrintnf(PrintStream ps, String s, float v[], int n) {
        ps.printf("%s", s);

        for (int i = 0; i < n; ++i) {
            ps.printf(" %f", v[i]);
        }

        ps.printf("\n");
    }
}