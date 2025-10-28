import java.util.*;

/**
 * Field class represents a generic field that can contain both mobs and plants.
 * It manages the grid-based environment where mobs and plants interact within a simulation.
 * 
 * This class is responsible for:
 * - Storing and managing all entities (mobs and plants) within the field.
 * - Keeping track of environmental conditions such as time of day, weather, and season.
 * - Providing methods to interact with the grid, such as adding and retrieving objects.
 * 
 * @author David J. Barnes, Michael Kölling, and Ali Alshiyoukh
 * @version 7.1
 */
public class Field {

    // Shared random number generator for the simulation.
    private static final Random rand = Randomizer.getRandom();

    // Dimensions of the field.
    private final int depth;
    private final int width;

    // Maps each grid location to an object (mob or plant).
    private final LinkedHashMap<Location, Object> field = new LinkedHashMap<>();
    
    // Lists to store all mobs and plants present in the field.
    private final LinkedList<Mob> mobs = new LinkedList<>();
    private final LinkedList<Plant> plants = new LinkedList<>();

    // Counters for different types of entities in the field.
    int numZombies = 0, numCows = 0, numPigs = 0, numVillagers = 0, numGrass = 0, numCreepers = 0;

    // Current environmental conditions in the field.
    private TimeOfDay timeOfDay = TimeOfDay.DAY; // Tracks whether it is day or night.
    private Weather weather = Weather.CLEAR;      // Tracks the current weather condition.
    private Season season = Season.SUMMER;        // Tracks the current season.

    /**
     * Creates a new field with the specified dimensions.
     * 
     * @param depth The depth (number of rows) of the field.
     * @param width The width (number of columns) of the field.
     */
    public Field(int depth, int width) {
        this.depth = depth;
        this.width = width;
    }



     /**
     * Retrieves a list of all free (empty) locations within the field.
     * The method iterates over the entire grid and adds locations without objects to the list.
     *
     * Can be useful in cases where plants/objects can spawn in empty locations during the simulation (Currently no uses)
     *
     * @return A list of free locations where new objects can be placed.
     */
    public List<Location> getFreeLocations() {
        // Initialize a list to hold all the available free locations.
        List<Location> freeLocations = new ArrayList<>();

        // Iterate through each row of the field.
        for (int row = 0; row < getDepth(); row++) {
            // Iterate through each column within the current row.
            for (int col = 0; col < getWidth(); col++) {
                Location loc = new Location(row, col);
                // Check if the current location is empty.
                if (getObjectAt(loc) == null) {
                    // Add the free location to the list.
                    freeLocations.add(loc);
                }
            }
        }
        // Return the list of all free locations in the field.
        return freeLocations;
    }


