import java.util.*;

/**
 * The Simulator class handles the main simulation loop and management of the field.
 * It initializes the simulation, manages the population of mobs and plants,
 * handles environmental changes, and controls the simulation steps.
 * 
 * The simulation includes:
 * - Day and night cycles
 * - Weather changes
 * - Seasonal changes
 * - Dynamic breeding and population management
 * 
 * The simulation runs until the field is no longer viable or the specified number of steps is reached.
 * 
 * @author David J. Barnes, Michael Kölling, and Ali Alshiyoukh
 * @version 1.0
 */
public class Simulator {

    // The default width of the simulation grid.
    private static final int DEFAULT_WIDTH = 120;

    // The default depth of the simulation grid.
    private static final int DEFAULT_DEPTH = 80;

    // Entity creation probabilities for initial population.
    private static final double CREEPER_CREATION_PROBABILITY = 0.04;
    private static final double ZOMBIE_CREATION_PROBABILITY = 0.04;
    private static final double COW_CREATION_PROBABILITY = 0.13;
    private static final double PIG_CREATION_PROBABILITY = 0.13;
    private static final double VILLAGER_CREATION_PROBABILITY = 0.13;
    private static final double GRASS_CREATION_PROBABILITY = 0.11;

    // Current environmental conditions of the simulation.
    private TimeOfDay currentTime = TimeOfDay.DAY;
    private Weather currentWeather = Weather.CLEAR;
    private Season currentSeason = Season.SUMMER;

    // The field that holds all entities within the simulation grid.
    private Field field;

    // The current step number of the simulation.
    private static int step;

    // The view component to visualize the simulation state.
    private final SimulatorView view;

    // Random number generator for simulation randomness.
    private final Random rand = new Random();

    /**
     * Constructs a simulation with default grid size.
     */
    public Simulator() {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }

    /**
     * Constructs a simulation with specified grid size.
     * If invalid dimensions are provided, defaults are used.
     * 
     * @param depth The depth (rows) of the grid.
     * @param width The width (columns) of the grid.
     */
    public Simulator(int depth, int width) {
        if (width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be >= zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }

        // Initialize the breeding manager with a random generator.
        BreedingManager.initialize(rand);

        // Initialize the simulation field and view.
        field = new Field(depth, width);
        view = new SimulatorView(depth, width);

        // Reset the simulation to its initial state.
        reset();
    }



    /**
     * Runs a long simulation for a default number of steps.
     * This method is useful for automated testing or extended observation.
     */
    public void runLongSimulation() {
        // Run the simulation for 2000 steps.
        simulate(2000);
    }

    /**
     * Runs the simulation for a specified number of steps.
     * During each step, the simulation advances through all active entities, 
     * performs garbage collection, and displays updated field statistics.
     * 
     * @param numSteps The number of steps to run the simulation.
     */
    public void simulate(int numSteps) {
        // Display initial statistics.
        reportStats();

        // Run the simulation for the specified number of steps or until the field is no longer viable.
        for (int n = 1; n <= numSteps && field.isViable(); n++) {
            simulateOneStep(); // Simulate a single step of the simulation.
            collectGarbage(); // Remove dead entities from the simulation.
            field.fieldStats(); // Display current field statistics.
            delay(100); // Introduce a delay for visualization.
        }
    }

    /**
     * Retrieves the current simulation step.
     * 
     * @return The current step number of the simulation.
     */
    public static int getStep() {
        return step;
    }

    /**
     * Changes the current weather condition to a random new weather.
     * Ensures that the new weather is different from the previous weather.
     */
    private void changeWeather() {
        // Get all possible weather types.
        Weather[] allWeathers = Weather.values(); 
        Weather newWeather;

        // Keep selecting a random weather until it's different from the current weather.
        do {
            newWeather = allWeathers[rand.nextInt(allWeathers.length)];
        } while (newWeather == currentWeather);

        // Update the current weather to the newly selected value.
        currentWeather = newWeather;
    }

