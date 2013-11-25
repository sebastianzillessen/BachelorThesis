package View;

import Ctrl.Controller;
import Display.*;
import Display.Plots.ImagePlot;
import Display.Plots.Plot;
import Solver.IterativeEnergySolver;
import View.ImageChooser.JImageChooser;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
public class GUIFrame extends JFrame implements ActionListener, Log, Runnable {

    JTabbedPane tabs = new JTabbedPane();
    private JPanel ctrlPnl;
    private ImageTableModel tableModel;
    private JTable table;
    private JTextArea log = new JTextArea();
    private JTextField prefix;
    private JButton btnStart;
    private JProgressBar progressBar;
    private double lambda = 50;
    private int iteration = 10;
    private double mu = 5;
    private String outputPrefix = "output_" + new Date().toString();
    private IterativeEnergySolver.WEIGHTNING_MODES weightning = IterativeEnergySolver.WEIGHTNING_MODES.DEFAULT;
    private long init_time = System.currentTimeMillis() / 1000;
    private double alpha = 0;
    private NumericTextField lambdaInput;
    private NumericTextField inputIterations;
    private NumericTextField inputMu;
    private NumericTextField inputAlpha;
    private boolean robustnessDataG;
    private boolean robustnessSmoothnessE;
    private boolean saltAndPepperNoise;

    private boolean updatePrefix() {
        try {
            lambda = lambdaInput.getDoubleValue().doubleValue();
            mu = inputMu.getDoubleValue().doubleValue();
            iteration = inputIterations.getLongValue().intValue();
            alpha = inputAlpha.getDoubleValue().doubleValue();
        } catch (Exception e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(this, "Fehlerhafter Input", "Die Parameter waren nicht gültig.", JOptionPane.OK_CANCEL_OPTION);
            return false;
        }
        String s = "output/" + System.currentTimeMillis() + "_";
        if (tableModel.getRowCount() > 0) {
            String[] str = tableModel.getValueAt(0, 0).toString().split("/");
            s += str[str.length - 1];
        }
        s += "_L=" + lambda;
        s += "_Mu=" + mu;
        s += "_It=" + iteration;
        s += "_RoDataG=" + (robustnessDataG ? "y" : "n");
        s += "_RoSmoothE=" + (robustnessSmoothnessE ? "y" : "n");
        s += "_We=" + weightning;
        s += "_Al=" + alpha;
        s += "_S&P=" + (saltAndPepperNoise ? "y" : "n");
        setTitle("HDR: " + s);
        if (prefix != null)
            prefix.setText(s);
        outputPrefix = s;
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ACTIONS a = ACTIONS.valueOf(actionEvent.getActionCommand());
        switch (a) {
            case CHOOSE_FILE:
                // JFileChooser-Objekt erstellen
                JImageChooser chooser = new JImageChooser(".");

                //Show it.
                int returnVal = chooser.showDialog(this,
                        "Öffnen");

                //Process the results.
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles();
                    showFileProperties(files);
                    if (files.length > 0) {
                        btnStart.setEnabled(true);
                    }
                }
                break;
            case START:
                if (updatePrefix()) {
                    this.outputPrefix = prefix.getText();
                    Map<String, Float> img = new HashMap<String, Float>();
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        img.put(tableModel.getValueAt(i, 0).toString(), (Float) tableModel.getValueAt(i, 1));
                    }
                    try {
                        Controller.getInstance().readImages(img, this.saltAndPepperNoise, false);
                        append("Images read.");
                        Controller.getInstance().solve(lambda, iteration, mu, robustnessDataG, robustnessSmoothnessE, weightning, alpha);
                    } catch (Exception e) {
                        append("Failure in Reading images: \n" + e.toString());
                        e.printStackTrace();
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Could not cast " + actionEvent.getActionCommand() + " to a ACTION.");
        }
    }


    private void showFileProperties(File[] files) {
        tableModel.setFiles(files);
        updatePrefix();
    }

    @Override
    public void append(Object s) {
        write(s + "\n");
        if (s instanceof Solver.Image) {
            final Solver.Image image = (Solver.Image) s;
            this.addPlot(new ImagePlot(image), "Image");
        }
    }


    @Override
    public void write(final Object s) {
        log.append(s.toString());
    }

    @Override
    public void run() {
        pack();
        setVisible(true);
    }

    public void setProgress(int progress) {
        progressBar.setEnabled(true);
        progressBar.setValue(progress);
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }


    private enum ACTIONS {
        CHOOSE_FILE,
        START
    }

    public GUIFrame(String name) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tabs.setPreferredSize(new Dimension(700, 700));
        getContentPane().add(tabs, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        buildCtrlPanel();
        progressBar = new JProgressBar(0, 100);
        progressBar.setEnabled(false);
        progressBar.setStringPainted(true);
        getContentPane().add(progressBar, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this);
        loadDefaultPictures();


    }

    private void loadDefaultPictures() {
        try {
            File folder = new File("Pictures/img/");
            File[] files = folder.listFiles();
            showFileProperties(files);
            if (files.length > 0) {
                btnStart.setEnabled(true);
            }
        } catch (Exception e) {
            System.out.println("Loading default pictures failed. " + e.getMessage());
        }
    }

