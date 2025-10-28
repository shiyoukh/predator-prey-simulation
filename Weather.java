/**
 * Enumeration representing the possible weather conditions in the simulation.
 * The weather conditions can affect various aspects of the simulation, such as mob behavior,
 * plant growth, and disease spread.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public enum Weather {
CLEAR  {
        @Override
        public String toString() {
            return "Clear";
        }
    },
CLOUDY {
        @Override
        public String toString() {
            return "Cloudy";
        }
    },
RAINY {
        @Override
        public String toString() {
            return "Rainy";
        }
    }
}
