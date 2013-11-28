package View.ImageChooser;

import javax.swing.*;
import java.io.File;

/**
 * File chooser that allows only to select images and offers a preview for the user.
 *
 * @author sebastianzillessen
 * @see http://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html#FileChooserDemo2
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
