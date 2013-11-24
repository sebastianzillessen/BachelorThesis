package View.ImageChooser;

import javax.swing.*;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 19.11.13
 * Time: 22:14
 * To change this template use File | Settings | File Templates.
 */
public class JImageChooser extends JFileChooser {
    public JImageChooser(String folder) {
        super();
        setAcceptAllFileFilterUsed(false);
        setFileFilter(new ImageFilter());
        setFileView(new ImageFileView());
        setAccessory(new ImagePreview(this));
        setCurrentDirectory(new File(folder));
        setMultiSelectionEnabled(true);
    }
}
