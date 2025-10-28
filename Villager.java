import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a villager.
 * Villagers age, move, breed, and die within the simulation.
 * They can also become diseased and spread illness to others.
 * 
 * 
 * Credit: Mojang, Inspired by Minecraft mechanics.
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public class Villager extends PreyMob implements DiseaseHandler, ImageProvider {

    // The age at which a villager can start to breed.
    private static final int BREEDING_AGE = 5;
    // The maximum age a villager can reach.
    private static final int MAX_AGE = 100;
    // The maximum number of offspring a villager can produce at one time.
    private static final int MAX_LITTER_SIZE = 5;
    // Random number generator for age, breeding, and behavior.
    private static final Random rand = Randomizer.getRandom();
    // The maximum hunger level before a villager dies of starvation.
    private static final int HUNGER_LIMIT = 80;
    // The food value provided when the villager is consumed by a predator.
    private static final int FOOD_VALUE = 60;
    // The base rate at which villagers can become diseased.
    private static final double DISEASE_RATE = 0.0005;
    // The rate at which disease spreads from a villager to others.
    private static final double DISEASE_SPREAD_RATE = 0.007;

    // Indicates whether this villager is currently diseased.
    private boolean isDiseased = false;
    // Images representing a healthy villager and a diseased villager.
    private static BufferedImage villagerImage;
    private static BufferedImage diseasedVillagerImage;
    // Randomly determines the gender of the villager (true = male, false = female).
    private boolean isMale = rand.nextBoolean();

    /**
     * Constructs a new Villager instance.
     * If randomAge is true, the villager starts with a random age up to MAX_AGE;
     * otherwise, the villager is created as a newborn (age 0).
     * The initial food level is set randomly between half and the full hunger limit.
     * 
     * @param randomAge If true, assigns a random age to the villager.
     * @param location The initial location of the villager within the field.
     */
    public Villager(boolean randomAge, Location location) {
        super(location);
        // Initialize age based on randomAge flag.
        if (randomAge) {
            age = rand.nextInt(MAX_AGE); // Start with a random age.
        } else {
            age = 0;
        }
        // Set the initial food level to a random value between half and full hunger limit.
        foodLevel = rand.nextInt(HUNGER_LIMIT / 2, HUNGER_LIMIT);
    }

    // Static block to load villager images only once for efficiency.
    static {
        try {
            villagerImage = ImageIO.read(Villager.class.getResourceAsStream("Images/villager.jpg"));
            diseasedVillagerImage = ImageIO.read(Villager.class.getResourceAsStream("Images/diseasedVillager.jpg"));
        } catch (IOException e) {
            System.err.println("Error loading villager images: " + e.getMessage());
        }
    }

    /**
     * Returns the type of mob this villager represents.
     * 
     * @return MobType.VILLAGER indicating the mob type.
     */
    @Override
    public MobType getMobType() {
        return MobType.VILLAGER;
    }

    /**
     * Returns the image to display for the villager.
     * If the villager is diseased, returns the diseased image; otherwise, returns the healthy image.
     * 
     * @return A BufferedImage representing the current state of the villager.
     */
    @Override
    public BufferedImage getImage() {
        return isDiseased ? diseasedVillagerImage : villagerImage;
    }

    /**
     * Checks whether the villager is male.
     * 
     * @return true if the villager is male, false otherwise.
     */
    public boolean isMale() {
        return isMale;
    }

    /**
     * Returns the food value of the villager when consumed by a predator.
     * 
     * @return The food value.
     */
    @Override
    protected int getFoodValue() {
        return FOOD_VALUE;
    }


    /**
     * Returns the minimum age at which a villager can breed.
     * 
     * @return The breeding age.
     */
    @Override
    protected int getBreedingAge() {
        return BREEDING_AGE;
    }

    /**
     * Returns the maximum age a villager can live.
     * 
     * @return The maximum age.
     */
    public int getMaxAge() {
        return MAX_AGE;
    }

    /**
     * Returns the maximum number of offspring a villager can produce in one breeding cycle.
     * 
     * @return The maximum litter size.
     */
    @Override
    protected int getMaxLitterSize() {
        return MAX_LITTER_SIZE;
    }

    /**
     * Creates a new Villager instance as an offspring.
     * The offspring is always created as a newborn (age 0).
     * 
     * @param location The location where the new villager will be placed.
     * @return A new Villager instance.
     */
    @Override
    protected PreyMob createOffspring(Location location) {
        return new Villager(false, location);
    }

    /**
     * Returns the maximum hunger level for the villager.
     * If the food level drops to zero or below, the villager dies of starvation.
     * 
     * @return The hunger limit.
     */
    @Override
    public int getHungerLimit() {
        return HUNGER_LIMIT;
    }

    /**
     * Returns the base disease rate for the villager.
     * This rate is used to determine the probability of the villager becoming diseased.
     * 
     * @return The disease rate.
     */
    @Override
    public double getDiseaseRate() {
        return DISEASE_RATE;
    }

    /**
     * Returns the rate at which the villager can spread disease to other entities.
     * 
     * @return The disease spread rate.
     */
    @Override
    public double getDiseaseSpreadRate() {
        return DISEASE_SPREAD_RATE;
    }

    /**
     * Sets the disease status of the villager.
     * If the villager becomes diseased, its age is accelerated to simulate faster deterioration.
     * 
     * @param diseased true to mark the villager as diseased, false otherwise.
     */
    @Override
    public void setDiseased(boolean diseased) {
        isDiseased = diseased;
        if (isDiseased) {
            incrementAge(age);
        }
    }

    /**
     * Checks whether the villager is currently diseased.
     * 
     * @return true if the villager is diseased, false otherwise.
     */
    @Override
    public boolean isDiseased() {
        return isDiseased;
    }
}
