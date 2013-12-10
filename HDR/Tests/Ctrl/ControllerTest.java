package Ctrl;

import Model.WeightMode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by sebastianzillessen on 08.12.13.
 */
public class ControllerTest {
    @Test
    public void testGetInstance() throws Exception {
        Controller instance = Controller.getInstance();
        assertNotNull(instance);
        assertTrue(Controller.getInstance() instanceof Controller);
        assertEquals(Controller.getInstance(), instance);
    }


    @Test
    public void testSolve() throws Exception {
        Controller.getInstance().solve(1, 10, 0, false, false, WeightMode.DEFAULT, 0.0);

    }

    @Test
    public void testReadImages() throws Exception {

    }

    @Test
    public void testGetDisplay() throws Exception {
        assertNotNull(Controller.getInstance().getDisplay());
    }

    @Test
    public void testUpdateState() throws Exception {

    }

    @Test
    public void testErrorOccured() throws Exception {

    }

    @Test
    public void testExtractExposureTime() throws Exception {

    }

    @Test
    public void testCalculate() throws Exception {
        assertEquals(0.2, Controller.getInstance().calculate("1/5"), 0.0001);
        assertEquals(0.2, Controller.getInstance().calculate("1:5"), 0.0001);
        assertEquals(0.2, Controller.getInstance().calculate("10/50"), 0.0001);
        assertEquals(0.2, Controller.getInstance().calculate("0.2"), 0.0001);
        assertEquals(null, Controller.getInstance().calculate("test"));
    }
}
