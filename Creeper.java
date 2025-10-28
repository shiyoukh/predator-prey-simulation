import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a creeper.
 * Creepers age, move, eat cows, pigs, and villagers, and die.
 * 
 * Credit: Mojang, inspired by Minecraft mechanics.
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public class Creeper extends PredatorMob implements DiseaseHandler, ImageProvider {
    
    // The age at which a creeper can start to breed.
    private static final int BREEDING_AGE = 20;
    // The maximum age a creeper can live before it dies naturally.
    private static final int MAX_AGE = 230;
    // The maximum number of offspring a creeper can produce at one time.
    private static final int MAX_LITTER_SIZE = 3;
    // The maximum hunger level before a creeper dies of starvation.
    private static final int HUNGER_LIMIT = 260;
    // Random number generator used for randomizing age, breeding, and other behaviors.
    private static final Random rand = Randomizer.getRandom();
    // Image representation of a healthy creeper.
    private static BufferedImage creeperImage;
    // Image representation of a diseased creeper.
    private static BufferedImage diseasedCreeperImage;
    // The probability of a creeper becoming diseased.
    private static final double DISEASE_RATE = 0.0005;
    // The probability of a disease spreading from this creeper to others.
    private static final double DISEASE_SPREAD_RATE = 0.007;
    // Tracks whether this creeper is currently diseased.
    private boolean isDiseased = false;


    /**
     * Constructs a new Creeper instance.
     * If randomAge is true, the creeper is initialized with a random age up to the maximum age.
     * Otherwise, the creeper starts at age 0.
     * The initial food level is set to a third of the hunger limit.
     * 
     * @param randomAge If true, the creeper will have a random age.
     * @param location The initial location of the creeper in the field.
     */
    public Creeper(boolean randomAge, Location location) {
        super(location);
        // Assign a random age if specified, otherwise start as a newborn.
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        } else {
            age = 0;
        }
        // Set the initial food level to a third of the maximum hunger limit.
        foodLevel = HUNGER_LIMIT / 3;
    }

    
    
    /**
     * Static initializer block to load images for the creeper.
     * Attempts to load both healthy and diseased creeper images from the file system.
     * If the images cannot be loaded, an error message is printed to the console.
     */
    static {
        try {
            // Load the image for a healthy creeper.
            creeperImage = ImageIO.read(Creeper.class.getResourceAsStream("Images/creeper.jpg"));
            // Load the image for a diseased creeper.
            diseasedCreeperImage = ImageIO.read(Creeper.class.getResourceAsStream("Images/diseasedCreeper.jpg"));
        } catch (IOException e) {
            // Output an error message if the image files cannot be loaded.
            System.err.println("Error loading creeper images: " + e.getMessage());
        }
    }


     /**
     * Returns the appropriate image for the creeper.
     * If the creeper is diseased, the diseased image is returned.
     * Otherwise, the healthy creeper image is displayed.
     * 
     * @return BufferedImage of the creeper based on its health status.
     */
    @Override
    public BufferedImage getImage() {
        // Return the diseased image if the creeper is diseased, otherwise return the healthy image.
        return isDiseased ? diseasedCreeperImage : creeperImage;
    }


       /**
     * Defines the behavior of the creeper during each step of the simulation.
     * Handles aging, hunger, infection, breeding, hunting, movement, and starvation.
     * 
     * @param currentField The current field containing all the mobs.
     * @param nextFieldState The field representing the next state after the act is completed.
     */
    @Override
    public void act(Field currentField, Field nextFieldState) {

        if (isAlive()) {
            incrementAge(); // Increase age, may result in death if maximum age is reached.
            incrementHunger(); // Increase hunger, may result in death if starvation occurs.
            tryToInfect(this, currentField); // Attempt to infect nearby mobs if this creeper is diseased.

            // Get a list of free locations around the current position for possible movement or breeding.
            List<Location> freeLocations = nextFieldState.getFreeAdjacentLocations(getLocation());

            // Step 1: Breed only if hunger is above 50% and the area is not overcrowded.
            int nearbyCreepers = currentField.countNearbyMobs(getLocation(), Creeper.class);
            if (foodLevel > (HUNGER_LIMIT * 0.5) && nearbyCreepers < 4) {
                if (canBreed() && BreedingManager.canBreed(this.getClass(), nextFieldState)) {
                    giveBirth(nextFieldState, freeLocations); // Generate offspring in available locations.
                }
            }

            // Step 2: Attempt to hunt for prey in a small 3x3 area if hunger is below 60%.
            Location nextLocation = (foodLevel < (HUNGER_LIMIT * 0.6)) ? huntPrey(currentField) : null;

            // Step 3: If no prey is found, try to move to a free adjacent spot.
            if (nextLocation == null && !freeLocations.isEmpty()) {
                nextLocation = findBestMove(freeLocations); // Find the best available location to move.
                nextFieldState.placeObject(this, nextLocation); // Place the creeper in the new location.
            }

            // Step 4: Move to the determined location or reduce health if no move is possible.
            if (nextLocation != null) {
                setLocation(nextLocation); // Update the creeper's position.
                nextFieldState.placeObject(this, nextLocation); // Place the creeper in the new state.
            } else {
                // Gradually reduce health if the creeper is stuck and unable to move.
                foodLevel -= HUNGER_LIMIT * 0.05; // Decrease hunger by 5% of the max hunger per step.
                if (foodLevel <= 0) {
                    setDead(); // Kill the creeper if starvation threshold is reached.
                }
            }
        }
    }


    /**
     * Searches for prey within a 3x3 area around the creeper.
     * If the weather is rainy, the creeper will kill all adjacent prey.
     * Otherwise, the creeper will hunt for prey if its hunger level is low enough.
     * 
     * @param field The field in which the creeper is searching for prey.
     * @return The location of the prey if found, otherwise null.
     */
    @Override
    public Location huntPrey(Field field) {
        if (isAlive()) {
            // If the creeper is not hungry enough, it won't hunt.
            if (foodLevel >= HUNGER_LIMIT * 0.6) {
                return null;
            }

            // If it's raining, automatically kill all prey in adjacent locations.
            if (field.getWeather() == Weather.RAINY) {
                List<Location> adjacentLocations = field.getAdjacentLocations(getLocation());

                for (Location loc : adjacentLocations) {
                    Object obj = field.getObjectAt(loc);
                    if (obj instanceof PreyMob prey && obj instanceof Mob mob) {
                        mob.setDead(); // Kill the adjacent mob immediately.
                        // Increase the creeper's food level but do not exceed the hunger limit.
                        foodLevel = Math.min(foodLevel + prey.getFoodValue(), HUNGER_LIMIT);
                    }
                }
            }

            // Regular hunting behavior within a 3x3 area around the creeper.
            List<Location> nearby = field.getNearbyLocations(getLocation(), 3);

            for (Location loc : nearby) {
                Object obj = field.getObjectAt(loc);
                // Check if the object is prey and is still alive.
                if (obj instanceof PreyMob prey && obj instanceof Mob mob && mob.isAlive()) {
                    mob.setDead(); // Kill the prey.
                    // Increase food level without exceeding the hunger limit.
                    foodLevel = Math.min(foodLevel + prey.getFoodValue(), HUNGER_LIMIT);
                    return loc; // Move to the location of the killed prey.
                }
            }
        }
        // Return null if no prey is found.
        return null;
    }


    /**
     * Returns the type of mob that this creeper represents.
     * 
     * @return The MobType enum value for this creeper (MobType.CREEPER).
     */
    public MobType getMobType() {
        // The creeper is categorized as a CREEPER type in the simulation.
        return MobType.CREEPER;
    }


    /**
     * Returns the minimum age at which the creeper is capable of breeding.
     * 
     * @return The breeding age of the creeper.
     */
    @Override
    protected int getBreedingAge() {
        // The creeper can start breeding once it reaches the defined breeding age.
        return BREEDING_AGE;
    }


    /**
     * Returns the maximum age that a creeper can live before it dies of old age.
     * 
     * @return The maximum age of the creeper.
     */
    @Override
    protected int getMaxAge() {
        // The creeper will die naturally once it exceeds this age.
        return MAX_AGE;
    }


    /**
     * Returns the maximum number of offspring that a creeper can produce in one breeding cycle.
     * 
     * @return The maximum litter size for the creeper.
     */
    @Override
    protected int getMaxLitterSize() {
        // Defines the upper limit of how many baby creepers can be born at once.
        return MAX_LITTER_SIZE;
    }


      /**
     * Creates a new Creeper instance as an offspring.
     * The offspring is always created as a newborn with age set to 0.
     * 
     * @param location The location where the new creeper will be placed.
     * @return A new Creeper instance at the specified location.
     */
    @Override
    protected PredatorMob createOffspring(Location location) {
        // Instantiate a new creeper at the given location, starting at age 0.
        return new Creeper(false, location);
    }


      /**
     * Returns the base rate at which the creeper can become diseased.
     * This rate is used to calculate the probability of the creeper contracting a disease.
     * 
     * @return The disease rate for the creeper.
     */
    @Override
    public double getDiseaseRate() {
        // The probability that the creeper will become diseased per simulation step.
        return DISEASE_RATE;
    }


    /**
     * Returns the rate at which the creeper can spread disease to other mobs.
     * This rate is used to calculate the probability of infection spreading to nearby entities.
     * 
     * @return The disease spread rate for the creeper.
     */
    @Override
    public double getDiseaseSpreadRate() {
        // The probability of this creeper spreading its disease to others per step.
        return DISEASE_SPREAD_RATE;
    }


    /**
     * Sets the disease status of the creeper.
     * If the creeper becomes diseased, its age is immediately incremented to accelerate aging.
     * 
     * @param diseased true if the creeper should be marked as diseased, false otherwise.
     */
    @Override
    public void setDiseased(boolean diseased) {
        // Update the diseased status of the creeper.
        isDiseased = diseased;
        
        // If the creeper is diseased, rapidly increase its age to simulate faster aging.
        if (isDiseased) {
            incrementAge(age); // The creeper ages by its current age, effectively doubling it.
        }
    }


    /**
     * Checks whether the creeper is currently diseased.
     * 
     * @return true if the creeper is diseased, false otherwise.
     */
    @Override
    public boolean isDiseased() {
        // Returns the current disease status of the creeper.
        return isDiseased;
    }

}
