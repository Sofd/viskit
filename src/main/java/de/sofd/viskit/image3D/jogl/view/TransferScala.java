package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.io.*;

import javax.media.opengl.*;

import com.sun.opengl.util.gl2.*;

import de.sofd.util.*;
import de.sofd.util.properties.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;

public class TransferScala extends Component {

    protected final static int FONT_MAX_WIDTH = 50;
    protected final static int FONT_MAX_HEIGHT = 10;
    
    protected Size size;

    protected int lineGap;

    protected int lineWidth;

    protected int labelMinQuantity;

    protected int labelUnitLevel1;
    protected int labelUnitLevel2;
    protected int labelUnitLevel3;

    protected TransferFunction transferFunction;
    
    protected IntRange range = new IntRange(0, 1000);

    public TransferScala(int x, int y, TransferFunction transferFunction, ExtendedProperties props) throws IOException {
        super(x, y, 1, 1);
        
        size = new Size(props.getI("volumeConfig.gui.transfer.scala.width"), props.getI("volumeConfig.gui.transfer.scala.height"));
        lineGap = props.getI("volumeConfig.gui.transfer.scala.line.gap");
        lineWidth = props.getI("volumeConfig.gui.transfer.scala.line.width");
        labelMinQuantity = props.getI("volumeConfig.gui.transfer.scala.label.minQuantity");
        labelUnitLevel1 = props.getI("volumeConfig.gui.transfer.scala.label.unit.level1");
        labelUnitLevel2 = props.getI("volumeConfig.gui.transfer.scala.label.unit.level2");
        labelUnitLevel3 = props.getI("volumeConfig.gui.transfer.scala.label.unit.level3");

        setWidth(size.getWidth() + lineWidth + FONT_MAX_WIDTH);
        setHeight(size.getHeight());
        
        this.transferFunction = transferFunction;
        
        

    }
    
    public void setRange( int min, int max ) {
        range.setMin(min);
        range.setMax(max);
    }
    
    public void show( GL2 gl, GLUT glut )
    {
        int x4 = x + width;
        int x3 = x4 - size.getWidth();
        int x2 = x3 - lineWidth;
        
        int y2 = y + height;
        
        gl.glEnable( GL_TEXTURE_1D );
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );
        
        transferFunction.bindTexture(gl);

        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GLUtil.texQuad1D( gl, x3, y, size.getWidth(), size.getHeight() , true);
        
        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_1D );
        
        gl.glLineWidth(1.0f);
        GLUtil.lineQuad(gl, x3, y, size.getWidth(), size.getHeight());
        
        int lines = 1 + size.getHeight() / lineGap;
        
        gl.glBegin(GL_LINES);
            for ( int i = 0; i < lines; ++i ) {
                gl.glVertex2f(x2, y2 - i * lineGap);
                gl.glVertex2f(x3, y2 - i * lineGap);
            }
        gl.glEnd();
        
        int labelUnitLevel = labelUnitLevel1;
        
        int valueGap = (int)Math.floor( getVisibleRange() * 1.0 / ( labelUnitLevel1 * (labelMinQuantity - 1) ) ) * labelUnitLevel1;
        if ( valueGap == 0 ) {
            valueGap = (int)Math.floor( getVisibleRange() * 1.0 / ( labelUnitLevel2 * (labelMinQuantity - 1) ) ) * labelUnitLevel2;
            labelUnitLevel = labelUnitLevel2;
        }
        
        if ( valueGap == 0 ) {
            valueGap = (int)Math.floor( getVisibleRange() * 1.0 / ( labelUnitLevel3 * (labelMinQuantity - 1) ) ) * labelUnitLevel3;
            labelUnitLevel = labelUnitLevel3;
        }
        
        int valueStart =( (int)Math.floor(( getVisibleMin() - 1 ) * 1.0 / labelUnitLevel ) + 1 ) * labelUnitLevel;
        int visibleMax = getVisibleMax();
        
        int value = valueStart;
        
        while ( value <= visibleMax ) {
            infoText(gl, glut, x, getValueY(value), String.format("%+6d", value));
            value += valueGap;
        }
        
    }
    
    private int getVisibleMin() {
        return range.getMin() + ( range.getDelta() - getVisibleRange() ) / 2;
    }
    
    private int getVisibleMax() {
        return range.getMax() - ( range.getDelta() - getVisibleRange() ) / 2;
    }

    private int getVisibleRange() {
        return (int)( ( 1 - FONT_MAX_HEIGHT * 1.0f / size.getHeight() ) * range.getDelta() );
    }

    private int getValueY(int value) {
        return ( y + ( ( value - range.getMin() ) * size.getHeight() ) / range.getDelta() ) - FONT_MAX_HEIGHT / 2;
    }
    
    

}