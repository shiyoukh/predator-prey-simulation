import java.util.List;
import java.util.Random;

/**
 * Abstract PreyMob class representing all prey entities in the simulation.
 * Prey mobs can breed, find food, and create offspring within the simulation environment.
 * This class provides the core behavior for all prey, including breeding and feeding.
 * 
 * Specific prey types must implement the abstract methods to define their own
 * breeding age, maximum age, litter size, food value, and gender.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public abstract class PreyMob extends Mob implements DiseaseHandler {

    // Random number generator for breeding and feeding behavior.
    private static final Random rand = new Random();

    /**
     * Constructs a new PreyMob at the specified location.
     * 
     * @param location The initial location of the prey within the field.
     */
    public PreyMob(Location location) {
        super(location);
    }

    /**
     * Returns the minimum age at which the prey is capable of breeding.
     * Specific prey types must provide their own breeding age.
     * 
     * @return The breeding age of the prey.
     */
    protected abstract int getBreedingAge();

    /**
     * Returns the maximum age that a prey can live before dying of old age.
     * Specific prey types must provide their own maximum age.
     * 
     * @return The maximum age of the prey.
     */
    protected abstract int getMaxAge();

    /**
     * Returns the maximum number of offspring a prey can produce in one breeding cycle.
     * Specific prey types must define their own litter size.
     * 
     * @return The maximum litter size for the prey.
     */
    protected abstract int getMaxLitterSize();

    /**
     * Returns the food value of the prey when consumed by predators.
     * 
     * @return The food value of the prey.
     */
    protected abstract int getFoodValue();

    /**
     * Creates a new instance of the prey as an offspring.
     * Specific prey types must implement this method to return a new prey instance.
     * 
     * @param location The location where the new prey will be placed.
     * @return A new PreyMob instance representing the offspring.
     */
    protected abstract PreyMob createOffspring(Location location);

    /**
     * Checks if the prey is male or female.
     * Specific prey types must define their gender.
     * 
     * @return true if the prey is male, false if female.
     */
    protected abstract boolean isMale();

    /**
     * Returns the hunger limit of the prey.
     * This is the maximum hunger level before the prey dies of starvation.
     * 
     * @return The hunger limit of the prey.
     */
    protected abstract int getHungerLimit();


    /**
     * Defines the generic actions of a Prey Mob during each simulation step.
     * This method handles aging, hunger increase, potential infection, breeding, and feeding.
     * It provides a standard behavior for all prey entities, which can be overridden by subclasses if needed.
     *
     * The default behavior is as follows:
     * - Increment age and hunger; if either exceeds limits, the prey dies.
     * - Attempt to infect nearby entities if diseased.
     * - If there are free adjacent locations and the food level is sufficiently high (above 60% of hunger limit),
     *   attempt to breed.
     * - If hunger is below 50% of the hunger limit, attempt to find food.
     * - If no food is found and there are free locations, move to one of those locations.
     * - If no movement is possible, gradually decrease the food level until the prey eventually dies of starvation.
     *
     * @param currentField The current state of the field.
     * @param nextFieldState The new field state being built for the next simulation step.
     */
    public void act(Field currentField, Field nextFieldState) {
        if (isAlive()) {
            incrementAge();      // Increase age and check for death by old age.
            incrementHunger();   // Increase hunger and check for starvation.
            tryToInfect(this, currentField); // Attempt to infect others if diseased.

            // Retrieve a list of free adjacent locations for movement or breeding.
            List<Location> freeLocations = nextFieldState.getFreeAdjacentLocations(getLocation());

            // Only attempt breeding if there is sufficient food (hunger above 60% of limit).
            if (!freeLocations.isEmpty() && foodLevel > (getHungerLimit() * 0.6)) {
                tryToBreed(nextFieldState, freeLocations);
            }

            // Attempt to find food if hunger is below 50% of the hunger limit.
            Location nextLocation = (foodLevel < (getHungerLimit() * 0.5)) ? findFood(currentField) : null;

            // If no food is found and free locations exist, choose a random free location to move.
            if (nextLocation == null && !freeLocations.isEmpty()) {
                nextLocation = findBestMove(freeLocations);
                nextFieldState.placeObject(this, nextLocation);
            }

            // Move to the determined location or gradually decrease food level if unable to move.
            if (nextLocation != null) {
                setLocation(nextLocation);
                nextFieldState.placeObject(this, nextLocation);
            } else {
                // Decrease food level gradually (lose 5% of maximum hunger per step).
                foodLevel -= getHungerLimit() * 0.05;
                if (foodLevel <= 0) {
                    setDead();
                }
            }
        }
    }


    /**
     * Handles the breeding process for the prey.
     * If a mate of the opposite gender is found, creates new offspring at available locations.
     * 
     * @param nextFieldState The field state for the next simulation step.
     * @param freeLocations The list of free locations where offspring can be placed.
     */
    protected void tryToBreed(Field nextFieldState, List<Location> freeLocations) {
        if (canBreed() && BreedingManager.canBreed(this.getClass(), nextFieldState) && foodLevel >= 30) {
            List<Location> adjacent = nextFieldState.getAdjacentLocations(getLocation());
            for (Location loc : adjacent) {
                Object mob = nextFieldState.getObjectAt(loc);
                if (mob == null) continue;

                if (this.getClass().equals(mob.getClass())) {
                    PreyMob partner = (PreyMob) mob;

                    // Check if the potential partner is of the opposite gender.
                    if (this.isMale() != partner.isMale()) {
                        int births = rand.nextInt(getMaxLitterSize()) + 1;

                        // Create offspring in available free locations.
                        for (int b = 0; b < births && !freeLocations.isEmpty(); b++) {
                            Location birthLoc = freeLocations.remove(0);
                            PreyMob young = createOffspring(loc);
                            nextFieldState.placeObject(young, birthLoc);
                        }
                    }
                }
            }
        }
    }

    /**
     * Allows the prey to find food in adjacent locations.
     * The prey can eat grass if it is not in the seed state, gaining food value.
     * 
     * @param field The field in which the prey is searching for food.
     * @return The location of the food if found, otherwise null.
     */
    public Location findFood(Field field) {
        if (foodLevel >= getHungerLimit()) {
            return null; // Prey is already full, no need to eat.
        }

        List<Location> adjacent = field.getAdjacentLocations(getLocation());

        for (Location where : adjacent) {
            Object plant = field.getObjectAt(where);

            // Check if the object is grass and it is not in the seed state.
            if (plant instanceof Grass grass) {
                if (!grass.isSeed()) {
                    grass.setSeed(true); // Consume the grass by turning it back into a seed.
                    int foodGain = grass.getFoodValue();
                    foodLevel = Math.min(foodLevel + foodGain, getHungerLimit());
                    return where;
                }
            }
        }
        return null;
    }

    /**
     * Checks whether the prey is capable of breeding.
     * The prey must be alive and have reached the breeding age.
     * 
     * @return true if the prey can breed, false otherwise.
     */
    protected boolean canBreed() {
        return this.isAlive() && age >= getBreedingAge();
    }
}
