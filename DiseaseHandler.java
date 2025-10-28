import java.util.List;
import java.util.Random;

/**
 * The DiseaseHandler interface provides methods for handling disease states
 * and infection spreading among mobs within a field.
 * 
 * Classes implementing this interface can:
 * - Define their own disease rate and spread rate.
 * - Mark themselves as diseased or healthy.
 * - Attempt to infect nearby mobs of the same type.
 * 
 * This interface also includes a default method for managing disease infection behavior.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public interface DiseaseHandler {

    // Shared random number generator used for disease calculations.
    Random rand = new Random();

    /**
     * Returns the base rate at which the mob can become diseased.
     * The disease rate is a probability value between 0.0 and 1.0.
     * 
     * @return The probability that this mob will contract a disease.
     */
    double getDiseaseRate();

    /**
     * Returns the rate at which the mob can spread disease to other nearby mobs.
     * The spread rate is a probability value between 0.0 and 1.0.
     * 
     * @return The probability of spreading the disease to adjacent mobs.
     */
    double getDiseaseSpreadRate();

    /**
     * Sets the disease status of the mob.
     * 
     * @param diseased true if the mob should be marked as diseased, false otherwise.
     */
    void setDiseased(boolean diseased);

    /**
     * Checks whether the mob is currently diseased.
     * 
     * @return true if the mob is diseased, false otherwise.
     */
    boolean isDiseased();

    /**
     * Attempts to infect the mob itself and spread the disease to adjacent mobs.
     * If it is winter, the base disease rate is doubled.
     * If the mob becomes diseased, it will attempt to infect adjacent mobs of the same type.
     * 
     * @param self The mob implementing this interface.
     * @param currentField The current field containing all mobs and their positions.
     */
    default void tryToInfect(Mob self, Field currentField) {
        double diseaseRate = getDiseaseRate();

        // Increase disease rate during winter.
        if (currentField.getSeason().equals(Season.WINTER)) {
            diseaseRate *= 2;
        }

        // Check if this mob becomes diseased based on the disease rate.
        if (rand.nextDouble() < diseaseRate) {
            setDiseased(true);
        }

        // If not diseased, do not attempt to spread the disease.
        if (!isDiseased()) return;

        // Get adjacent locations to potentially spread the disease.
        List<Location> adjacentLocations = currentField.getAdjacentLocations(self.getLocation());

        // Iterate through adjacent locations to spread the disease.
        for (Location loc : adjacentLocations) {
            Object obj = currentField.getObjectAt(loc);

            // Check if the object is a mob of the same type that can handle disease.
            if (obj instanceof DiseaseHandler handler && self.getClass().equals(handler.getClass())) {
                
                // Spread the disease to healthy adjacent mobs based on the spread rate.
                if (!handler.isDiseased() && rand.nextDouble() < getDiseaseSpreadRate()) {
                    handler.setDiseased(true);
                }
            }
        }
    }
}
