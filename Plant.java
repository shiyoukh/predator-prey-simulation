/**
 * Abstract Plant class that provides the base functionality for all plant types in the simulation.
 * Plants can grow, die, and interact with the field environment.
 * Each specific plant type must implement its own growth mechanics and define its plant type.
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public abstract class Plant {

    // The current location of the plant within the field.
    protected Location location;
    // Indicates whether the plant is currently alive.
    protected boolean alive = true;

    /**
     * Constructs a new Plant at the specified location.
     * 
     * @param location The initial location of the plant within the field.
     */
    public Plant(Location location) {
        this.location = location;
    }

    /**
     * Checks whether the plant is still alive.
     * 
     * @return true if the plant is alive, false otherwise.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Marks the plant as dead.
     * Once dead, the plant's location is cleared and it is removed from the simulation.
     */
    public void setDead() {
        alive = false;
        location = null;
    }

    /**
     * Returns the current location of the plant within the field.
     * 
     * @return The location of the plant.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Abstract method to define the growth behavior of the plant.
     * Specific growth mechanics must be implemented by subclasses.
     * 
     * @param currentField The current state of the field.
     * @param nextField The field representing the next simulation state.
     */
    public abstract void grow(Field currentField, Field nextField);

    /**
     * Returns the specific type of plant.
     * Must be implemented by subclasses to provide the appropriate PlantType.
     * 
     * @return The PlantType of the plant.
     */
    public abstract PlantType getPlantType();
}