    private void buildCtrlPanel() {
        ctrlPnl = new JPanel(new BorderLayout());
        JPanel btns = new JPanel(new GridLayout(10, 1));
        JButton btnChoose = new JButton("Bilder wählen");
        btnChoose.addActionListener(this);
        btnChoose.setActionCommand(String.valueOf(ACTIONS.CHOOSE_FILE));
        btns.add(btnChoose);

        tableModel = new ImageTableModel();
        table = new JTable(tableModel);
        LeftDotTableRenderer leftDot = new LeftDotTableRenderer();
        table.getColumnModel().getColumn(0).setCellRenderer(leftDot);
        ctrlPnl.add(new JScrollPane(table), BorderLayout.CENTER);


        btnStart = new JButton("Start");
        btnStart.setEnabled(false);
        btnStart.setActionCommand(String.valueOf(ACTIONS.START));
        btnStart.addActionListener(this);
        btns.add(btnStart);


        buildLambdaSlider(btns);
        buildMuSlider(btns);
        buildIterationSlider(btns);
        buildRobustnessSelector(btns);
        buildOutputPrefix(btns);
        buildWightPanel(btns);
        buildAlphaSlider(btns);


        ctrlPnl.add(btns, BorderLayout.NORTH);


        tabs.addTab("Control", ctrlPnl);
        tabs.addTab("LOG", new JScrollPane(log));


    }

    private void buildWightPanel(JPanel btns) {
        btns.add(new JLabel("Weighting function"));
        String[] list = {IterativeEnergySolver.WEIGHTNING_MODES.NONE.toString(), IterativeEnergySolver.WEIGHTNING_MODES.DEFAULT.toString(), IterativeEnergySolver.WEIGHTNING_MODES.PARABEL.toString()};
        JComboBox ws = new JComboBox(list);
        ws.setSelectedIndex(1);
        ws.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String cmd = (String) cb.getSelectedItem();
                weightning = IterativeEnergySolver.WEIGHTNING_MODES.valueOf(cmd);
                updatePrefix();
            }
        });
        btns.add(ws);
    }

    private void buildOutputPrefix(JPanel btns) {
        btns.add(new JLabel("Picture output prefix"));
        prefix = new JTextField(this.outputPrefix);
        btns.add(prefix);
    }

    private void buildRobustnessSelector(JPanel btns) {

        btns.add(new JLabel("Subquadratische Bestrafungsterme (Datenterm g)"));
        JCheckBox check = new JCheckBox("enable");
        check.setSelected(this.robustnessDataG);
        check.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JCheckBox box = (JCheckBox) changeEvent.getSource();
                GUIFrame.this.robustnessDataG = box.isSelected();
                updatePrefix();
            }
        });
        btns.add(check);

        btns.add(new JLabel("Subquadratische Bestrafungsterme (Glattheitsterm von E)"));
        JCheckBox check3 = new JCheckBox("enable");
        check3.setSelected(this.robustnessSmoothnessE);
        check3.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JCheckBox box = (JCheckBox) changeEvent.getSource();
                GUIFrame.this.robustnessSmoothnessE = box.isSelected();
                updatePrefix();
            }
        });
        btns.add(check3);

        btns.add(new JLabel("Salt & Pepper Rauschen?"));
        JCheckBox saltNPepper = new JCheckBox("enable");
        saltNPepper.setSelected(this.robustnessSmoothnessE);
        saltNPepper.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JCheckBox box = (JCheckBox) changeEvent.getSource();
                GUIFrame.this.saltAndPepperNoise = box.isSelected();
                updatePrefix();
            }
        });
        btns.add(saltNPepper);

    }

    private void buildIterationSlider(JPanel btns) {
        String tool = "Bestimmt wie viele Hauptiterationen durchgeführt werden soll.";
        final JLabel l = new JLabel("Iterationen= " + alpha);
        l.setToolTipText(tool);
        btns.add(l);
        inputIterations = new NumericTextField();
        inputIterations.setValue(iteration);
        btns.add(inputIterations);
    }

    private void buildLambdaSlider(JPanel btns) {
        String tool = "Bestimmt wie sehr der Glattheitsterm gewichtet werden soll.";
        final JLabel l = new JLabel("Smoothness");
        l.setToolTipText(tool);
        btns.add(l);

        lambdaInput = new NumericTextField();
        lambdaInput.setValue(lambda);
        btns.add(lambdaInput);
    }

    private void buildMuSlider(JPanel btns) {
        String tool = "Bestimmt wie sehr der Monotonie-Term gewichtet werden soll.";
        final JLabel l = new JLabel("Monotonie");
        l.setToolTipText(tool);
        btns.add(l);
        inputMu = new NumericTextField();
        inputMu.setValue(mu);
        btns.add(inputMu);
    }

    private void buildAlphaSlider(JPanel btns) {
        String tool = "Bestimmt wie sehr der Räumliche-Glattheitsterm gewichtet werden soll.";
        final JLabel l = new JLabel("Räumlicher Glattheitsterm");
        l.setToolTipText(tool);
        btns.add(l);

        inputAlpha = new NumericTextField();
        inputAlpha.setValue(alpha);
        btns.add(inputAlpha);
    }

    public void addPlot(Plot p, String name) {
        tabs.addTab(name, p);
        p.setOutputFileName(this.outputPrefix + "_" + name + "_" + init_time);
    }

}
