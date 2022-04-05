package ch.vorburger.hotea.examples.swing;

import java.io.File;

import javax.swing.JFrame;

import ch.vorburger.hotea.HotClassLoader;
import ch.vorburger.hotea.HotClassLoader.Listener;
import ch.vorburger.hotea.HotClassLoaderBuilder;

public class HotSwingExampleMain implements Listener {

    public interface Drawer {
        void draw(JFrame frame);
    }

    JFrame jFrame;
    String drawerImplementationClassName;
    Drawer drawer;
    
    private void createAndShowGUI() {
        jFrame = new JFrame("HOT HelloWorldSwing");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocation(500, 100);
        jFrame.setVisible(true);
    }
    
    private void run(File pluginClassesBaseDir, String drawerImplementationClassName) throws Exception {
        javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI()); Thread.yield(); Thread.sleep(100);
        
        this.drawerImplementationClassName = drawerImplementationClassName;
        
        @SuppressWarnings("unused") HotClassLoader hcl = 
                new HotClassLoaderBuilder().addClasspathEntry(pluginClassesBaseDir).addListener(this).build();
        // hcl.close(); // TODO where should this go in a Swing app? Can NOT do it here.. 
    }

    @SuppressWarnings("unchecked")
    @Override public void onReload(ClassLoader newClassLoader) throws Throwable {
        Class<Drawer> drawerClass = (Class<Drawer>) newClassLoader.loadClass(drawerImplementationClassName);
        drawer = drawerClass.newInstance();
        jFrame.getContentPane().removeAll();
        drawer.draw(jFrame);
    }

    public static void main(String[] args) throws Exception {
        // System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        new HotSwingExampleMain().run(
                new File("../ch.vorburger.hotea.examples.swing/target/classes"), 
                "ch.vorburger.hotea.examples.swing1.ExampleSwingDrawer");
    }

}
