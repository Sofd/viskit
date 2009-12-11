package de.sofd.viskit.model;

public interface ITransferFunction
{
    /**
     * Maps 16 bit short value to float value in range [ 0, 1 ].
     * @param x Original value.
     * @return Value of transfer function in range [ 0, 1 ].
     */
    public float getY( short x );
    
    /**
     * Get inverse value of transfer function.
     * @param y Value of transfer function in range [ 0, 1 ].
     * @return Original value.
     */
    public short getX( float y );
}