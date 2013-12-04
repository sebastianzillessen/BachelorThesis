package View;

import Ctrl.Controller;
import Model.Image;
import Model.WeightMode;
import View.ImageChooser.JImageChooser;
import View.ImageTable.ImageTableModel;
import View.ImageTable.LeftDotTableRenderer;
import View.NumericTextField.NumericTextField;
import View.Plots.ImagePlot;
import View.Plots.Plot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
public class GUIFrame extends JFrame implements ActionListener, Log, Runnable {

    // UI Elements
    private JTabbedPane tabs = new JTabbedPane();
    private JPanel ctrlPnl;
    private ImageTableModel tableModel;
    private JTable table;
    private JTextArea log = new JTextArea();
    private JTextField prefix;
    private JButton btnStart;
    private JProgressBar progressBar;
    private NumericTextField lambdaInput;
    private NumericTextField inputIterations;
    private NumericTextField inputMu;
    private NumericTextField inputAlpha;
    private NumericTextField inputDevStd;

    private final DecimalFormat format = new DecimalFormat("###.###");
    private NumericTextField[] nums = new NumericTextField[5];
    private Controller ctrl = Controller.getInstance();

    // Parameters
    private double lambda = 50;
    private int iteration = 10;
    private double mu = 5;
    private WeightMode weightning = WeightMode.DEFAULT;
    private long init_time = System.currentTimeMillis() / 1000;
    private boolean robustnessDataG = false;
    private boolean robustnessSmoothnessE = false;
    private boolean saltAndPepperNoise = false;
    private double devStd = 0;
    private double alpha = 0;

    private List<Plot> plots = new ArrayList<Plot>();
    private String outputPrefix = "output_" + new Date().toString();
    private JImageChooser chooser;

    private enum ACTIONS {
        CHOOSE_FILE,
        START
    }


    /**
     * Action Handler for the button events on the UI
     *
     * @param actionEvent
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ACTIONS a = ACTIONS.valueOf(actionEvent.getActionCommand());
        switch (a) {
            case CHOOSE_FILE:
                // create JFileChooser-Object
                chooser = new JImageChooser(".");
                //Show it.
                int returnVal = chooser.showDialog(this,
                        "Öffnen");
                //Process the results.
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles();
                    showFileProperties(files);
                    if (files.length > 0) {
                        btnStart.setEnabled(true);
                    } else {
                        btnStart.setEnabled(false);
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
                        Controller.getInstance().readImages(img, this.saltAndPepperNoise, this.devStd);
                        append("Images read (" + (saltAndPepperNoise ? "SaltNPepperNoise" : "") + " " + (devStd > 0 ? "GaussianNoise: " + devStd : "") + ")");
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


    /**
     * Constructor to build up a new GUI for the HDR Solver.
     *
     * @param name Display Name
     */
    public GUIFrame(String name) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // add Ui Components
        tabs.setPreferredSize(new Dimension(700, 700));
        add(tabs, BorderLayout.CENTER);
        buildCtrlPanel();
        addLog();
        addProgressBar();

        addMenuBar();

