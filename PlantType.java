/**
 * Enumeration representing the different types of plants in the simulation.
 * Each enum constant overrides the toString() method to provide a formatted name.
 * 
 * Currently, the simulation supports only the "Grass" plant type.
 * Additional plant types can be added to this enum as needed.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public enum PlantType {
    
    GRASS {
        /**
         * Provides a formatted string representation of the Grass plant type.
         * 
         * @return The string "Grass" representing the plant type.
         */
        @Override
        public String toString() {
            return "Grass";
        }
    }
}
