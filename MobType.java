/**
 * Enumeration representing the different types of mobs in the simulation.
 * Each enum constant overrides the toString() method to provide a formatted name.
 * 
 * Mobs include standard entities like Cows and Pigs, as well as Minecraft-inspired mobs
 * such as Zombies, Creepers, and Villagers.
 * 
 * Credit: Mojang (inspired by Minecraft mechanics)
 * 
 * @author Ali Alshiyoukh
 * @version 1.0
 */
public enum MobType {
    
    COW {
        @Override
        public String toString() {
            return "Cow";
        }
    },

    PIG {
        @Override
        public String toString() {
            return "Pig";
        }
    },

    ZOMBIE {
        @Override
        public String toString() {
            return "Zombie";
        }
    },

    CREEPER {
        @Override
        public String toString() {
            return "Creeper";
        }
    },

    VILLAGER {
        @Override
        public String toString() {
            return "Villager";
        }
    }
}
