import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a zombie.
 * Zombies age, move, eat cows, pigs, and villagers, and die.
 * Zombies can also become diseased and spread disease.
 * Additionally, zombies have a mechanism for spawning new zombies in the morning.
 * 
 * 
 * Credit: Mojang, inspired by Minecraft mechanics.
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public class Zombie extends PredatorMob implements DiseaseHandler, ImageProvider {
    // The age at which a zombie can start to breed.
    private static final int BREEDING_AGE = 15;
    // The maximum age a zombie can reach.
    private static final int MAX_AGE = 300;
    // The maximum number of offspring a zombie can produce in one breeding cycle.
    private static final int MAX_LITTER_SIZE = 3;
    // The hunger limit before a zombie dies of starvation.
    private static final int HUNGER_LIMIT = 260;
    // Random number generator for zombie behavior.
    private static final Random rand = Randomizer.getRandom();
    // Counter for zombies that need to be spawned in the morning.
    private static int zombiesToSpawn = 0;
    // Images for a healthy zombie and a diseased zombie.
    private static BufferedImage zombieImage;
    private static BufferedImage diseasedZombieImage;
    // The base rate at which a zombie can become diseased.
    private static final double DISEASE_RATE = 0.0005;
    // The rate at which a zombie can spread disease to others.
    private static final double DISEASE_SPREAD_RATE = 0.007;
    // Indicates whether the zombie is currently diseased.
    private boolean isDiseased = false;

    /**
     * Constructs a new Zombie instance.
     * If randomAge is true, the zombie is assigned a random age up to MAX_AGE;
     * otherwise, it is created as a newborn (age 0).
     * The initial food level is set to a third of the hunger limit.
     * 
     * @param randomAge If true, assigns a random age to the zombie.
     * @param location The initial location of the zombie in the field.
     */
    public Zombie(boolean randomAge, Location location) {
        super(location);
        if (randomAge) {
            age = rand.nextInt(MAX_AGE);
        } else {
            age = 0;
        }
        foodLevel = HUNGER_LIMIT / 3;
    }

    // Static block to load zombie images only once.
    static {
        try {
            zombieImage = ImageIO.read(Zombie.class.getResourceAsStream("Images/zombie.jpg"));
            diseasedZombieImage = ImageIO.read(Zombie.class.getResourceAsStream("Images/diseasedZombie.jpg"));
        } catch (IOException e) {
            System.err.println("Error loading zombie images: " + e.getMessage());
        }
    }

    /**
     * Returns the image representing the zombie.
     * If the zombie is diseased, returns the diseased image; otherwise, returns the normal image.
     * 
     * @return A BufferedImage of the zombie.
     */
    @Override
    public BufferedImage getImage() {
        return isDiseased ? diseasedZombieImage : zombieImage;
    }

    /**
     * Spawns new zombies during the day based on the zombiesToSpawn counter.
     * The method picks a random existing zombie as a "parent" and attempts to create new zombies
     * in free adjacent locations. The spawn count is capped by the number of existing zombies.
     * 
     * @param nextFieldState The field representing the next simulation state.
     */
    private static void spawnZombies(Field nextFieldState) {
        // Only spawn zombies during the day and if there are zombies queued for spawning.
        if (nextFieldState.getTimeOfDay() != TimeOfDay.DAY || zombiesToSpawn <= 0) {
            return;
        }

        // Retrieve the list of existing zombies in the field.
        List<Zombie> existingZombies = nextFieldState.getMobs(Zombie.class);
        if (existingZombies.isEmpty()) return;

        // Limit the number of zombies spawned to the smaller of zombiesToSpawn and the number of existing zombies.
        int spawnCount = Math.min(zombiesToSpawn, existingZombies.size());

        for (int i = 0; i < spawnCount; i++) {
            // Choose a random parent zombie from the existing list.
            Zombie parentZombie = existingZombies.get(rand.nextInt(existingZombies.size()));
            // Retrieve free adjacent locations near the parent zombie.
            List<Location> adjacentLocations = nextFieldState.getFreeAdjacentLocations(parentZombie.getLocation());

            if (!adjacentLocations.isEmpty()) {
                // Choose a random free location for the new zombie.
                Location spawnLocation = adjacentLocations.get(rand.nextInt(adjacentLocations.size()));

                // Create a new zombie offspring near the parent.
                Zombie newZombie = new Zombie(false, spawnLocation);
                newZombie.setAge(rand.nextInt(Zombie.MAX_AGE / 2)); // Assign a random age up to half the maximum age.
                newZombie.setFoodLevel(rand.nextInt(Zombie.HUNGER_LIMIT / 3)); // Assign a random hunger level.

                // Place the new zombie in the field.
                nextFieldState.placeObject(newZombie, spawnLocation);
            }
        }

        // Reduce the spawn counter by the number of zombies spawned.
        zombiesToSpawn -= spawnCount;
    }

    /**
     * Defines the actions of the zombie during each simulation step.
     * The zombie will age, become hungrier, try to infect others, spawn new zombies (if applicable),
     * breed with nearby zombies if conditions are met, hunt for prey, and move accordingly.
     * If no movement is possible, the zombie gradually loses food and may die.
     * 
     * @param currentField The current state of the field.
     * @param nextFieldState The field representing the next simulation state.
     */
    @Override
    public void act(Field currentField, Field nextFieldState) {
        if (isAlive()) {
            // Age the zombie and increase its hunger.
            incrementAge();
            incrementHunger();
            // Attempt to infect nearby entities if this zombie is diseased.
            tryToInfect(this, currentField);

            // Spawn new zombies during the day.
            spawnZombies(nextFieldState);

            // Retrieve free adjacent locations for potential movement or breeding.
            List<Location> freeLocations = nextFieldState.getFreeAdjacentLocations(getLocation());

            // Attempt breeding if there are free locations, the zombie is well-fed, and overcrowding is minimal.
            int nearbyZombies = currentField.countNearbyMobs(getLocation(), Zombie.class);
            if (foodLevel > (HUNGER_LIMIT * 0.5) && nearbyZombies < 4) {
                if (canBreed() && BreedingManager.canBreed(this.getClass(), nextFieldState)) {
                    giveBirth(nextFieldState, freeLocations);
                }
            }

            // Attempt to hunt prey if the zombie is sufficiently hungry.
            Location nextLocation = (foodLevel < (HUNGER_LIMIT * 0.6)) ? huntPrey(currentField) : null;

            // If no prey is found and free adjacent locations exist, choose the best available move.
            if (nextLocation == null && !freeLocations.isEmpty()) {
                nextLocation = findBestMove(freeLocations);
                nextFieldState.placeObject(this, nextLocation);
            }

            // Move the zombie if a valid target location is found.
            if (nextLocation != null) {
                setLocation(nextLocation);
                nextFieldState.placeObject(this, nextLocation);
            } else {
                // If the zombie cannot move, reduce its food level gradually.
                foodLevel -= HUNGER_LIMIT * 0.05;
                if (foodLevel <= 0) {
                    setDead();
                }
            }
        }
    }

    /**
     * Searches for prey within a 3x3 area around the zombie.
     * If prey is found, the zombie kills the prey, increases its food level,
     * and if hunting occurs at night, increments the zombiesToSpawn counter.
     * 
     * @param field The field in which the zombie hunts for prey.
     * @return The location of the prey if found; otherwise, returns null.
     */
    @Override
    public Location huntPrey(Field field) {
        if (isAlive()) {
            // If the zombie is not hungry enough, it will not hunt.
            if (foodLevel >= HUNGER_LIMIT * 0.6) {
                return null;
            }

            // Search for prey within a 3x3 area around the zombie's current location.
            List<Location> nearby = field.getNearbyLocations(getLocation(), 3);

            for (Location loc : nearby) {
                Object obj = field.getObjectAt(loc);
                if (obj instanceof PreyMob prey && obj instanceof Mob mob && mob.isAlive()) {
                    // Kill the prey and increase the zombie's food level.
                    mob.setDead();
                    foodLevel = Math.min(foodLevel + prey.getFoodValue(), HUNGER_LIMIT);

                    // If the hunt occurs at night, increment the zombie spawn counter.
                    if (field.getTimeOfDay() == TimeOfDay.NIGHT) {
                        zombiesToSpawn = Math.min(++zombiesToSpawn, 400);
                    }
                    // Return the location where the prey was found.
                    return loc;
                }
            }
        }
        // Return null if no prey is found.
        return null;
    }


    /**
     * Sets the age of the zombie to a specific value.
     * This method is used during zombie spawning to assign a random age.
     * 
     * @param age The new age for the zombie.
     */
    private void setAge(int age) {
        this.age = age;
    }

    /**
     * Sets the food level of the zombie to a specific value.
     * This method is used during zombie spawning to assign a random hunger level.
     * 
     * @param foodLevel The new food level for the zombie.
     */
    private void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    /**
     * Returns the minimum age at which a zombie can breed.
     * 
     * @return The breeding age for zombies.
     */
    @Override
    protected int getBreedingAge() {
        return BREEDING_AGE;
    }

    /**
     * Returns the maximum age that a zombie can live.
     * 
     * @return The maximum age for zombies.
     */
    @Override
    protected int getMaxAge() {
        return MAX_AGE;
    }

    /**
     * Returns the maximum number of offspring a zombie can produce in one breeding cycle.
     * 
     * @return The maximum litter size for zombies.
     */
    @Override
    protected int getMaxLitterSize() {
        return MAX_LITTER_SIZE;
    }

    /**
     * Returns the MobType representing a zombie.
     * 
     * @return MobType.ZOMBIE.
     */
    public MobType getMobType() {
        return MobType.ZOMBIE;
    }

    /**
     * Creates a new Zombie instance as an offspring.
     * The offspring is created as a newborn (age 0) at the specified location.
     * 
     * @param location The location for the new zombie.
     * @return A new Zombie instance.
     */
    @Override
    protected PredatorMob createOffspring(Location location) {
        return new Zombie(false, location);
    }

    /**
     * Returns the base disease rate for zombies.
     * 
     * @return The disease rate.
     */
    @Override
    public double getDiseaseRate() {
        return DISEASE_RATE;
    }

    /**
     * Returns the rate at which zombies can spread disease.
     * 
     * @return The disease spread rate.
     */
    @Override
    public double getDiseaseSpreadRate() {
        return DISEASE_SPREAD_RATE;
    }

    /**
     * Sets the disease status of the zombie.
     * If the zombie becomes diseased, its age is accelerated to simulate faster deterioration.
     * 
     * @param diseased true to mark the zombie as diseased, false otherwise.
     */
    @Override
    public void setDiseased(boolean diseased) {
        isDiseased = diseased;
        if (isDiseased) {
            incrementAge(age);
        }
    }

    /**
     * Checks whether the zombie is currently diseased.
     * 
     * @return true if the zombie is diseased, false otherwise.
     */
    @Override
    public boolean isDiseased() {
        return isDiseased;
    }
}
