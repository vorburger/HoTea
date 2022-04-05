package ch.vorburger.hotea.examples.swing;

import ch.vorburger.hotea.HotClassLoader.Listener;
import ch.vorburger.hotea.HotClassLoaderBuilder;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class HotSwingExampleMain implements Listener {

    public interface Drawer {
        void draw(JFrame frame);
    }

    JFrame jFrame;
    String drawerImplementationClassName;
    Drawer drawer;

    private void createAndShowGUI() {
        jFrame = new JFrame("HOT HelloWorldSwing");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setLocation(500, 100);
        jFrame.setVisible(true);
    }

    private void run(File pluginClassesBaseDir, String drawerImplementationClassName) throws Exception {
        javax.swing.SwingUtilities.invokeLater(this::createAndShowGUI);
        Thread.yield();
        Thread.sleep(100);

        this.drawerImplementationClassName = drawerImplementationClassName;

        new HotClassLoaderBuilder().addClasspathEntry(pluginClassesBaseDir).addListener(this).build();
    }

    @SuppressWarnings("unchecked")
    @Override public void onReload(ClassLoader newClassLoader) throws Throwable {
        Class<Drawer> drawerClass = (Class<Drawer>) newClassLoader.loadClass(drawerImplementationClassName);
        drawer = drawerClass.getDeclaredConstructor().newInstance();
        jFrame.getContentPane().removeAll();
        drawer.draw(jFrame);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        new HotSwingExampleMain().run(new File("../ch.vorburger.hotea.examples.swing/target/classes"),
                "ch.vorburger.hotea.examples.swing1.ExampleSwingDrawer");
    }
}
