/**
 * Enumeration representing the time of day in the simulation.
 * The simulation can switch between DAY and NIGHT cycles.
 * Each enum constant provides a formatted string representation.
 * 
 * Time of day affects the behavior of certain mobs and environmental factors
 * within the simulation, such as breeding, hunting, and visibility.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public enum TimeOfDay {

    /**
     * Represents daytime in the simulation.
     * Daytime may affect mob behavior such as increased activity for certain entities.
     */
    DAY {
        /**
         * Returns a formatted string representation of the daytime.
         * 
         * @return The string "Day" representing the time of day.
         */
        @Override
        public String toString() {
            return "Day";
        }
    },

    /**
     * Represents nighttime in the simulation.
     * Nighttime may affect mob behavior, such as increased activity for nocturnal mobs.
     */
    NIGHT {
        /**
         * Returns a formatted string representation of the nighttime.
         * 
         * @return The string "Night" representing the time of day.
         */
        @Override
        public String toString() {
            return "Night";
        }
    };
}
