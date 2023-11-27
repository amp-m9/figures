package xyz.andrick.figures.utilities;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Helpers {
    public static void keepImageWithinParentBounds(ImageView image, Pane parent) {
        double imageHeight = image.getBoundsInParent().getHeight();
        double imageWidth = image.getBoundsInParent().getWidth();

        double LowerBoundX = Math.max((parent.getWidth() - imageWidth) / 2, 0);
        double UpperBoundX = Math.min(parent.getWidth(), (parent.getWidth() + imageWidth) / 2);
        double LowerBoundY = Math.max((parent.getHeight() - imageHeight) / 2, 0);
        double UpperBoundY = Math.min(parent.getHeight(), (parent.getHeight() + imageHeight) / 2);

        double imageMinY = image.getBoundsInParent().getMinY(); // top
        double imageMaxY = image.getBoundsInParent().getMaxY(); // bottom
        double imageMinX = image.getBoundsInParent().getMinX(); // lhs
        double imageMaxX = image.getBoundsInParent().getMaxX(); // rhs

        if (imageMinY > LowerBoundY)
            image.setY(image.getY() - (imageMinY - LowerBoundY));

        if (imageMaxY < UpperBoundY)
            image.setY(image.getY() + (UpperBoundY - imageMaxY));

        if (imageMinX > LowerBoundX)
            image.setX(image.getX() - (imageMinX - LowerBoundX));

        if (imageMaxX < UpperBoundX)
            image.setX(image.getX() + (UpperBoundX - imageMaxX));
    }

    public static InputStream getInputStreamFromUrl(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        return connection.getInputStream();
    }
}