    /**
     * Returns a list of nearby locations within a specified radius around a given location.
     * This method is useful for finding adjacent or nearby entities within a defined area.
     * 
     * @param location The central location from which to search for nearby locations.
     * @param radius The radius around the location to search for other locations.
     * @return A list of nearby locations, excluding the original location itself.
     */
    public List<Location> getNearbyLocations(Location location, int radius) {
        // List to store all the nearby locations within the specified radius.
        List<Location> nearbyLocations = new ArrayList<>();

        // Define the bounds of the search area, ensuring they stay within field limits.
        int startRow = Math.max(0, location.row() - radius);
        int endRow = Math.min(getDepth() - 1, location.row() + radius);
        int startCol = Math.max(0, location.col() - radius);
        int endCol = Math.min(getWidth() - 1, location.col() + radius);

        // Iterate through the defined grid area to find all nearby locations.
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                // Exclude the original location itself from the results.
                if (row != location.row() || col != location.col()) {
                    nearbyLocations.add(new Location(row, col));
                }
            }
        }

        // Return the list of all valid nearby locations.
        return nearbyLocations;
    }


    /**
     * Counts the number of mobs of a specific class type in adjacent locations.
     * This method is useful for checking the population density of specific mobs around a location.
     * 
     * @param location The central location to check around.
     * @param mobClass The class type of the mobs to count (e.g., Creeper.class, Cow.class).
     * @return The number of adjacent mobs that match the specified class type.
     */
    public int countNearbyMobs(Location location, Class<?> mobClass) {
        int count = 0;

        // Get all adjacent locations around the specified location.
        List<Location> adjacentLocations = getAdjacentLocations(location);

        // Iterate through each adjacent location.
        for (Location adj : adjacentLocations) {
            // Get the object present at the current adjacent location.
            Object obj = getObjectAt(adj);
            
            // If the object is an instance of the specified mob class, increment the count.
            if (mobClass.isInstance(obj)) {
                count++;
            }
        }

        // Return the total count of nearby mobs of the specified type.
        return count;
    }


    /**
     * Places an object (either a Mob or a Plant) into the specified location in the field.
     * The method ensures that only one type of object (Mob or Plant) occupies a location at a time.
     * If the specified location already contains an incompatible object, the new object is not placed.
     * 
     * @param obj The object to place in the field, either a Mob or a Plant.
     * @param location The location where the object should be placed.
     */
    public void placeObject(Object obj, Location location) {
        // Avoid null objects or invalid locations.
        if (obj == null || location == null) return;

        // Get the existing object at the target location.
        Object existing = field.get(location);

        // Check if the object is a Mob and the existing object is not a Mob.
        if (obj instanceof Mob) {
            if (!(existing instanceof Mob)) {
                field.put(location, obj); // Place the mob in the field.
                mobs.add((Mob) obj); // Add the mob to the list of all mobs.
            }
        }

        // Check if the object is a Plant and the existing object is not a Plant.
        if (obj instanceof Plant) {
            if (!(existing instanceof Plant)) {
                field.put(location, obj); // Place the plant in the field.
                plants.add((Plant) obj); // Add the plant to the list of all plants.
            }
        }
    }


    /**
     * Returns a list of all plants currently present in the field.
     * The list includes all plants regardless of their location.
     * 
     * @return A list containing all Plant objects in the field.
     */
    public List<Plant> getPlants() {
        // Return the list of plants managed by the field.
        return plants;
    }


    /**
     * Removes an object from the field.
     * This method specifically handles Mob objects, removing them from the field and marking them as dead.
     * 
     * @param obj The object to remove, expected to be a Mob instance.
     */
    public void removeObject(Object obj) {
        // Check if the object is a Mob.
        if (obj instanceof Mob mob) {
            // Remove the mob from the list of active mobs.
            mobs.remove(mob);
            // Remove the mob from its current location in the field.
            field.remove(mob.getLocation());
            // Mark the mob as dead to stop its behavior in the simulation.
            mob.setDead();
            // Nullify the object reference to assist garbage collection.
            obj = null;
        }
    }

    
    /**
     * Retrieves the object present at a specific location in the field.
     * The object could be a Mob, a Plant, or null if the location is empty.
     * 
     * @param location The location to check for an object.
     * @return The object at the specified location, or null if the location is empty.
     */
    public Object getObjectAt(Location location) {
        // Return the object stored at the given location in the field map.
        return field.get(location);
    }


    /**
     * Retrieves a list of free or plant-occupied adjacent locations around a given location.
     * Free locations are either empty or contain plants, allowing mobs to move or breed there.
     * 
     * @param location The central location to check around.
     * @return A list of free or plant-occupied adjacent locations.
     */
    public List<Location> getFreeAdjacentLocations(Location location) {
        // List to store free or plant-occupied locations.
        List<Location> free = new LinkedList<>();
        // Get all adjacent locations around the specified location.
        List<Location> adjacent = getAdjacentLocations(location);

        // Check each adjacent location for availability.
        for (Location next : adjacent) {
            Object obj = getObjectAt(next);
            // Add location to the list if it is empty or contains a plant.
            if (obj == null || obj instanceof Plant) {
                free.add(next);
            }
        }

        // Return the list of free adjacent locations.
        return free;
    }



    /**
     * Prints the current count of each type of entity present in the field.
     * This method iterates through all objects in the field, checks their type,
     * and counts only the entities that are still alive.
     * The results are displayed in the console with a formatted summary.
     */
    public void fieldStats() {

        // Iterate through all objects currently in the field.
        for (Object obj : field.values()) {

            // Check the type of each object and count only if it is alive.
            if (obj instanceof Zombie zombie && zombie.isAlive()) {
                numZombies++;
            } else if (obj instanceof Cow cow && cow.isAlive()) {
                numCows++;
            } else if (obj instanceof Pig pig && pig.isAlive()) {
                numPigs++;
            } else if (obj instanceof Villager villager && villager.isAlive()) {
                numVillagers++;
            } else if (obj instanceof Grass grass && grass.isAlive()) {
                numGrass++;
            } else if (obj instanceof Creeper creeper && creeper.isAlive()) {
                numCreepers++;
            }
        }

        // Output the statistics of each type of entity to the console.
        System.out.println("Zombies: " + numZombies + " | Creepers: " + numCreepers + " | Cows: " + numCows +
                " | Pigs: " + numPigs + " | Villagers: " + numVillagers + " | Grass: " + numGrass);
    }


    /**
     * Retrieves a list of adjacent locations around a specified location.
     * The method includes all valid adjacent grid positions, ensuring they remain within field boundaries.
     * The returned list is shuffled randomly to prevent biased movement patterns.
     * 
     * @param location The central location to check around.
     * @return A list of adjacent locations, randomly shuffled.
     */
    public List<Location> getAdjacentLocations(Location location) {
        // Initialize a list to store the adjacent locations.
        List<Location> locations = new ArrayList<>();

        // Ensure the provided location is not null.
        if (location != null) {
            int row = location.row(); // Get the row index of the location.
            int col = location.col(); // Get the column index of the location.

            // Iterate over a 3x3 grid around the specified location.
            for (int roffset = -1; roffset <= 1; roffset++) {
                int nextRow = row + roffset;

                // Ensure the row index is within the field's boundaries.
                if (nextRow >= 0 && nextRow < depth) {
                    for (int coffset = -1; coffset <= 1; coffset++) {
                        int nextCol = col + coffset;

                        // Check if the column is within boundaries and exclude the original location.
                        if (nextCol >= 0 && nextCol < width && (roffset != 0 || coffset != 0)) {
                            // Add the valid adjacent location to the list.
                            locations.add(new Location(nextRow, nextCol));
                        }
                    }
                }
            }
            
            // Shuffle the list of adjacent locations to introduce randomness.
            Collections.shuffle(locations, rand);
        }

        // Return the list of shuffled adjacent locations.
        return locations;
    }


    /**
     * Clears all objects from the field, including mobs and plants.
     * This method resets the field to an empty state, removing all stored entities.
     */
    public void clear() {
        // Clear all objects from the field grid.
        field.clear();
        // Clear the list of all active mobs in the simulation.
        mobs.clear();
        // Clear the list of all plants to maintain consistency.
        plants.clear();
    }

    
    /**
     * Checks whether the field is still viable for the simulation.
     * The field is considered viable if at least one type of mob is present.
     * If no mobs are left, the simulation step at which it ended is printed.
     * 
     * @return true if there are any living mobs in the field, false otherwise.
     */
    public boolean isViable() {
        // Check if any of the main mob types are still present in the field.
        boolean hasLivingMobs = numZombies > 0 || numCows > 0 || numPigs > 0 || numVillagers > 0 || numCreepers > 0;

        // If no mobs are left, log the step at which the simulation ended.
        if (!hasLivingMobs) {
            System.out.println("Ended at step: " + Simulator.getStep());
        }

        // Return true if the field contains any living mobs, otherwise false.
        return hasLivingMobs;
    }

    /**
     * Returns the current time of day in the field.
     *
     * @return The current TimeOfDay, either DAY or NIGHT.
     */
    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    /**
     * Sets the current time of day in the field.
     * This affects the behavior of certain mobs and plants that react to day and night cycles.
     *
     * @param time The new time of day, either TimeOfDay.DAY or TimeOfDay.NIGHT.
     */
    public void setTimeOfDay(TimeOfDay time) {
        // Update the field's time of day to the specified value.
        this.timeOfDay = time;
    }

    /**
     * Returns the current weather condition in the field.
     * The weather can influence mob behavior and plant growth.
     *
     * @return The current Weather, such as SUNNY, RAINY, or SNOWY.
     */
    public Weather getWeather() {
        // Retrieve the current weather setting for the field.
        return weather;
    }

    /**
     * Sets the current weather condition in the field.
     * The weather can affect the behavior of mobs and plants, including breeding and movement.
     *
     * @param weather The new weather condition, e.g., SUNNY, RAINY, or SNOWY.
     */
    public void setWeather(Weather weather) {
        // Update the field's weather to the specified condition.
        this.weather = weather;
    }


    /**
     * Sets the current season in the field.
     * The season affects environmental conditions and mob behaviors, such as breeding and survival.
     *
     * @param season The new season, e.g., SUMMER, WINTER, SPRING, or FALL.
     */
    public void setSeason(Season season) {
        // Update the field's season to the specified season.
        this.season = season;
    }


    /**
     * Returns the current season in the field.
     * The season can influence mob behavior, plant growth, and disease rates.
     *
     * @return The current season, e.g., SUMMER, WINTER, SPRING, or FALL.
     */
    public Season getSeason() {
        // Retrieve the current season set for the field.
        return season;
    }

    /**
     * Returns a list of all mobs currently present in the field.
     * This list includes all living entities that can act within the simulation.
     * 
     * @return A list containing all Mob objects in the field.
     */
    public List<Mob> getMobs() {
        // Return the list of mobs managed by the field.
        return mobs;
    }

    /**
     * Counts the total population of a specific type of entity within the field.
     * This method can be used for both mobs and plants by providing the appropriate class type.
     * 
     * @param entityClass The class type of the entities to count (e.g., Creeper.class, Grass.class).
     * @return The total number of entities of the specified type present in the field.
     */
    public int getPopulation(Class<?> entityClass) {
        int count = 0;

        // Iterate through all objects in the field.
        for (Object obj : field.values()) {
            // Increment the count if the object is an instance of the specified class.
            if (entityClass.isInstance(obj)) {
                count++;
            }
        }

        // Return the total count of entities matching the specified class.
        return count;
    }


    /**
     * Retrieves a list of all mobs of a specific class type present in the field.
     * This method uses generics to safely return a list of mobs of the specified type.
     * 
     * @param <T> The type of the mob (e.g., Creeper, Cow, Zombie).
     * @param mobClass The class type of the mobs to retrieve.
     * @return A list of mobs of the specified type, or an empty list if none are found.
     */
    public <T> List<T> getMobs(Class<T> mobClass) {
        // Initialize a list to store mobs of the specified class type.
        List<T> mobs = new ArrayList<>();

        // Iterate through all objects in the field.
        for (Object obj : field.values()) {
            // Check if the object is an instance of the specified mob class.
            if (mobClass.isInstance(obj)) {
                // Cast the object to the correct type and add it to the list.
                mobs.add(mobClass.cast(obj));
            }
        }

        // Return the list of mobs of the specified type.
        return mobs;
    }


    /**
     * Returns the depth of the field (i.e., the number of rows).
     * This value defines the vertical size of the grid.
     * 
     * @return The depth of the field.
     */
    public int getDepth() {
        // Return the total number of rows in the field grid.
        return depth;
    }

    /**
     * Returns the width of the field (i.e., the number of columns).
     * This value defines the horizontal size of the grid.
     * 
     * @return The width of the field.
     */
    public int getWidth() {
        // Return the total number of columns in the field grid.
        return width;
    }

}
