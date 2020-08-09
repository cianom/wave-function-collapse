package cianom.wfc.core.out;

import java.awt.image.BufferedImage;

public interface Target<T> {


    int getWidth();

    int getHeight();

    void observe(int x, int y, T value);

    BufferedImage toImage();

}
