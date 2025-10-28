import java.util.List;
import java.util.Random;

/**
 * Abstract PredatorMob class representing all predator entities in the simulation.
 * Predator mobs can hunt, breed, and create offspring within the simulation environment.
 * This class provides the core breeding and hunting behavior for all predators.
 * 
 * Specific predator types must implement the abstract methods to define their own
 * breeding age, maximum age, litter size, and hunting behavior.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public abstract class PredatorMob extends Mob {

    // Random number generator for breeding and hunting behavior.
    private static final Random rand = new Random();

    /**
     * Constructs a new PredatorMob at the specified location.
     * 
     * @param location The initial location of the predator within the field.
     */
    public PredatorMob(Location location) {
        super(location);
    }

    /**
     * Returns the minimum age at which the predator is capable of breeding.
     * Specific predator types must provide their own breeding age.
     * 
     * @return The breeding age of the predator.
     */
    protected abstract int getBreedingAge();

    /**
     * Returns the maximum age that a predator can live before dying of old age.
     * Specific predator types must provide their own maximum age.
     * 
     * @return The maximum age of the predator.
     */
    protected abstract int getMaxAge();

    /**
     * Returns the maximum number of offspring a predator can produce in one breeding cycle.
     * Specific predator types must define their own litter size.
     * 
     * @return The maximum litter size for the predator.
     */
    protected abstract int getMaxLitterSize();

    /**
     * Defines the hunting behavior for the predator.
     * Specific predator types must implement this method to locate prey.
     * 
     * @param field The field in which the predator hunts for prey.
     * @return The location of prey, or null if no prey is found.
     */
    protected abstract Location huntPrey(Field field);

    /**
     * Checks whether the predator is old enough to breed.
     * 
     * @return true if the predator can breed, false otherwise.
     */
    protected boolean canBreed() {
        return getAge() >= getBreedingAge();
    }

    /**
     * Handles the breeding process for the predator.
     * If a mate is found in adjacent locations, creates new offspring at available locations.
     * The number of births is randomized up to the maximum litter size.
     * 
     * @param nextFieldState The field state for the next simulation step.
     * @param freeLocations The list of free locations where offspring can be placed.
     */
    public void giveBirth(Field nextFieldState, List<Location> freeLocations) {
        if (canBreed()) {
            // Check for potential mates in adjacent locations.
            List<Location> adjacent = nextFieldState.getAdjacentLocations(getLocation());
            boolean foundMate = false;

            for (Location loc : adjacent) {
                Object mob = nextFieldState.getObjectAt(loc);
                if (mob != null && mob.getClass().equals(this.getClass()) && ((Mob) mob).isAlive()) {
                    foundMate = true;
                    break;
                }
            }

            // If a mate is found, create offspring in available free locations.
            if (foundMate) {
                int births = rand.nextInt(getMaxLitterSize()) + 1;
                for (int b = 0; b < births && !freeLocations.isEmpty(); b++) {
                    Location loc = freeLocations.remove(0);
                    PredatorMob young = createOffspring(loc);
                    nextFieldState.placeObject(young, loc);
                }
            }
        }
    }

    /**
     * Creates a new instance of the predator as an offspring.
     * Specific predator types must implement this method to return a new predator instance.
     * 
     * @param location The location where the new predator will be placed.
     * @return A new PredatorMob instance representing the offspring.
     */
    protected abstract PredatorMob createOffspring(Location location);
}
