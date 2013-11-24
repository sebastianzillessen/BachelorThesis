package Ctrl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import View.GUIFrame;
import Display.Plots.ToneMappingPlot;
import Maths.DecimalVector;
import Solver.*;
import Solver.Image;
import tonemapping.*;

import javax.swing.*;
/**
 *
 */

/**
 * @author sebastianzillessen
 */
public class Controller {

    private GUIFrame display;

    private static Controller ourInstance = new Controller();
    private SwingWorker<HDRResult, DecimalVector> worker;
    private IHDRSolver solver;

    public static Controller getInstance() {
        return ourInstance;
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        try {

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private ArrayList<Solver.Image> images;


    private Controller() {
        display = new GUIFrame("HDR-Solver");
        display.append("Ready...");
    }

    public void solve(double lambda, final int iterations, double mu, boolean robustnessDataG, boolean robustnessSmoothnessG, IterativeEnergySolver.WEIGHTNING_MODES weight, double alpha) {

        if (solver != null && solver.getState() != IHDRSolver.StateValue.DONE) {
            solver.cancel(true);
        }

        solver = new IterativeEnergySolver(images, lambda, iterations, mu, robustnessDataG, robustnessSmoothnessG, weight, alpha);
        solver.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                try {
                    if (propertyChangeEvent.getPropertyName() == "progress")
                        display.setProgress(Integer.parseInt(propertyChangeEvent.getNewValue().toString()));

                    if (propertyChangeEvent.getPropertyName() == "state" && propertyChangeEvent.getNewValue().toString().equals("DONE")) {
                        //display.addPlot(new ToneMappingPlot(solver.get(), new LinearMapping()), "LinearMapping");
                        display.addPlot(new ToneMappingPlot(solver.get(), new LocalReinhardMapping(0.6, 0.05, 8.0, 0.18)), "LocalReinhardMapping");
                        //display.addPlot(new ToneMappingPlot(solver.get(), new HighContrast()), "HighContrast");
                        //display.addPlot(new ToneMappingPlot(solver.get(), new SpatiallyUniformMapping()), "SpatiallyUniformMapping");
                        display.addPlot(new ToneMappingPlot(solver.get(), new ReinhardMapping(0.72)), "Reinhard");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        solver.execute();

    }


    public void readImages(Map<String, Float> imgList) throws Exception {
        display.append("Reading files...");
        images = new ArrayList<Solver.Image>();
        for (Map.Entry<String, Float> e : imgList.entrySet()) {
            Image image = new Image(e.getKey(), e.getValue());
            image.addSaltAndPepper(0.05);
            images.add(image);
        }

        // align images
        //images = ImageAlignment.align(images);
        for (Image image : images) {
            display.append(image);
        }
    }


    public GUIFrame getDisplay() {
        return display;
    }
}