    /**
     * Advances the current season to the next season in the cycle.
     * The seasons progress in the order: Summer -> Autumn -> Winter -> Spring -> Summer.
     */
    private void changeSeason() {
        switch (currentSeason) {
            case SUMMER -> currentSeason = Season.AUTUMN;
            case AUTUMN -> currentSeason = Season.WINTER;
            case WINTER -> currentSeason = Season.SPRING;
            case SPRING -> currentSeason = Season.SUMMER;
        }
    }

    /**
     * Collects and removes all dead mobs from the field.
     * This method uses an iterator to safely remove elements from the list while iterating.
     */
    private void collectGarbage() {
        // Create an iterator for the list of mobs in the field.
        Iterator<Mob> iterator = field.getMobs().iterator();

        // Iterate through all mobs.
        while (iterator.hasNext()) {
            Mob mob = iterator.next();

            // If the mob is not alive, remove it from the field.
            if (!mob.isAlive()) {
                iterator.remove();  // Removes the mob from the list safely.
                field.removeObject(mob); // Remove the mob from the field grid.
            }
        }
    }

    /**
     * Simulates a single step in the simulation.
     * Handles day/night cycles, weather and season changes, entity actions, and updates the field state.
     */
    private void simulateOneStep() {
        step++; // Increment the simulation step.

        // Handle day and night cycles based on step intervals.
        if (step % 300 == 0) {
            currentTime = TimeOfDay.DAY;
        } else if (step % 100 == 0) {
            currentTime = TimeOfDay.NIGHT;
        }

        // Change weather every 150 steps.
        if (step % 150 == 0) {
            changeWeather();
        }

        // Change season every 200 steps.
        if (step % 200 == 0) {
            changeSeason();
        }

        // Create a new field to represent the next state of the simulation.
        Field nextField = new Field(field.getDepth(), field.getWidth());
        nextField.setTimeOfDay(currentTime);
        nextField.setWeather(currentWeather);
        nextField.setSeason(currentSeason);

        // Update breeding thresholds dynamically based on current field state.
        BreedingManager.updateThresholds(field);

        // Transfer all plants (including dead ones) to the new field state.
        for (Plant plant : field.getPlants()) {
            // Ensure the plant is not already transferred to avoid duplication.
            if (!nextField.getPlants().contains(plant)) {
                nextField.placeObject(plant, plant.getLocation());
            }
            // Call the grow method on each plant to handle growth behavior.
            plant.grow(field, nextField);
        }

        // Perform actions for all living mobs in the field.
        for (Mob mob : field.getMobs()) {
            mob.act(field, nextField);
        }

        // Update the field to the newly generated state.
        field = nextField;

        // Update the visualization with the current state of the simulation.
        view.showStatus(step, field, currentTime, currentWeather, currentSeason);
    }


    /**
     * Resets the simulation to its initial state.
     * Clears the field, reinitializes entities, and updates the view to show the initial state.
     */
    private void reset() {
        step = 0; // Reset the simulation step counter.
        field.clear(); // Clear all objects from the field.
        populate(); // Repopulate the field with initial entities.
        view.showStatus(step, field, currentTime, currentWeather, currentSeason); // Display the reset state.
    }

