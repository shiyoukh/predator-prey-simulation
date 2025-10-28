import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.image.BufferedImage;

/**
 * A graphical view of the simulation grid.
 * The view displays a colored rectangle for each location
 * representing its contents. It uses a default background color.
 * Colors for each type of species can be defined using the
 * setColor method.
 * 
 * This view also displays additional simulation information such as:
 * - Current step of the simulation
 * - Time of day (Day/Night)
 * - Current weather conditions
 * - Current season
 * 
 * Credit: David J. Barnes, Michael Kölling, and Ali Alshiyoukh
 * @version 7.0
 */
public class SimulatorView extends JFrame {

    // Color used for empty locations.
    private static final Color EMPTY_COLOR = Color.white;

    // Color used for unknown objects.
    private static final Color UNKNOWN_COLOR = Color.gray;

    // Prefix labels for displaying step and population information.
    private final String STEP_PREFIX = "Step: ";
    private final String POPULATION_PREFIX = "Population: ";

    // GUI components for displaying simulation data.
    private final JLabel stepLabel;
    private final JLabel population;
    private final JLabel timeLabel;
    private final JLabel weatherLabel;
    private final JLabel seasonLabel;

    // A map to store colors for different simulation participants.
    private final Map<Class<?>, Color> colors;

    // A statistics object for computing and storing simulation data.
    private final FieldStats stats;

    // The field view component that visually displays the grid.
    private final FieldView fieldView;

    /**
     * Constructs a view for the simulation with the specified grid size.
     * Initializes GUI components and sets default display settings.
     * 
     * @param height The simulation's grid height.
     * @param width The simulation's grid width.
     */
    public SimulatorView(int height, int width) {
        stats = new FieldStats();
        colors = new LinkedHashMap<>();

        // Set default colors for different mob and plant types.
        setColor(Creeper.class, Color.green);
        setColor(Zombie.class, Color.darkGray);
        setColor(Pig.class, Color.pink);
        setColor(Villager.class, Color.magenta);
        setColor(Grass.class, Color.yellow);

        // Set the window title.
        setTitle("Minecraft Mob Predator/Prey Simulation");

        // Initialize GUI labels for displaying simulation state.
        stepLabel = new JLabel(STEP_PREFIX, JLabel.CENTER);
        timeLabel = new JLabel("Time: Day", JLabel.CENTER);
        weatherLabel = new JLabel("Weather: ", JLabel.CENTER);
        seasonLabel = new JLabel("Season: ", JLabel.CENTER);
        population = new JLabel(POPULATION_PREFIX, JLabel.CENTER);

        // Get the screen size and set the window to maximize automatically.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set the window size and behavior.
        setSize(screenWidth, screenHeight);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        // Initialize the field view for displaying the simulation grid.
        fieldView = new FieldView(height, width);

        // Create a panel for the top section of the window.
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(4, 1));
        topPanel.add(stepLabel);
        topPanel.add(timeLabel);
        topPanel.add(weatherLabel);
        topPanel.add(seasonLabel);

        // Add components to the main window.
        Container contents = getContentPane();
        contents.add(topPanel, BorderLayout.NORTH);
        contents.add(fieldView, BorderLayout.CENTER);
        contents.add(population, BorderLayout.SOUTH);

