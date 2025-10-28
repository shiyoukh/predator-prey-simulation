import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a pig.
 * Pigs age, move, breed, and die within the simulation.
 * Pigs can also become diseased and spread illness to others.
 * 
 * 
 * 
 * Credit: Mojang, inspired by Minecraft mechanics.
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public class Pig extends PreyMob implements DiseaseHandler, ImageProvider {

    // The age at which a pig can start to breed.
    private static final int BREEDING_AGE = 5;
    // The maximum age a pig can live before dying of old age.
    private static final int MAX_AGE = 100;
    // The maximum number of piglets a pig can produce at once.
    private static final int MAX_LITTER_SIZE = 5;
    // Random number generator for age, breeding, and behavior.
    private static final Random rand = Randomizer.getRandom();
    // The maximum hunger level before a pig dies of starvation.
    private static final int HUNGER_LIMIT = 80;
    // The food value provided to predators when this pig is eaten.
    private static final int FOOD_VALUE = 60;

    // Images for a healthy pig and a diseased pig.
    private static BufferedImage pigImage;
    private static BufferedImage diseasedPigImage;

    // The base rate at which pigs can become diseased.
    private static final double DISEASE_RATE = 0.0005;
    // The rate at which disease spreads from this pig to others.
    private static final double DISEASE_SPREAD_RATE = 0.007;

    // Indicates whether the pig is currently diseased.
    private boolean isDiseased = false;
    // Randomly determines the gender of the pig (true = male, false = female).
    private boolean isMale = rand.nextBoolean();

    /**
     * Constructs a new Pig instance.
     * The pig may start with a random age and a randomized initial food level.
     * 
     * @param randomAge If true, the pig will start with a random age.
     * @param location The location within the field where the pig is placed.
     */
    public Pig(boolean randomAge, Location location) {
        super(location);
        if (randomAge) {
            age = rand.nextInt(MAX_AGE); // Start with a random age
        } else {
            age = 0;
        }
        // Initialize the pig's food level to a random value between half and full hunger.
        foodLevel = rand.nextInt(HUNGER_LIMIT / 2, HUNGER_LIMIT);
    }

    // Static block to load images once for efficiency.
    static {
        try {
            pigImage = ImageIO.read(Pig.class.getResourceAsStream("Images/pig.jpg"));
            diseasedPigImage = ImageIO.read(Pig.class.getResourceAsStream("Images/diseasedPig.jpg"));
        } catch (IOException e) {
            System.err.println("Error loading pig images: " + e.getMessage());
        }
    }

    /**
     * Returns whether the pig is male.
     * 
     * @return true if the pig is male, false if female.
     */
    public boolean isMale() {
        return isMale;
    }

    /**
     * Returns the appropriate image for the pig based on its health status.
     * 
     * @return The BufferedImage representing the pig.
     */
    @Override
    public BufferedImage getImage() {
        return isDiseased ? diseasedPigImage : pigImage;
    }

    /**
     * Returns the type of mob this pig represents.
     * 
     * @return The MobType.PIG enum value.
     */
    @Override
    public MobType getMobType() {
        return MobType.PIG;
    }

    /**
     * Returns the rate at which this pig can become diseased.
     * 
     * @return The disease rate.
     */
    @Override
    public double getDiseaseRate() {
        return DISEASE_RATE;
    }

    /**
     * Returns the rate at which this pig can spread disease to others.
     * 
     * @return The disease spread rate.
     */
    @Override
    public double getDiseaseSpreadRate() {
        return DISEASE_SPREAD_RATE;
    }

    /**
     * Sets the disease status of the pig.
     * If the pig becomes diseased, its age is incremented to accelerate aging.
     * 
     * @param diseased true if the pig is diseased, false otherwise.
     */
    @Override
    public void setDiseased(boolean diseased) {
        isDiseased = diseased;
        if (isDiseased) {
            incrementAge(age);
        }
    }

    /**
     * Checks if the pig is currently diseased.
     * 
     * @return true if the pig is diseased, false otherwise.
     */
    @Override
    public boolean isDiseased() {
        return isDiseased;
    }


      /**
     * Returns the food value of the pig when consumed by predators.
     * This value contributes to the predator's hunger level upon eating the pig.
     * 
     * @return The food value of the pig.
     */
    @Override
    protected int getFoodValue() {
        return FOOD_VALUE;
    }

    /**
     * Returns the minimum age at which the pig is capable of breeding.
     * Pigs must reach this age before they can produce offspring.
     * 
     * @return The breeding age of the pig.
     */
    @Override
    protected int getBreedingAge() {
        return BREEDING_AGE;
    }

    /**
     * Returns the maximum age that a pig can live before it dies of old age.
     * Pigs exceeding this age are automatically marked as dead.
     * 
     * @return The maximum age of the pig.
     */
    @Override
    protected int getMaxAge() {
        return MAX_AGE;
    }

    /**
     * Returns the maximum number of piglets that a pig can produce in one breeding cycle.
     * 
     * @return The maximum litter size for the pig.
     */
    @Override
    protected int getMaxLitterSize() {
        return MAX_LITTER_SIZE;
    }

    /**
     * Creates a new Pig instance as an offspring.
     * The offspring is always created as a newborn with age set to 0.
     * 
     * @param location The location where the new pig will be placed.
     * @return A new Pig instance at the specified location.
     */
    @Override
    protected PreyMob createOffspring(Location location) {
        return new Pig(false, location);
    }

    /**
     * Returns the hunger limit of the pig.
     * This is the maximum hunger level before the pig dies of starvation.
     * 
     * @return The hunger limit of the pig.
     */
    @Override
    public int getHungerLimit() {
        return HUNGER_LIMIT;
    }

}
