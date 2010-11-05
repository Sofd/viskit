package de.sofd.viskit.ui.twl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBoxDisplay;
import de.matthiasmann.twl.renderer.DynamicImage;
import de.matthiasmann.twl.renderer.Renderer;
import de.sofd.viskit.model.LookupTable;

public class LutListBox extends ListBox<LookupTable> {
    
    @Override
    protected ListBoxDisplay createDisplay() {
        return new LutBoxDisplay();
    }
    
    //TODO public only for testing, protected
    public static class LutBoxDisplay extends ListBoxLabel {
        private DynamicImage background;
        private LookupTable lut;
        
        @Override
        public void setData(Object data) {
            super.setData(data);
            if(data instanceof LookupTable) {
                lut = (LookupTable)data;
            }
        }
        
        @Override
        protected void paintBackground(GUI gui) {
            if(background == null) {
                createBackgroundImage(gui.getRenderer());
            }
//            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
//            background.draw(getAnimationState(), getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());            
//            GL11.glPointSize(10);
//            GL11.glBegin(GL11.GL_POINTS);
//            GL11.glColor3f(1, 0, 0);
//            GL11.glVertex2d(getInnerX(), getInnerY());
//            GL11.glEnd();
        }

        private void createBackgroundImage(Renderer renderer) {
            background = renderer.createDynamicImage(256, 1);            
            
            FloatBuffer lutBuffer = lut.getRGBAValues();
            ByteBuffer bb = ByteBuffer.allocateDirect(1024*Float.SIZE/8);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer fb = bb.asFloatBuffer();
        
            fb.put(lutBuffer.array());
//            for (int i = 0; i < 1024; i++) {
//                fb.put(i, lutBuffer.get(i));
//            }
            background.update(bb, DynamicImage.Format.RGBA);
        }
    }
}
