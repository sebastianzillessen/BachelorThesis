package View.ImageTable;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 14.07.13
 * Time: 13:34
 * To change this template use File | Settings | File Templates.
 */
public class ImageTableModel extends AbstractTableModel {


    private Map<File, Float> fileMap;

    public ImageTableModel() {
        this.fileMap = new HashMap<File, Float>();
    }

    public ImageTableModel(File[] files) {
        this();
        setFiles(files);
    }

    @Override
    public int getRowCount() {
        return fileMap.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                File f = ((File) fileMap.keySet().toArray()[row]);
                return f;
            case 1:
                return fileMap.values().toArray()[row];
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return "File";
            case 1:
                return "Belichtungszeit";
            default:
                return "";
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        return (col == 1);
    }


    private float extractExposureTime(File f) {
        float v = 0;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(f);
            ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
            v = directory.getFloat(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
        } catch (Exception e) {
            v = calculate(f.getName());
        }
        return v;

    }

    public float calculate(String s) {
        String filename = s.split("\\.")[0].replace(":", "/");
        float v = 0;
        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            v = Float.parseFloat(engine.eval(String.valueOf(filename)).toString());
        } catch (NumberFormatException e) {
        } catch (ScriptException e) {
        }
        return v;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        try {
            if (col == 1) {
                fileMap.put((File) getValueAt(row, 0), calculate(value.toString()));
                fireTableCellUpdated(row, col);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFiles(File[] files) {
        this.fileMap.clear();
        for (File f : files) {
            fileMap.put(f, extractExposureTime(f));
        }
        fireTableStructureChanged();
    }
}
