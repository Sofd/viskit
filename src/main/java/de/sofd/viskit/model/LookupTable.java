package de.sofd.viskit.model;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public interface LookupTable {

    String getName();

    /**
     *
     * @return the LUT's color values, as an r-g-b-a-r-g-b-a-r-g-b-a-r-g-b-a... table
     *         with the values being floats scaled to the 0..1 range
     *
     */
    FloatBuffer getRGBAValues();

    /**
     *
     * @return the LUT's color values, as an r-g-b-a-r-g-b-a-r-g-b-a-r-g-b-a... table
     *         with the values being floats scaled to the 0..255 range
     *
     */
    FloatBuffer getRGBA256floatValues();

    /**
     *
     * @return the LUT's color values, as an r-g-b-a-r-g-b-a-r-g-b-a-r-g-b-a... table
     *         with the values being ints scaled to the 0..255 range
     *
     */
    IntBuffer getRGBA256intValues();

    /**
     *
     * @return the LUT's color values, as r-g-b-a arrays
     *         with the values being floats scaled to the 0..1 range
     *
     */
    float[][] getRGBAfloatArrays();

    /**
     *
     * @return the LUT's color values, as r-g-b-a arrays
     *         with the values being floats scaled to the 0..255 range
     *
     */
    float[][] getRGBA256floatArrays();

    /**
     *
     * @return the LUT's color values, as r-g-b-a arrays
     *         with the values being ints scaled to the 0..255 range
     *
     */
    int[][] getRGBA256intArrays();
}
