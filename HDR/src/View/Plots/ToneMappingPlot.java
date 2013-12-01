package View.Plots;

import Model.HDRResult;
import View.ToneMappers.ToneMapping;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 15.07.13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */
public class ToneMappingPlot extends ImagePlot {
    private final HDRResult r;
    private ToneMapping mapping;
    private JTextField[] inputs;
    private String[] vars;

    public ToneMappingPlot(HDRResult r, ToneMapping mapping) {
        super(mapping.getImage(r.getWidth(), r.getHeight(), r.getE().toArray()));
        this.r = r;
        this.mapping = mapping;
        buildInputCtrl();
    }


    private void buildInputCtrl() {
        vars = mapping.getVars();
        inputs = new JTextField[vars.length];
        if (vars.length == 0)
            return;
        JPanel pnl = new JPanel(new GridLayout(Math.max(vars.length + 1, 10), 2));
        pnl.add(new JLabel("Parameter"));
        JButton doit = new JButton("Parameter übernehmen");
        doit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean error = false;
                for (int i = 0; i < vars.length; i++) {

                    if (!mapping.setVar(vars[i], inputs[i].getText())) {
                        inputs[i].setBorder(BorderFactory.createLineBorder(Color.red));
                        error = true;
                    } else {
                        inputs[i].setBorder(BorderFactory.createLineBorder(Color.gray));
                    }
                }
                if (!error) {
                    ToneMappingPlot.super.setImage(mapping.getImage(r.getWidth(), r.getHeight(), r.getE().toArray()));
                    redraw();
                }
            }
        });
        pnl.add(doit);

        for (int i = 0; i < vars.length; i++) {
            pnl.add(new JLabel(vars[i]));
            JTextField input = new JTextField(mapping.getVar(vars[i]));
            inputs[i] = input;
            pnl.add(input);
        }

        add(pnl, BorderLayout.EAST);

    }


    private void updateInputFields() {
        for (int i = 0; i < vars.length; i++) {
            inputs[i].setText(mapping.getVar(vars[i]));
        }
    }


}
