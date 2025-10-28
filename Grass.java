import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;

/**
 * Grass class represents a plant that starts as a seed and can grow into full grass.
 * Grass can serve as food for prey mobs and has a defined growth probability.
 * 
 * Implements the ImageProvider interface to supply visual representation for the simulation.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public class Grass extends Plant implements ImageProvider {

    // The probability that a seed will grow into grass at each simulation step.
    private static final double GROWTH_PROBABILITY = 0.007; // 0.7% chance per step

    // Tracks whether the grass is still a seed or fully grown.
    private boolean isSeed = false;

    // Random number generator for growth probability calculations.
    private static final Random rand = new Random();

    // The food value provided by fully grown grass to mobs.
    private int FOOD_VALUE = 30;

    /**
     * Returns the food value of the grass when consumed by mobs.
     * 
     * @return The food value of the grass.
     */
    public int getFoodValue() {
        return FOOD_VALUE;
    }

    // Static images for seed and fully grown grass.
    private static BufferedImage seedImage;
    private static BufferedImage grassImage;

    // Static block to load images once for efficiency.
    static {
        try {
            // Load the image representing a seed.
            seedImage = ImageIO.read(Grass.class.getResourceAsStream("Images/seed.jpg"));
            // Load the image representing fully grown grass.
            grassImage = ImageIO.read(Grass.class.getResourceAsStream("Images/grass.jpg"));
        } catch (IOException e) {
            // Print an error message if image loading fails.
            System.err.println("Error loading plant images: " + e.getMessage());
        }
    }

    /**
     * Constructs a Grass object at the specified location.
     * 
     * @param location The location where the grass is placed.
     */
    public Grass(Location location) {
        super(location);
    }

    /**
     * Handles the growth process of the grass.
     * Grass has a probability to grow from a seed to full grass at each simulation step.
     * 
     * @param field The current state of the field.
     * @param nextField The field representing the next simulation state.
     */
    @Override
    public void grow(Field field, Field nextField) {
        // If the grass is still a seed, attempt to grow based on the growth probability.
        if (isSeed && rand.nextDouble() < GROWTH_PROBABILITY) {
            isSeed = false; // Mark the grass as fully grown.
        }
    }

    /**
     * Returns the image to display for this grass.
     * Displays a different image if the grass is still a seed.
     * 
     * @return The BufferedImage representing the current state of the grass.
     */
    public BufferedImage getImage() {
        // Return the appropriate image based on whether the grass is a seed or fully grown.
        return isSeed ? seedImage : grassImage;
    }

    /**
     * Checks if the grass is still in the seed state.
     * 
     * @return true if the grass is a seed, false otherwise.
     */
    public boolean isSeed() {
        return isSeed;
    }

    /**
     * Sets whether the grass is in the seed state or fully grown.
     * 
     * @param isSeed true to mark the grass as a seed, false to mark as fully grown.
     */
    public void setSeed(boolean isSeed) {
        this.isSeed = isSeed;
    }

    /**
     * Returns the type of plant this grass represents.
     * 
     * @return The plant type as PlantType.GRASS.
     */
    @Override
    public PlantType getPlantType() {
        return PlantType.GRASS;
    }

}
