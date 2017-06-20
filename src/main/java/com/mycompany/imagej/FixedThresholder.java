package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.table.DefaultFloatTable;
import net.imagej.table.Table;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;


@Plugin(type = Command.class, menuPath = "Plugins > Fixed Threshold")
public class FixedThresholdeer<T extends RealType<T>> implements Command {

    @Parameter(type = ItemIO.INPUT, persistKey = "asdf")
    Img<T> img;

    @Parameter(type = ItemIO.OUTPUT)
    Table res;


    @Override
    public void run() {
        // Get the number of pixels
        Long numPix = 1l;
        for (int d= 0; d < img.numDimensions(); d ++) {
            numPix *= img.dimension(d);
        }

        // Sum up all the pixels
        Cursor<T> cur = img.cursor();
        T sum = img.firstElement().copy();
        cur.next();
        while (cur.hasNext()) {
            cur.next();
            sum.add(cur.get());
        }

        // Compute the mean pixel value and display it in a table
        Float mean = sum.getRealFloat() / numPix.floatValue();
        res = new DefaultFloatTable(1,1);
        res.setColumnHeader(0, "Image Mean Pixel Value");
        res.set(0, 0, mean);
    }                                                            


    public static void main(String[] args) {
        final ImageJ ij = net.imagej.Main.launch(args);
//        ij.launch(args);
        Img image = ij.op().create().img(new Long[]{100l, 80l});
        ij.op().image().equation(image, "p[0]+p[1]");

        //problem is that 'image' is a raw imglib2 type, not an ij2 dataset. so it gets auto-wrapped, but with a null name.
        // we should also fix things so that when autowrapping happens, name is not null? not sure...
        ImgPlus imgPlus = new ImgPlus((Img) image);
        imgPlus.setName("my awesome image");
        Dataset dataset = new DefaultDataset(ij.context(), imgPlus);


        final ImagePlus imp = IJ.openImage( SandboxIJ1.class.getResource( "/clown.png" ).getFile() );

        ij.ui().show(dataset);
        ij.command().run(FixedThresholdeer.class, true);
    }
}
