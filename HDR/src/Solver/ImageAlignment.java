package Solver;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 24.10.13
 * Time: 21:21
 * Basic is coming from: http://pages.cs.wisc.edu/~lizhang/courses/cs766-2008f/projects/hdr/jgtpap2.pdf
 */
public class ImageAlignment {
    /**
     * returns the aligned version of a set of pictures.
     * The first picture is used as source-
     *
     * @param images the pictures to align to this
     * @return list of aligned pictures (of same size)
     */
    public static ArrayList<Image> align(ArrayList<Image> images) {
        try {
            for (int i = 0; i < images.size(); i++) {
                int[] shift = GetExpShift("CMP 0_" + i + "_", images.get(0), images.get(i), 6, new int[]{0, 0});
                System.out.println("Shift of 0 and " + i + " should be: " + shift[0] + "|" + shift[1]);
                images.get(i).shift(shift[0], shift[1]).save("output/shifted_" + i);
                images.set(i, images.get(i).shift(shift[0], shift[1]));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    /**
     * This function takes two exposure images, and determines how much to move the second exposure (img2)
     * in x and y to align it with the first exposure (img1).
     * The maximum number of bits in the final offsets is determined by the shift_bits parameter.
     *
     * @param shift_bits
     * @param shift_ret
     */

    private static int[] GetExpShift(String prefix, Image img1, Image img2, int shift_bits, int[] shift_ret) {
        int min_err;
        int cur_shift[] = {0, 0};
        Bitmap tb1, tb2;
        Bitmap eb1, eb2;
        if (shift_bits > 0)

        {
            Image sml_img1, sml_img2;
            sml_img1 = img1.downsample();
            sml_img2 = img2.downsample();
            cur_shift = GetExpShift(prefix, sml_img1, sml_img2, shift_bits - 1, cur_shift);
            cur_shift[0] *= 2;
            cur_shift[1] *= 2;
        } else
            cur_shift[0] = cur_shift[1] = 0;

        tb1 = img1.getMTB();
        eb1 = img1.getExclusionBitmap(5);
        tb2 = img2.getMTB();
        eb2 = img2.getExclusionBitmap(5);
        min_err = img1.getWidth() * img1.getHeight();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int xs = cur_shift[0] + i;
                int ys = cur_shift[1] + j;
                Bitmap shifted_tb2 = tb2.shift(xs, ys);
                Bitmap shifted_eb2 = eb2.shift(xs, ys);
                Bitmap diff_b = tb1.xor(shifted_tb2).and(eb1).and(shifted_eb2);
                int err = diff_b.countBits();
                if (err < min_err) {
                    shift_ret[0] = xs;
                    shift_ret[1] = ys;
                    min_err = err;
                }
            }
        }
        return shift_ret;
    }
}
