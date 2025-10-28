import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Abstract Mob class that serves as a common base for all mob entities.
 * Mobs include entities like creeprs, zombies, and other mobs that act within the simulation.
 * Provides core functionality for movement, aging, hunger management, and image representation.
 * 
 * 
 * Credit: Mojang, inspired by Minecraft mechanics.
 * @author David J. Barnes, Michael Kölling, and Ali Alshiyoukh
 * @version 7.1
 */
public abstract class Mob {

    // Indicates whether the mob is currently alive.
    private boolean alive;
    // The current location of the mob within the field.
    private Location location;
    // Placeholder image used if no specific image is provided by subclasses.
    private static BufferedImage placeholderImage;
    // The field in which the mob exists.
    private Field field;
    // The current food level of the mob, impacting its survival.
    protected int foodLevel;
    // The age of the mob, which increases over time.
    protected int age;
    // Random number generator for various behaviors.
    private Random rand;

    /**
     * Constructs a new Mob at the specified location.
     * Mobs start alive with an initial age and food level defined by subclasses.
     * 
     * @param location The initial location of the mob within the field.
     */
    public Mob(Location location) {
        this.alive = true;
        this.location = location;
        this.field = field;
        this.rand = new Random();
    }

    
    /**
     * Static initializer block to load images for the creeper.
     * Attempts to load both healthy and diseased creeper images from the file system.
     * If the images cannot be loaded, an error message is printed to the console.
     */
    static {
        try {
            placeholderImage = ImageIO.read(new File("Images/placeholderImage.jpg"));
            } catch (IOException e) {                System.err.println("Error loading placeholder image: " + e.getMessage());
             }
        }

    /**
     * Defines the actions that the mob should take in each simulation step.
     * Subclasses must implement this method to define specific behavior.
     * 
     * @param currentField The current state of the field.
     * @param nextFieldState The new state being built.
     */
    abstract protected void act(Field currentField, Field nextFieldState);

    /**
     * Returns the type of mob represented by this class.
     * Must be implemented by all subclasses to provide the correct MobType.
     * 
     * @return The MobType of the mob.
     */
    abstract protected MobType getMobType();

    /**
     * Increments the hunger of the mob.
     * If the mob's food level drops to zero or below, it dies of starvation.
     */
    protected void incrementHunger() {
        foodLevel--;
        if (foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * Increments the age of the mob.
     * If the mob's age exceeds its maximum allowed age, it dies of old age.
     */
    protected void incrementAge() {
        age++;
        if (age >= getMaxAge()) {
            setDead();
        }
    }

    /**
     * Returns the current age of the mob.
     * 
     * @return The age of the mob.
     */
    protected int getAge() {
        return age;
    }

    /**
     * Increments the age of the mob by a specified number of years.
     * This method is particularly useful for rapidly aging diseased mobs.
     * 
     * @param years The number of years to add to the mob's age.
     */
    protected void incrementAge(int years) {
        age += years;
        if (age >= getMaxAge()) {
            setDead();
        }
    }

    /**
     * Returns the maximum age that the mob can live.
     * This value is defined by subclasses.
     * 
     * @return The maximum age of the mob.
     */
    abstract protected int getMaxAge();

    /**
     * Checks whether the mob is still alive.
     * 
     * @return true if the mob is alive, false otherwise.
     */
    protected boolean isAlive() {
        return alive;
    }

    /**
     * Marks the mob as dead.
     * The mob's location is cleared and it no longer participates in the simulation.
     */
    protected void setDead() {
        alive = false;
        location = null;
    }

    /**
     * Returns the current location of the mob within the field.
     * 
     * @return The current location of the mob.
     */
    protected Location getLocation() {
        return location;
    }

    /**
     * Returns the image associated with the mob.
     * Subclasses can override this method to provide specific images.
     * 
     * @return A BufferedImage representing the mob.
     */
    protected BufferedImage getImage() {
        return placeholderImage;
    }

    /**
     * Sets the location of the mob within the field.
     * 
     * @param location The new location for the mob.
     */
    protected void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Determines the best move for the mob among a list of free locations.
     * Prefers to move to the furthest available location to avoid clustering.
     * If no optimal move is found, a random free location is chosen.
     * 
     * @param freeLocations The list of available locations for movement.
     * @return The chosen location to move to, or null if none are available.
     */
    protected Location findBestMove(List<Location> freeLocations) {
        if (freeLocations.isEmpty()) return null;

        Location bestLocation = null;
        int maxDistance = -1;

        // Evaluate each free location to find the one with the maximum distance from the current position.
        for (Location loc : freeLocations) {
            int distance = Math.abs(loc.row() - getLocation().row()) + Math.abs(loc.col() - getLocation().col());
            if (distance > maxDistance) {
                maxDistance = distance;
                bestLocation = loc;
            }
        }

        // If no optimal move is found, pick a random free spot.
        return (bestLocation != null) ? bestLocation : freeLocations.get(rand.nextInt(freeLocations.size()));
    }
}
