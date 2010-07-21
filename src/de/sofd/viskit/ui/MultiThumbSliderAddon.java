package de.sofd.viskit.ui;

import org.jdesktop.swingx.plaf.AbstractComponentAddon;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

/**
*
* @author jm158417
*/
public class MultiThumbSliderAddon extends AbstractComponentAddon {
   
   /** Creates a new instance of MultiThumbSliderAddon */
   public MultiThumbSliderAddon() {
       super("JXMultiThumbSlider");
   }
   
   @Override
   protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
       super.addBasicDefaults(addon, defaults);
       
       defaults.add(JXMultiThumbSlider.uiClassID,
               "de.sofd.viskit.ui.BasicMultiThumbSliderUI");
   }
   
}