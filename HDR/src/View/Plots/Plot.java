package View.Plots;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */
public abstract class Plot extends JPanel {

    private final JPanel pnl;
    protected String outputFileName;
    private boolean zoom = false;


    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    protected void redraw() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                pnl.repaint();
            }
        });
    }

    public enum Scale {
        LINEAR,
        LOG
    }

    protected abstract void paintPlot(Graphics g);

    protected int getPlotWidth() {
        return pnl.getWidth();
    }


    protected int getPlotHeight() {
        return pnl.getHeight();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        pnl.repaint();
    }

    public Plot() {
        super(new BorderLayout());
        buildControls();
        pnl = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintPlot(g);
            }
        };
        pnl.setPreferredSize(new Dimension(getWidth(), getHeight()));
        add(pnl, BorderLayout.CENTER);
        pnl.repaint();
    }


    private void buildControls() {
        JButton save = new JButton("Save");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Plot.this.save();
            }
        });
        add(save, BorderLayout.NORTH);

    }

    protected void save() {
        this.saveGraphic(outputFileName);
    }


    public void saveGraphic(String filename, BufferedImage buff) {
        if (!filename.endsWith(".png"))
            filename += ".png";
        try {
            ImageIO.write(buff, "png", new File(filename));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void saveGraphic(String filename) {
        BufferedImage bi = new BufferedImage(getPlotWidth(), getPlotHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        this.pnl.paint(g);  //this == JComponent
        g.dispose();
        saveGraphic(filename, bi);
    }

}
