package Ctrl;

import Maths.Vector;
import Model.WeightMode;
import View.Plots.ToneMappingPlot;
import Model.HDRResult;
import Solver.IHDRSolver;
import Model.Image;
import Solver.IterativeEnergySolver;
import View.GUIFrame;
import View.ToneMappers.LocalReinhardMapping;
import View.ToneMappers.ReinhardMapping;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Map;
/**
 *
 */

/**
 * @author sebastianzillessen
 */
public class Controller {

    public static final double PERCENTAGE_OF_SALT_N_PEPPER = 0.02;
    private GUIFrame display;

    private static Controller ourInstance = new Controller();
    private SwingWorker<HDRResult, Vector> worker;
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

    private ArrayList<Image> images;


    private Controller() {
        display = new GUIFrame("HDR-Solver");
        display.append("Ready...");
    }

    public void solve(double lambda, final int iterations, double mu, boolean robustnessDataG, boolean robustnessSmoothnessE, WeightMode weight, double alpha) {

        if (solver != null && solver.getState() != IHDRSolver.StateValue.DONE) {
            solver.cancel(true);
        }

        solver = new IterativeEnergySolver(images, lambda, iterations, mu, robustnessDataG, robustnessSmoothnessE, weight, alpha);
        display.append(solver.toString());
        solver.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                try {
                    if (propertyChangeEvent.getPropertyName() == "progress")
                        display.setProgress(Integer.parseInt(propertyChangeEvent.getNewValue().toString()));

                    if (propertyChangeEvent.getPropertyName() == "state" && propertyChangeEvent.getNewValue().toString().equals("DONE")) {
                        display.addPlot(new ToneMappingPlot(solver.get(), new LocalReinhardMapping(0.6, 0.05, 8.0, 0.18)), "LocalReinhardMapping");
                        display.addPlot(new ToneMappingPlot(solver.get(), new ReinhardMapping(0.72)), "Reinhard");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        solver.execute();

    }


    public void readImages(Map<String, Float> imgList, boolean saltAndPepperNoise, double gaussianNoise) throws Exception {
        display.append("Reading files...");
        images = new ArrayList<Image>();
        for (Map.Entry<String, Float> e : imgList.entrySet()) {
            Image image = new Image(e.getKey(), e.getValue());
            images.add(image);
        }

        for (Image image : images) {

            // add salt and pepper nois if required
            if (saltAndPepperNoise)
                image.addSaltAndPepper(PERCENTAGE_OF_SALT_N_PEPPER);
            // add gauss noise if required
            if (gaussianNoise > 0)
                image.addGaussian(gaussianNoise);
            display.append(image);
        }



    }


    public GUIFrame getDisplay() {
        return display;
    }
}
