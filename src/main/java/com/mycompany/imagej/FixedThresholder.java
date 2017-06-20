package com.mycompany.imagej;

import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.IOException;


@Plugin(type = Command.class, menuPath = "Plugins > Fixed Threshold")
public class FixedThresholder<T extends RealType<T>> implements Command {

    @Parameter(type = ItemIO.INPUT, label="input image")
    ImgPlus<T> img;

    @Parameter
    Float threshold;

    @Parameter(type = ItemIO.OUTPUT, label = "thresholded image")
    Img<BitType> bw;


    @Override
    public void run() {
        // Get the image dimensions
        long[] dim = new long[img.numDimensions()];
        for (int d= 0; d < img.numDimensions(); d ++) {
            dim[d] = img.dimension(d);
        }

        // Create an image to hold the thresholded pixels
        bw = ArrayImgs.bits(dim);


        threshold1(img, bw);
//        threshold2(img, bw);
    }

    private void threshold1(Img<T> img, Img<BitType> bw) {
        Cursor<T> cur = img.cursor();
        RandomAccess<BitType> ra = bw.randomAccess();

        // Iterate over the pixels and compare them to the threshold
        while (cur.hasNext()) {
            cur.next();
            T gcv = cur.get();
            ra.setPosition(cur);

            boolean bwv = gcv.getRealFloat() > 50;
            ra.get().set(new BitType(bwv));
        }
    }

    // According to the suggestions of Stefan
    private void threshold2(Img<T> img, Img<BitType> bw) {
        // make sure the cursors iterate over the pixels in the same order
        IterableInterval<T> iter1 = Views.flatIterable(img);
        IterableInterval<BitType> iter2 = Views.flatIterable(bw);

        // get the cursors
        Cursor<T> cur1 = iter1.cursor();
        Cursor<BitType> cur2 = iter2.cursor();

        // Put the threshold value according to the input pixel type
        T thresholdT = img.firstElement().copy();
        thresholdT.setReal(threshold);

        // Iterate over the pixels and compare them to the threshold
        while (cur1.hasNext()) {
            cur1.fwd();
            cur2.fwd();

            boolean bwv = cur1.get().compareTo(thresholdT) > -1;
            cur2.get().set(new BitType(bwv));
        }
    }

    public static void main(String[] args) throws IOException {
        final ImageJ ij = net.imagej.Main.launch(args);
        Object img = ij.io().open(FixedThresholder.class.getResource("/blobs.tif").getFile());
        ij.ui().show(img);
        ij.command().run(FixedThresholder.class, true);
    }
}