        // Make the window visible.
        setVisible(true);
    }


    /**
     * Defines a color to be used for a specific class of plant or mob.
     * This method allows dynamic assignment of colors to different simulation entities.
     * 
     * @param objClass The class of the plant or mob.
     * @param color The color to be associated with this class.
     */
    public void setColor(Class<?> objClass, Color color) {
        colors.put(objClass, color);
    }

    /**
     * Retrieves the color assigned to a specific class of plant or mob.
     * If no color is defined for the class, returns the default unknown color.
     * 
     * @param objClass The class of the plant or mob.
     * @return The color associated with the class, or a default gray if undefined.
     */
    private Color getColor(Class<?> objClass) {
        Color col = colors.get(objClass);
        if (col == null) {
            return UNKNOWN_COLOR; // Default color for undefined classes
        } else {
            return col;
        }
    }

    /**
     * Displays the current status of the simulation field.
     * Updates the simulation step, time of day, weather, and season information.
     * Removes dead objects from the field and updates the graphical display.
     * 
     * @param step The current simulation step.
     * @param field The field whose status is to be displayed.
     * @param time The current time of day in the simulation.
     * @param weather The current weather condition in the simulation.
     * @param season The current season in the simulation.
     */
    public void showStatus(int step, Field field, TimeOfDay time, Weather weather, Season season) {
        if (!isVisible()) {
            setVisible(true); // Ensure the window is visible.
        }

        // Update GUI labels with the current simulation state.
        stepLabel.setText(STEP_PREFIX + step);
        timeLabel.setText("Time: " + time);
        weatherLabel.setText("Weather: " + weather);
        seasonLabel.setText("Season: " + season);

        // Reset field statistics and prepare the field view for repainting.
        stats.reset();
        fieldView.preparePaint();

        // Step 1: Identify dead objects in the field for removal.
        ArrayList<Location> toRemove = new ArrayList<>();

        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                Location location = new Location(row, col);
                Object obj = field.getObjectAt(location);

                // Add dead mobs and grass to the removal list.
                if (obj instanceof Mob mob && !mob.isAlive()) {
                    toRemove.add(location);
                } else if (obj instanceof Grass grass && !grass.isAlive()) {
                    toRemove.add(location);
                }
            }
        }

        // Step 2: Remove all dead objects from the field and clear their associated images.
        for (Location loc : toRemove) {
            Object obj = field.getObjectAt(loc);
            field.removeObject(obj);
            fieldView.clearImage(loc); // Explicitly clear the image from the view.
        }

        // Step 3: Redraw the updated field.
        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                Location location = new Location(row, col);
                Object obj = field.getObjectAt(location);

                // Draw mobs with either their image or a colored mark.
                if (obj instanceof Mob mob) {
                    if (mob instanceof ImageProvider provider) {
                        fieldView.drawImage(col, row, provider.getImage());
                    } else {
                        fieldView.drawMark(col, row, getColor(mob.getClass()));
                    }
                    stats.incrementCount(mob.getClass());
                } 
                // Draw grass with its image.
                else if (obj instanceof Grass grass) {
                    fieldView.drawImage(col, row, grass.getImage());
                    stats.incrementCount(Grass.class);
                } 
                // Draw empty spaces with the default empty color.
                else {
                    fieldView.drawMark(col, row, EMPTY_COLOR);
                }
            }
        }

        // Update the statistics and repaint the view to show changes.
        stats.countFinished();
        population.setText(POPULATION_PREFIX + stats.getPopulationDetails(field));
        fieldView.repaint();
    }


    /**
     * Determines whether the simulation should continue to run.
     * The simulation remains viable as long as there is more than one species alive.
     * This method delegates the viability check to the FieldStats object.
     * 
     * @param field The field to check for viability.
     * @return true if more than one species is alive in the simulation, false otherwise.
     */
    public boolean isViable(Field field) {
        return stats.isViable(field);
    }

        /**
     * A graphical view of a rectangular field within the simulation.
     * This nested class defines a custom Swing component for displaying the simulation grid.
     * The FieldView class handles drawing each cell of the field based on the simulation state.
     * 
     * This component supports displaying both colored marks and buffered images for simulation entities.
     * 
     * @author Ali Alshiyoukh
     * @version 1.0
     */
    private class FieldView extends JPanel {

        // Scaling factor for determining the size of each grid cell.
        private final int GRID_VIEW_SCALING_FACTOR = 18;

        // Dimensions of the grid in terms of the number of cells.
        private final int gridWidth, gridHeight;

        // Scaling factors to convert grid coordinates to pixel coordinates.
        private int xScale, yScale;

        // The size of the entire field view component.
        Dimension size;

        // Graphics context used for drawing operations.
        private Graphics g;

        // An image that holds the current state of the field.
        private Image fieldImage;

        /**
         * Constructs a new FieldView component for a given grid size.
         * 
         * @param height The number of rows in the simulation grid.
         * @param width The number of columns in the simulation grid.
         */
        public FieldView(int height, int width) {
            gridHeight = height;
            gridWidth = width;
            size = new Dimension(0, 0);
        }

        /**
         * Clears the image at a specific location in the grid.
         * This method forces the cell to be redrawn with the default empty color.
         * 
         * @param loc The location of the cell to clear.
         */
        public void clearImage(Location loc) {
            int x = loc.col();
            int y = loc.row();
            drawMark(x, y, EMPTY_COLOR); // Force clearing of the cell to an empty state.
        }

        /**
         * Draws a buffered image at the specified grid coordinates.
         * The image is scaled to fit within the grid cell size.
         * 
         * @param x The x-coordinate (column) of the cell.
         * @param y The y-coordinate (row) of the cell.
         * @param image The BufferedImage to draw in the cell.
         */
        public void drawImage(int x, int y, BufferedImage image) {
            if (g != null && image != null) {
                // Calculate the scaled dimensions of the image to fit within the cell.
                int scaledWidth = Math.max(1, xScale - 1);
                int scaledHeight = Math.max(1, yScale - 1);

                // Draw the image at the specified grid position.
                g.drawImage(image, x * xScale, y * yScale, scaledWidth, scaledHeight, null);
            }
        }



        /**
         * Provides the preferred size of the field view component.
         * This size is used by the GUI manager to determine the layout of components.
         * 
         * @return The preferred dimension of the grid view based on scaling factor.
         */
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                gridWidth * GRID_VIEW_SCALING_FACTOR, 
                gridHeight * GRID_VIEW_SCALING_FACTOR
            );
        }

        /**
         * Prepares the field view for a new round of painting.
         * This method recalculates scaling factors if the component size has changed.
         * It also repositions the grid to remain centered within the available space.
         */
        public void preparePaint() {
            Dimension newSize = getSize();

            // Check if the component size has changed since the last paint cycle.
            if (!size.equals(newSize)) {
                size = newSize;

                // Create a new image buffer for the field's graphical representation.
                fieldImage = createImage(size.width, size.height);
                g = fieldImage.getGraphics();

                // Compute scaling factors to adapt the grid size to the screen size.
                int xScaleFactor = size.width / gridWidth;
                int yScaleFactor = size.height / gridHeight;

                // Maintain the aspect ratio by using the smaller scaling factor.
                xScale = yScale = Math.max(Math.min(xScaleFactor, yScaleFactor), 1);

                // Calculate the offset needed to center the grid in the window.
                int totalWidth = gridWidth * xScale;
                int totalHeight = gridHeight * yScale;
                int xOffset = (size.width - totalWidth) / 2;
                int yOffset = (size.height - totalHeight) / 2;

                // Shift the graphics context to center the grid.
                g.translate(xOffset, yOffset);
            }
        }
        
        /**
         * Paints a grid cell at the specified location with the given color.
         * This method fills a rectangle representing a grid cell on the simulation grid.
         * 
         * @param x The x-coordinate (column) of the cell.
         * @param y The y-coordinate (row) of the cell.
         * @param color The color to fill the grid cell with.
         */
        public void drawMark(int x, int y, Color color) {
            g.setColor(color);
            // Draw a filled rectangle representing a grid cell with a slight border for visibility.
            g.fillRect(x * xScale, y * yScale, xScale - 1, yScale - 1);
        }

        /**
         * Paints the entire field view component.
         * This method is called automatically when the component needs to be redrawn.
         * It copies the internal image buffer to the screen.
         * 
         * @param g The graphics context to use for painting.
         */
        @Override
        public void paintComponent(Graphics g) {
            if (fieldImage != null) {
                Dimension currentSize = getSize();

                if (size.equals(currentSize)) {
                    // If the size hasn't changed, draw the image at its original scale.
                    g.drawImage(fieldImage, 0, 0, null);
                } else {
                    // If the size has changed, rescale the image to fit the new size.
                    g.drawImage(fieldImage, 0, 0, currentSize.width, currentSize.height, null);
                }
            }
        }

    }
}
