import java.awt.image.BufferedImage;

/**
 * ImageProvider is an interface for classes that provide an image representation.
 * Implementing classes are expected to return a BufferedImage when requested.
 * 
 * This interface is useful for displaying visual representations of objects 
 * in graphical simulations or games.
 * 
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public interface ImageProvider {

    /**
     * Returns a BufferedImage representing the visual appearance of the object.
     * This method should be implemented by any class that needs to provide an image.
     * 
     * @return The BufferedImage associated with the implementing object.
     */
    BufferedImage getImage();
}
