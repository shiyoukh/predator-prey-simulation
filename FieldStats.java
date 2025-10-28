import java.util.HashMap;
import java.util.Map;

/**
 * This class collects and provides some statistical data on the state 
 * of a field. It is flexible: it will create and maintain a counter 
 * for any class of object that is found within the field.
 * 
 * @author David J. Barnes, Michael Kölling, and Ali Alshiyoukh
 * @version 7.1
 */
public class FieldStats
{
    // Counters for each type of entity (creeper, zombie, etc.) in the simulation.
    private final Map<Class<?>, Counter> counters;
    // Whether the counters are currently up to date.
    private boolean countsValid;

    /**
     * Construct a FieldStats object.
     */
    public FieldStats()
    {
        // Set up a collection for counters for each type of mob that
        // we might find
        counters = new HashMap<>();
        countsValid = true;
    }

    /**
     * Get details of what is in the field.
     * @return A string describing what is in the field.
     */
    public String getPopulationDetails(Field field)
    {
        StringBuilder details = new StringBuilder();
        if(!countsValid) {
            generateCounts(field);
        }
        for(Class<?> key : counters.keySet()) {
            Counter info = counters.get(key);
            details.append(info.getName())
                   .append(": ")
                   .append(info.getCount())
                   .append(' ');
        }
        return details.toString();
    }
    
    /**
     * Invalidate the current set of statistics; reset all 
     * counts to zero.
     */
    public void reset()
    {
        countsValid = false;
        for(Class<?> key : counters.keySet()) {
            Counter count = counters.get(key);
            count.reset();
        }
    }

    /**
     * Increment the count for one class of mob.
     * @param mobClass The class of mob to increment.
     */
    public void incrementCount(Class<?> mobClass)
    {
        Counter count = counters.get(mobClass);
        if(count == null) {
            // We do not have a counter for this species yet.
            // Create one.
            count = new Counter(mobClass.getName());
            counters.put(mobClass, count);
        }
        count.increment();
    }

    /**
     * Indicate that an mob count has been completed.
     */
    public void countFinished()
    {
        countsValid = true;
    }

    /**
     * Determine whether the simulation should continue to run.
     * I.e., should it continue to run.
     * @return true If no species has gone extinct.
     */
    public boolean isViable(Field field)
    {
        return field.isViable();
    }

    /**
     * Generates counts of all entities currently in the field.
     * This method iterates through every cell in the field, checking for non-null entities.
     * The counts are not kept up to date dynamically but are only generated when requested.
     *
     * @param field The field to generate statistics for, including all mobs and plants.
     */
    private void generateCounts(Field field) {
        reset(); // Reset all counts before generating fresh statistics
        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                Object entity = field.getObjectAt(new Location(row, col));
                if (entity != null) {
                    incrementCount(entity.getClass()); // Increment count for the specific class of the entity
                }
            }
        }
        countsValid = true; // Mark the counts as valid until the next field update
    }

}
