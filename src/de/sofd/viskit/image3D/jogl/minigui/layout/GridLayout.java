package de.sofd.viskit.image3D.jogl.minigui.layout;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class GridLayout extends Layout
{
    protected int columns;

    protected int rows;

    public GridLayout( int rows, int columns )
    {
        super( rows * columns );
        this.rows = rows;
        this.columns = columns;
    }

    public void add(    Component component,
                        int row,
                        int column )
    {
        this.components.set( row * columns + column, component );
    }

    protected void getPreferredRaster(    int[] colWidth,
                                        int[] rowHeight,
                                        int compWidth,
                                        int compHeight )
    {
        Size[][] sizes = new Size[ rows ][ columns ];

        for ( int col = 0; col < columns; ++col )
        {
            for ( int row = 0; row < rows; ++row )
            {
                Component comp = components.get( row * columns + col );

                if ( comp == null )
                    sizes[ row ][ col ] = new Size( compWidth, compHeight );
                else
                    sizes[ row ][ col ] = comp.getPreferredSize( compWidth, compHeight );

            }
        }

        for ( int col = 0; col < columns; ++col )
        {
            colWidth[ col ] = compWidth;

            for ( int row = 0; row < rows; ++row )
                colWidth[ col ] = Math.min( colWidth[ col ], sizes[ row ][ col ].getWidth() );
        }

        for ( int row = 0; row < rows; ++row )
        {
            rowHeight[ row ] = compHeight;

            for ( int col = 0; col < columns; ++col )
                rowHeight[ row ] = Math.min( rowHeight[ row ], sizes[ row ][ col ].getHeight() );
        }

    }

    @Override
    public Size getPreferredSize(    int width,
                                    int height )
    {
        int prefWidth = 0;
        int prefHeight = 0;

        int[] colWidth = new int[ columns ];
        int[] rowHeight = new int[ rows ];

        getPreferredRaster( colWidth, rowHeight, width / columns, height / rows );

        for ( int rh : rowHeight )
            prefHeight += rh;
        for ( int cw : colWidth )
            prefWidth += cw;

        return new Size( prefWidth, prefHeight );
    }

    @Override
    public void pack(    int x,
                        int y,
                        int width,
                        int height )
    {
        int[] colWidth = new int[ columns ];
        int[] rowHeight = new int[ rows ];

        getPreferredRaster( colWidth, rowHeight, width / columns, height / rows );

        int cy = y;

        for ( int row = rows - 1; row >= 0; --row )
        {

            int cx = x;

            for ( int col = 0; col < columns; ++col )
            {
                Component comp = components.get( row * columns + col );

                if ( comp != null )
                {
                    comp.pack( cx, cy, colWidth[ col ], rowHeight[ row ] );
                }

                cx += colWidth[ col ];
            }

            cy += rowHeight[ row ];
        }

    }

    @Override
    public void resize( int x,
                        int y,
                        int width,
                        int height )
    {

        int index = -1;
        int compWidth = width / columns;
        int compHeight = height / rows;

        for ( Component component : components )
        {
            index++;

            if ( component == null )
                continue;

            int colIndex = index % columns;
            int rowIndex = rows - index / columns - 1;

            if ( component.isMaximized() )
                component.resize( x, y, width, height );
            else
                component.resize( x + ( colIndex * width ) / columns, y + ( rowIndex * height ) / rows, compWidth,
                        compHeight );
        }
    }

}