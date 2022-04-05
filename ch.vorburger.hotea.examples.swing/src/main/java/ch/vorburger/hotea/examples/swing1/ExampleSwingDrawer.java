package ch.vorburger.hotea.examples.swing1;

import ch.vorburger.hotea.examples.swing.HotSwingExampleMain;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ExampleSwingDrawer implements HotSwingExampleMain.Drawer {

    @Override
    public void draw(JFrame frame) {
        JLabel label = new JLabel(" hello, world ");
        label.setFont(new Font("Courier", Font.BOLD, 48));
        frame.getContentPane().add(label);
        frame.pack();
    }

}
