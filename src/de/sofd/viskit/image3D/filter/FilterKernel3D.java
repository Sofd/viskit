package de.sofd.viskit.image3D.filter;

public class FilterKernel3D
{
    protected float[] values;
    
    protected int dim;

    public FilterKernel3D( float[] values, int dim )
    {
        super();
        this.values = values;
        this.dim = dim;
    }

    public int getDim()
    {
        return dim;
    }

    public float[] getValues()
    {
        return values;
    }
    
    public void normalize()
    {
        float valSum=0;
        
        for ( float value : values )
            valSum += value;
        
        for ( int i = 0; i < values.length; ++i )
            values[i] /= valSum;
    }

    public void setDim( int dim )
    {
        this.dim = dim;
    }

    public void setValues( float[] values )
    {
        this.values = values;
    }
    
    @Override
    public String toString()
    {
        String s = "[ ";
        
        for ( int i = 0; i < values.length; ++i )
            s += values[i] + " ";
        
        s += "]";
        
        return s;
        
    }
}