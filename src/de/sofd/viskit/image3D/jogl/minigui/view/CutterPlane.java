package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import javax.media.opengl.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.minigui.util.*;

public class CutterPlane extends DragTexComponent
{
    protected FloatRange horiRange;

    protected FloatRange vertiRange;

    protected float epsilon = 3.0f;

    public CutterPlane( int x, int y, int width, int height, FloatRange horiRange, FloatRange vertiRange )
    {
        super( x, y, width, height, null, new float[]
        {
                1.0f, 1.0f, 1.0f, 0.5f
        }, null );

        this.horiRange = horiRange;
        this.vertiRange = vertiRange;
    }

    public float getBottomBoundY()
    {
        return ( y + vertiRange.getMin() * height );
    }

    public FloatRange getHoriRange()
    {
        return horiRange;
    }

    public float getLeftBoundX()
    {
        return ( x + horiRange.getMin() * width );
    }

    public float getRightBoundX()
    {
        return ( x + horiRange.getMax() * width );
    }

    public float getTopBoundY()
    {
        return ( y + vertiRange.getMax() * height );
    }

    public FloatRange getVertiRange()
    {
        return vertiRange;
    }

    @Override
    public void glCreate() throws Exception
    {
        tex = ResourceLoader.getImageTex( "minigui.cutter.bg" );

        tex.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        tex.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_NEAREST );

        tex.setTexParameteri( GL_TEXTURE_WRAP_T, GL_REPEAT );
        tex.setTexParameteri( GL_TEXTURE_WRAP_S, GL_REPEAT );
    }

    public void setHoriMax( int horiMax )
    {
        float oldHoriMax = getRightBoundX();
        
        if ( horiMax > oldHoriMax || horiMax - getLeftBoundX() >= 2 * epsilon )
            horiRange.setMax( getRelativeX( horiMax ) );
    }

    public void setHoriMin( int horiMin )
    {
        float oldHoriMin = getLeftBoundX();
        
        if ( horiMin < oldHoriMin || getRightBoundX() - horiMin >= 2 * epsilon )
            horiRange.setMin( getRelativeX( horiMin ) );
    }

    public void setHoriRange( FloatRange horiRange )
    {
        this.horiRange = horiRange;
    }

    public void setVertiMax( int vertiMax )
    {
        float oldVertiMax = getTopBoundY();
        
        if ( vertiMax > oldVertiMax || vertiMax - getBottomBoundY() >= 2 * epsilon )
            vertiRange.setMax( getRelativeY( vertiMax ) );
    }

    public void setVertiMin( int vertiMin )
    {
        float oldVertiMin = getBottomBoundY();
        
        if ( vertiMin < oldVertiMin || getTopBoundY() - vertiMin >= 2 * epsilon )
            vertiRange.setMin( getRelativeY( vertiMin ) );
    }

    public void setVertiRange( FloatRange vertiRange )
    {
        this.vertiRange = vertiRange;
    }

    @Override
    public void show( GL2 gl )
    {

        gl.glEnable( GL_TEXTURE_2D );

        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );

        tex.bind();
        gl.glColor4fv( color, 0 );

        float ovx1 = x;
        float ovy1 = y;
        float ovx2 = x + width;
        float ovy2 = y + height;
        float otx1 = 0;
        float oty1 = 0;
        float otx2 = width * 1.0f / tex.getWidth();
        float oty2 = height * 1.0f / tex.getHeight();

        float itx1 = width * horiRange.getMin() / tex.getWidth();
        float ity1 = height * vertiRange.getMin() / tex.getHeight();
        float itx2 = width * horiRange.getMax() / tex.getWidth();
        float ity2 = height * vertiRange.getMax() / tex.getHeight();

        float ivx1 = getLeftBoundX();
        float ivy1 = getBottomBoundY();
        float ivx2 = getRightBoundX();
        float ivy2 = getTopBoundY();

        gl.glBegin( GL2.GL_QUADS );
        gl.glNormal3f( 0, 0, 1 );

        gl.glTexCoord2f( otx1, oty1 );
        gl.glVertex2f( ovx1, ovy1 );
        gl.glTexCoord2f( otx2, oty1 );
        gl.glVertex2f( ovx2, ovy1 );
        gl.glTexCoord2f( itx2, ity1 );
        gl.glVertex2f( ivx2, ivy1 );
        gl.glTexCoord2f( itx1, ity1 );
        gl.glVertex2f( ivx1, ivy1 );

        gl.glTexCoord2f( otx2, oty1 );
        gl.glVertex2f( ovx2, ovy1 );
        gl.glTexCoord2f( otx2, oty2 );
        gl.glVertex2f( ovx2, ovy2 );
        gl.glTexCoord2f( itx2, ity2 );
        gl.glVertex2f( ivx2, ivy2 );
        gl.glTexCoord2f( itx2, ity1 );
        gl.glVertex2f( ivx2, ivy1 );

        gl.glTexCoord2f( otx2, oty2 );
        gl.glVertex2f( ovx2, ovy2 );
        gl.glTexCoord2f( otx1, oty2 );
        gl.glVertex2f( ovx1, ovy2 );
        gl.glTexCoord2f( itx1, ity2 );
        gl.glVertex2f( ivx1, ivy2 );
        gl.glTexCoord2f( itx2, ity2 );
        gl.glVertex2f( ivx2, ivy2 );

        gl.glTexCoord2f( otx1, oty2 );
        gl.glVertex2f( ovx1, ovy2 );
        gl.glTexCoord2f( otx1, oty1 );
        gl.glVertex2f( ovx1, ovy1 );
        gl.glTexCoord2f( itx1, ity1 );
        gl.glVertex2f( ivx1, ivy1 );
        gl.glTexCoord2f( itx1, ity2 );
        gl.glVertex2f( ivx1, ivy2 );
        gl.glEnd();

        gl.glDisable( GL_BLEND );

        gl.glDisable( GL_TEXTURE_2D );

    }
    
    public boolean touchesBottomBound( int mouseY )
    {
        return ( Math.abs( getBottomBoundY() - mouseY ) < epsilon && mouseY >= y );
    }
    
    public boolean touchesLeftBound( int mouseX )
    {
        return ( Math.abs( getLeftBoundX() - mouseX ) < epsilon && mouseX >= x );
    }

    public boolean touchesRightBound( int mouseX )
    {
        return ( Math.abs( getRightBoundX() - mouseX ) < epsilon && mouseX <= x + width );
    }
    
    public boolean touchesTopBound( int mouseY )
    {
        return ( Math.abs( getTopBoundY() - mouseY ) < epsilon && mouseY <= y + height );
    }

    

}