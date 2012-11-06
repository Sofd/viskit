package de.sofd.viskit.image3D.jogl.minigui.layout;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class BorderLayout extends Layout
{
    public final static int CENTER = 0;
    public final static int EAST = 2;
    public final static int NORTH = 1;
    public final static int SOUTH = 3;
    public final static int WEST = 4;

    // top, right, bottom, left
    protected int[] margin = new int[ 4 ];

    public BorderLayout( int top, int right, int bottom, int left )
    {
        super( 5 );
        setMargin( top, right, bottom, left );
    }

    public void add(    Component component,
                        int direction )
    {
        this.components.set( direction, component );
    }

    public int[] getMargin()
    {
        return margin;
    }

    @Override
    public Size getPreferredSize(    int width,
                                    int height )
    {
        int prefWidth = 0;
        int prefHeight = 0;

        Size[] sizes = getPreferredSizes( width, height );

        prefWidth = Math.min( Math.min( sizes[ NORTH ].getWidth(), sizes[ SOUTH ].getWidth() ), sizes[ WEST ]
                .getWidth()
                + sizes[ CENTER ].getWidth() + sizes[ EAST ].getWidth() );

        prefHeight = sizes[ NORTH ].getHeight()
                + Math.min( Math.min( sizes[ EAST ].getHeight(), sizes[ WEST ].getHeight() ), sizes[ CENTER ]
                        .getHeight() ) + sizes[ SOUTH ].getHeight();

        return new Size( prefWidth, prefHeight );
    }

    protected Size[] getPreferredSizes( int width,
                                        int height )
    {
        Size[] sizes = new Size[ 5 ];

        int w1 = margin[ 3 ];
        int w2 = width - margin[ 1 ] - margin[ 3 ];
        int w3 = margin[ 1 ];
        int h1 = margin[ 0 ];
        int h2 = height - margin[ 0 ] - margin[ 2 ];
        int h3 = margin[ 2 ];

        int[][] compSize = { { w2, h2 }, { width, h1 }, { w3, h2 }, { width, h3 }, { w1, h2 } };

        for ( int i = 0; i < 5; ++i )
            if ( components.get( i ) != null )
                sizes[ i ] = components.get( i ).getPreferredSize( compSize[ i ][ 0 ], compSize[ i ][ 1 ] );
            else
                sizes[ i ] = new Size( compSize[ i ][ 0 ], compSize[ i ][ 1 ] );

        return sizes;
    }

    @Override
    public void pack(    int x,
                        int y,
                        int width,
                        int height )
    {
        Size[] sizes = getPreferredSizes( width, height );

        int prefWidth = Math.min( Math.min( sizes[ NORTH ].getWidth(), sizes[ SOUTH ].getWidth() ), sizes[ WEST ]
                .getWidth()
                + sizes[ CENTER ].getWidth() + sizes[ EAST ].getWidth() );

        int prefCenHeight = Math.min( Math.min( sizes[ EAST ].getHeight(), sizes[ WEST ].getHeight() ), sizes[ CENTER ]
                .getHeight() );

        int cx = x;
        int cy = y;

        int dir = SOUTH;
        if ( components.get( dir ) != null )
            components.get( dir ).pack( cx, cy, prefWidth, sizes[ dir ].getHeight() );

        cy += sizes[ dir ].getHeight();

        dir = WEST;
        if ( components.get( dir ) != null )
            components.get( dir ).pack( cx, cy, sizes[ dir ].getWidth(), prefCenHeight );

        cx += sizes[ dir ].getWidth();

        dir = CENTER;
        if ( components.get( dir ) != null )
            components.get( dir ).pack( cx, cy, sizes[ dir ].getWidth(), prefCenHeight );

        cx += sizes[ dir ].getWidth();

        dir = EAST;
        if ( components.get( dir ) != null )
            components.get( dir ).pack( cx, cy, sizes[ dir ].getWidth(), prefCenHeight );

        cx = x;
        cy += sizes[ CENTER ].getHeight();

        dir = NORTH;
        if ( components.get( dir ) != null )
            components.get( dir ).pack( cx, cy, prefWidth, sizes[ dir ].getHeight() );
    }

    @Override
    public void resize( int x,
                        int y,
                        int width,
                        int height )
    {
        int w1 = margin[ 3 ];
        int w2 = width - margin[ 1 ] - margin[ 3 ];
        int w3 = margin[ 1 ];
        int h1 = margin[ 0 ];
        int h2 = height - margin[ 0 ] - margin[ 2 ];
        int h3 = margin[ 2 ];

        if ( components.get( CENTER ) != null )
            components.get( CENTER ).resize( x + w1, y + h3, w2, h2 );

        if ( components.get( NORTH ) != null )
            components.get( NORTH ).resize( x, y + h2 + h3, width, h1 );

        if ( components.get( EAST ) != null )
            components.get( EAST ).resize( x + w1 + w2, y + h3, w3, h2 );

        if ( components.get( SOUTH ) != null )
            components.get( SOUTH ).resize( x, y, width, h3 );

        if ( components.get( WEST ) != null )
            components.get( WEST ).resize( x, y + h3, w1, h2 );

    }

    public void setMargin(    int top,
                            int right,
                            int bottom,
                            int left )
    {
        margin[ 0 ] = top;
        margin[ 1 ] = right;
        margin[ 2 ] = bottom;
        margin[ 3 ] = left;

    }

}