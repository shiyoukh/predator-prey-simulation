import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

/**
 * Manages breeding behavior of mobs within the simulation.
 * Controls breeding probabilities and thresholds for population control.
 *
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public class BreedingManager {

    // Maps to store carrying capacities, repopulation thresholds, and breeding probabilities for mobs.
    private static final Map<Class<?>, Boolean> mobThresholds = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Integer> carryingCapacities = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Integer> repopulationThresholds = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Double> baseBreedingProbabilities = new ConcurrentHashMap<>();

    // Shared random instance for breeding probability calculations.
    private static Random rand;

    static {
        // Set default population limits for various mob types.
        setPopulationLimits(Cow.class, 1700, 400, 0.27);
        setPopulationLimits(Pig.class, 1700, 400, 0.27);
        setPopulationLimits(Villager.class, 1700, 400, 0.27);
        setPopulationLimits(Zombie.class, 500, 300, 0.15);
        setPopulationLimits(Creeper.class, 500, 300, 0.15);
    }

    /**
     * Initializes the breeding manager with a shared random instance.
     *
     * @param sharedRandom The shared random instance to use.
     */
    public static void initialize(Random sharedRandom) {
        rand = sharedRandom;
    }

    /**
     * Sets the population limits for a specific mob class.
     *
     * @param mobClass              The class of the mob.
     * @param carryingCapacity      The maximum population before breeding stops.
     * @param repopulationThreshold The population threshold to allow repopulation.
     * @param baseBreedingProbability The base probability of breeding.
     */
    public static void setPopulationLimits(Class<?> mobClass, int carryingCapacity, int repopulationThreshold, double baseBreedingProbability) {
        carryingCapacities.put(mobClass, carryingCapacity);
        repopulationThresholds.put(mobClass, repopulationThreshold);
        baseBreedingProbabilities.put(mobClass, baseBreedingProbability);
        mobThresholds.put(mobClass, false); // Default: breeding is allowed
    }

    /**
     * Updates the breeding permissions for mobs based on the current field population.
     *
     * @param field The simulation field containing the mobs.
     */
    public static void updateThresholds(Field field) {
        for (Class<?> mobClass : carryingCapacities.keySet()) {
            int carryingCapacity = carryingCapacities.get(mobClass);
            int repopulationThreshold = repopulationThresholds.get(mobClass);
            int currentPopulation = field.getPopulation(mobClass);

            boolean shouldStopBreeding = (currentPopulation >= carryingCapacity);
            boolean shouldAllowBreeding = (currentPopulation <= repopulationThreshold);

            // Only update if the state changes to avoid unnecessary writes
            if (shouldStopBreeding != mobThresholds.get(mobClass)) {
                mobThresholds.put(mobClass, shouldStopBreeding);
            } else if (shouldAllowBreeding) {
                mobThresholds.put(mobClass, false);
            }
        }
    }

    /**
     * Calculates the breeding probability for a specific mob class.
     *
     * @param mobClass The class of the mob.
     * @param field    The simulation field.
     * @return The probability of breeding for the mob.
     */
    public static double getBreedingProbability(Class<?> mobClass, Field field) {
        Integer carryingCapacity = carryingCapacities.get(mobClass);
        if (carryingCapacity == null) return 0.0;

        int currentPopulation = field.getPopulation(mobClass);
        int repopulationThreshold = repopulationThresholds.get(mobClass);
        double baseProbability = baseBreedingProbabilities.get(mobClass);

        if (currentPopulation >= carryingCapacity) return 0.0;
        if (currentPopulation > repopulationThreshold) return baseProbability * 0.2;

        // Adjust breeding chance based on the current population percentage
        return baseProbability * (1 - (double) currentPopulation / carryingCapacity);
    }

    /**
     * Determines if a specific mob can breed based on the current thresholds and probability.
     *
     * @param mobClass The class of the mob.
     * @param field    The simulation field.
     * @return true if the mob can breed, false otherwise.
     */
    public static boolean canBreed(Class<?> mobClass, Field field) {
        return !mobThresholds.getOrDefault(mobClass, false) &&
                rand.nextDouble() <= getBreedingProbability(mobClass, field);
    }
}
