/**
 * Enumeration representing the four seasons in the simulation.
 * Each season provides a formatted name through the overridden toString() method.
 * 
 * Seasons can influence various aspects of the simulation, such as breeding rates,
 * food availability, and disease spread among mobs and plants.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public enum Season {
    
    SUMMER {
        /**
         * Provides a formatted string representation of the Summer season.
         * 
         * @return The string "Summer" representing the season.
         */
        @Override
        public String toString() {
            return "Summer";
        }
    },

    AUTUMN {
        /**
         * Provides a formatted string representation of the Autumn season.
         * 
         * @return The string "Autumn" representing the season.
         */
        @Override
        public String toString() {
            return "Autumn";
        }
    },

    WINTER {
        /**
         * Provides a formatted string representation of the Winter season.
         * 
         * @return The string "Winter" representing the season.
         */
        @Override
        public String toString() {
            return "Winter";
        }
    },

    SPRING {
        /**
         * Provides a formatted string representation of the Spring season.
         * 
         * @return The string "Spring" representing the season.
         */
        @Override
        public String toString() {
            return "Spring";
        }
    }
}