        loadDefaultPictures();
        SwingUtilities.invokeLater(this);

    }

    private void addMenuBar() {
        JMenuBar menuBar;
        JMenu menu;
        menuBar = new JMenuBar();
        menu = new JMenu("Datei");
        menuBar.add(menu);
        menu.add(new JMenuItem("Schließen"));
        menu.add(new JMenuItem("Information"));
        this.setJMenuBar(menuBar);
    }

    /**
     * Run method to execute the ui in seperate thread
     */
    @Override
    public void run() {
        pack();
        setVisible(true);
    }

    /**
     * sets the progress on the UI.
     *
     * @param progress Progress (0..100)
     */
    public void setProgress(int progress) {
        progressBar.setEnabled(true);
        progressBar.setValue(progress);
    }


    /**
     * Add a Plot with a headline to the UI.
     *
     * @param p    Plot to be added
     * @param name Headline in the tap
     */
    public void addPlot(Plot p, String name) {
        plots.add(p);
        tabs.addTab(name, p);
        p.setOutputFileName(this.outputPrefix + "_" + name + "_" + init_time);
    }


    // LOG METHODS

    /**
     * Appends a log message
     *
     * @param s
     */
    @Override
    public void append(Object s) {
        write(s + "\n");
        if (s instanceof Model.Image) {
            final Image image = (Image) s;
            this.addPlot(new ImagePlot(image), "Image");
        }
    }

    /**
     * write some object to the logger
     *
     * @param s
     */
    @Override
    public void write(final Object s) {
        log.append(s.toString());
    }


    // ------------- PRIVATE METHODS


    /**
     * updates all the values from the input fields and generates a new prefix for the generated images.
     *
     * @return true if the reading from the input fields succeed.
     */
    private boolean updatePrefix() {
        try {
            lambda = lambdaInput.getDoubleValue().doubleValue();
            mu = inputMu.getDoubleValue().doubleValue();
            iteration = inputIterations.getLongValue().intValue();
            alpha = inputAlpha.getDoubleValue().doubleValue();
            devStd = inputDevStd.getDoubleValue().doubleValue();
        } catch (Exception e) {
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
        s += "_gauss=" + (devStd > 0 ? devStd + "" : "-");
        setTitle("HDR: " + s);
        if (prefix != null)
            prefix.setText(s);
        outputPrefix = s;
        return true;
    }


    private void showFileProperties(File[] files) {
        tableModel.setFiles(files);
        updatePrefix();
    }

    private void addProgressBar() {
        progressBar = new JProgressBar(0, 100);
        progressBar.setEnabled(false);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);
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
        }
    }

    private void buildCtrlPanel() {
        ctrlPnl = new JPanel(new BorderLayout());
        JPanel btns = new JPanel(new GridLayout(11, 1));
        JButton btnChoose = new JButton("Bilder wählen");
        btnChoose.addActionListener(this);
        btnChoose.setActionCommand(String.valueOf(ACTIONS.CHOOSE_FILE));
        btns.add(btnChoose);

        tableModel = new ImageTableModel();
        table = new JTable(tableModel);
        LeftDotTableRenderer leftDot = new LeftDotTableRenderer();
        table.getColumnModel().getColumn(0).setCellRenderer(leftDot);
        ctrlPnl.add(new JScrollPane(table), BorderLayout.SOUTH);


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

        ctrlPnl.add(btns, BorderLayout.CENTER);


        tabs.addTab("Control", ctrlPnl);

    }

    private void addLog() {
        log.setBackground(Color.black);
        log.setForeground(Color.gray);
        log.setLineWrap(true);
        log.setFont(new Font("Monospaced", Font.PLAIN, 15));
        tabs.addTab("Logs", new JScrollPane(log));
    }

    private void buildWightPanel(JPanel btns) {
        btns.add(new JLabel("Gewichtungsfunktion"));
        String[] list = {WeightMode.NONE.toString(), WeightMode.DEFAULT.toString(), WeightMode.PARABEL.toString()};
        JComboBox ws = new JComboBox(list);
        ws.setSelectedIndex(1);
        ws.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String cmd = (String) cb.getSelectedItem();
                weightning = WeightMode.valueOf(cmd);
                updatePrefix();
            }
        });
        btns.add(ws);
    }

    private void buildOutputPrefix(JPanel btns) {
        btns.add(new JLabel("Prefix für gespeicherte Bilder"));
        prefix = new JTextField(this.outputPrefix);
        btns.add(prefix);
    }

    private void buildRobustnessSelector(JPanel btns) {

        btns.add(new JLabel("Subquad. Bestrafungsterme (Datenterm g)"));
        JCheckBox check = new JCheckBox("aktivieren");
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

        btns.add(new JLabel("Subquad. Bestrafungsterme (Glattheitst. E)"));
        JCheckBox check3 = new JCheckBox("aktivieren");
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

        btns.add(new JLabel("Salt&Pepper (4%)"));
        JCheckBox saltNPepper = new JCheckBox("aktivieren");
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

        buildGaussInput(btns);
    }

    private void buildGaussInput(JPanel btns) {
        String tool = "Bestimmt wie groß die Standardabweichung des Gauss-Filters sein soll.";
        final JLabel l = new JLabel("Standardabweichung Gauss");
        l.setToolTipText(tool);
        btns.add(l);
        inputDevStd = new NumericTextField(5, format);
        inputDevStd.setValue(devStd);
        btns.add(inputDevStd);
    }

    private void buildIterationSlider(JPanel btns) {
        String tool = "Bestimmt wie viele Hauptiterationen durchgeführt werden soll.";
        final JLabel l = new JLabel("Iterationen");
        l.setToolTipText(tool);
        btns.add(l);
        inputIterations = new NumericTextField(5, format);
        inputIterations.setValue(iteration);
        btns.add(inputIterations);
    }

    private void buildLambdaSlider(JPanel btns) {
        String tool = "Bestimmt wie sehr der Glattheitsterm gewichtet werden soll.";
        final JLabel l = new JLabel("Smoothness");
        l.setToolTipText(tool);
        btns.add(l);

        lambdaInput = new NumericTextField(5, format);
        lambdaInput.setValue(lambda);
        btns.add(lambdaInput);
    }

    private void buildMuSlider(JPanel btns) {
        String tool = "Bestimmt wie sehr der Monotonie-Term gewichtet werden soll.";
        final JLabel l = new JLabel("Monotonie");
        l.setToolTipText(tool);
        btns.add(l);
        inputMu = new NumericTextField(5, format);
        inputMu.setValue(mu);
        btns.add(inputMu);
    }

    private void buildAlphaSlider(JPanel btns) {
        String tool = "Bestimmt wie sehr der Räumliche-Glattheitsterm gewichtet werden soll.";
        final JLabel l = new JLabel("Räumlicher Glattheitsterm");
        l.setToolTipText(tool);
        btns.add(l);
        inputAlpha = new NumericTextField(5, format);
        inputAlpha.setValue(alpha);
        btns.add(inputAlpha);
    }


}
