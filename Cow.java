import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a cow.
 * Cows age, move, breed, and die.
 * 
 * Credit: Mojang, inspired by Minecraft mechanics.
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public class Cow extends PreyMob implements DiseaseHandler, ImageProvider {

    // The age at which a cow can start to breed.
    private static final int BREEDING_AGE = 5;
    // The maximum age a cow can reach before it dies.
    private static final int MAX_AGE = 100;
    // The maximum number of offspring a cow can have at once.
    private static final int MAX_LITTER_SIZE = 6;
    // Random number generator for breeding and age initialization.
    private static final Random rand = Randomizer.getRandom();
    // The hunger limit after which a cow dies.
    private static final int HUNGER_LIMIT = 80;
    // The food value of a cow when eaten by a predator.
    private static final int FOOD_VALUE = 60;
    // The cow's gender, randomly assigned as male or female.
    private boolean isMale = rand.nextBoolean();
    // The disease status of the cow.
    private boolean isDiseased = false;
    // The probability of a cow becoming diseased.
    private static final double DISEASE_RATE = 0.0005;
    // The probability of a cow spreading the disease to others.
    private static final double DISEASE_SPREAD_RATE = 0.007;
    // Images for healthy and diseased cows.
    private static BufferedImage cowImage;
    private static BufferedImage diseasedCowImage;

    /**
     * Constructor for a cow.
     * Initializes age and food level, and sets the location.
     * 
     * @param randomAge If true, the cow is initialized with a random age.
     * @param location The cow's location in the field.
     */
    public Cow(boolean randomAge, Location location) {
        super(location);
        if (randomAge) {
            age = rand.nextInt(MAX_AGE);
        } else {
            age = 0;
        }
        foodLevel = rand.nextInt(HUNGER_LIMIT / 2, HUNGER_LIMIT);
    }

    // Static block to load cow images
    static {
        try {
            cowImage = ImageIO.read(Cow.class.getResourceAsStream("Images/cow.jpg"));
            diseasedCowImage = ImageIO.read(Cow.class.getResourceAsStream("Images/diseasedCow.jpg"));
        } catch (IOException e) {
            System.err.println("Error loading cow images: " + e.getMessage());
        }
    }

    /**
     * Returns whether the cow is male.
     * 
     * @return true if the cow is male, false otherwise.
     */
    public boolean isMale() {
        return isMale;
    }

    /**
     * Returns the food value of a cow when eaten by a predator.
     * 
     * @return The food value of the cow.
     */
    @Override
    protected int getFoodValue() {
        return FOOD_VALUE;
    }

    /**
     * Returns the type of mob this cow represents.
     * 
     * @return The mob type (COW).
     */
    @Override
    public MobType getMobType() {
        return MobType.COW;
    }

    /**
     * Returns the image of the cow, showing a diseased image if applicable.
     * 
     * @return The BufferedImage representing the cow.
     */
    @Override
    public BufferedImage getImage() {
        return isDiseased ? diseasedCowImage : cowImage;
    }

    /**
     * Returns the base rate of disease for a cow.
     * 
     * @return The disease rate.
     */
    @Override
    public double getDiseaseRate() {
        return DISEASE_RATE;
    }

    /**
     * Returns the rate at which the disease can spread to other cows.
     * 
     * @return The disease spread rate.
     */
    @Override
    public double getDiseaseSpreadRate() {
        return DISEASE_SPREAD_RATE;
    }

    /**
     * Sets the disease status of the cow.
     * If the cow becomes diseased, its age is increased.
     * 
     * @param diseased true if the cow is diseased, false otherwise.
     */
    @Override
    public void setDiseased(boolean diseased) {
        isDiseased = diseased;
        if (isDiseased) {
            incrementAge(age); // Accelerate aging if diseased
        }
    }

    /**
     * Returns whether the cow is currently diseased.
     * 
     * @return true if the cow is diseased, false otherwise.
     */
    @Override
    public boolean isDiseased() {
        return isDiseased;
    }


    /**
     * Returns the minimum age at which a cow can breed.
     * 
     * @return The breeding age of the cow.
     */
    @Override
    protected int getBreedingAge() {
        return BREEDING_AGE;
    }   

    /**
     * Returns the maximum age a cow can reach.
     * 
     * @return The maximum age of the cow.
     */
    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    /**
     * Returns the maximum number of offspring a cow can have.
     * 
     * @return The maximum litter size.
     */
    @Override
    protected int getMaxLitterSize() {
        return MAX_LITTER_SIZE;
    }

    /**
     * Creates a new cow at the specified location as an offspring.
     * 
     * @param location The location for the new cow.
     * @return A new Cow instance.
     */
    @Override
    protected PreyMob createOffspring(Location location) {
        return new Cow(false, location);
    }

    /**
     * Returns the hunger limit of the cow.
     * 
     * @return The hunger limit of the cow.
     */
    @Override
    public int getHungerLimit() {
        return HUNGER_LIMIT;
    }
}