    /**
     * Populates the field with initial entities based on creation probabilities.
     * The method fills the grid with mobs and plants at random locations.
     */
    private void populate() {
        Random rand = Randomizer.getRandom();

        // Iterate through every grid cell in the field.
        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                // Determine which type of entity (mob or plant) should occupy this location.
                Object entityType = getEntityTypeForPosition(rand);
                Location loc = new Location(row, col);

                // Place the appropriate entity at the location based on its type.
                if (entityType instanceof MobType mobType) {
                    placeMob(mobType, loc);
                } else if (entityType instanceof PlantType plantType) {
                    placePlant(plantType, loc);
                }
            }
        }
    }

    /**
     * Determines the type of entity to place at a grid location based on random probability.
     * Uses cumulative probability to randomly select between mobs and plants.
     * 
     * @param rand The Random instance used for generating random probabilities.
     * @return The type of entity (MobType or PlantType) to be placed, or null if no entity is selected.
     */
    private Object getEntityTypeForPosition(Random rand) {
        double probability = rand.nextDouble(); // Generate a random probability between 0.0 and 1.0.
        double cumulativeProbability = 0.0;

        // Map to hold entity types with their associated creation probabilities.
        Map<Object, Double> entityProbabilities = new LinkedHashMap<>();
        entityProbabilities.put(MobType.CREEPER, CREEPER_CREATION_PROBABILITY);
        entityProbabilities.put(MobType.ZOMBIE, ZOMBIE_CREATION_PROBABILITY);
        entityProbabilities.put(MobType.COW, COW_CREATION_PROBABILITY);
        entityProbabilities.put(MobType.PIG, PIG_CREATION_PROBABILITY);
        entityProbabilities.put(MobType.VILLAGER, VILLAGER_CREATION_PROBABILITY);
        entityProbabilities.put(PlantType.GRASS, GRASS_CREATION_PROBABILITY);

        // Iterate through each entity type and its probability.
        for (Map.Entry<Object, Double> entry : entityProbabilities.entrySet()) {
            cumulativeProbability += entry.getValue();

            // Select the entity type if the random probability is within its cumulative range.
            if (probability <= cumulativeProbability) {
                return entry.getKey();
            }
        }
        
        // Return null if no entity is selected, meaning this location remains empty.
        return null;
    }

        /**
     * Places a specific type of mob at the given location in the field.
     * Each mob type is instantiated with random attributes.
     * 
     * @param type The MobType to place (e.g., Creeper, Cow, Pig, Zombie, Villager).
     * @param location The Location where the mob will be placed.
     */
    private void placeMob(MobType type, Location location) {
        if (type == null) return;
        
        switch (type) {
            case CREEPER -> field.placeObject(new Creeper(true, location), location);
            case COW -> field.placeObject(new Cow(true, location), location);
            case PIG -> field.placeObject(new Pig(true, location), location);
            case ZOMBIE -> field.placeObject(new Zombie(true, location), location);
            case VILLAGER -> field.placeObject(new Villager(true, location), location);
        }
    }

    /**
     * Places a specific type of plant at the given location in the field.
     * Additionally, allows for "cluster growth," where grass can spread to nearby free spaces.
     * 
     * @param type The PlantType to place (currently only Grass is implemented).
     * @param location The Location where the plant will be placed.
     */
    private void placePlant(PlantType type, Location location) {
        if (type == null || location == null) return;

        Object existing = field.getObjectAt(location);

        if (!(existing instanceof Plant)) {
            switch (type) {
                case GRASS -> {
                    field.placeObject(new Grass(location), location);

                    // Get adjacent free locations for potential grass cluster growth
                    List<Location> adjacentLocations = field.getFreeAdjacentLocations(location);
                    Collections.shuffle(adjacentLocations); // Randomize spreading

                    // Spread to up to 3 adjacent locations
                    int clusterSize = rand.nextInt(3) + 1; // 1 to 3 additional grass patches
                    for (int i = 0; i < clusterSize && i < adjacentLocations.size(); i++) {
                        Location adjacent = adjacentLocations.get(i);
                        field.placeObject(new Grass(adjacent), adjacent);
                    }
                }
            }
        }
    }

    /**
     * Outputs the current simulation statistics to the terminal.
     * Calls the Field's fieldStats() method, which provides a summary of all mob and plant populations.
     */
    private void reportStats() {
        field.fieldStats();
    }
    
    /**
     * Introduces a delay between simulation steps to control the speed of the simulation.
     * 
     * @param milliseconds The delay duration in milliseconds.
     */
    private void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // The interruption is intentionally ignored to avoid halting the simulation.
        }
    }

}
