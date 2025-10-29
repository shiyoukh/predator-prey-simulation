# 🧠 Minecraft Mob Predator/Prey Simulation

A dynamic ecosystem simulation inspired by **Minecraft**’s mobs, where predators and prey interact, evolve, and adapt to their environment.

---

## 🌍 Overview

This simulation expands on the *Foxes and Rabbits* model by introducing **two predators** (Zombies, Creepers) and **three prey** (Cows, Pigs, Villagers).  
Each mob exhibits unique behaviors such as hunting, breeding, and avoiding predators, influenced by time, weather, and seasonal cycles.

Environmental factors like **day/night**, **weather**, and **seasons** dynamically affect mob survival and disease spread — creating a truly evolving ecosystem.

---

## 🎮 Features

| Feature | Description |
|----------|-------------|
| 🧟‍♂️ **Predator & Prey System** | Creepers and Zombies hunt Cows, Pigs, and Villagers. |
| 🌦️ **Dynamic Weather** | Randomly alternates between Clear, Cloudy, and Rainy, influencing mob behavior. |
| 🌗 **Day/Night Cycle** | TimeOfDay enum affects mob aggression and visibility. |
| ❄️ **Seasons** | Four seasons impact disease likelihood and grass growth. |
| 💉 **Disease System** | DiseaseHandler controls infection and spreading behavior. |
| 🌿 **Plant Growth** | Grass grows over time; prey must eat to survive. |
| 🧬 **Breeding Manager** | Unified system manages population balance and breeding rates. |
| 🖼️ **Image Provider** | Each mob is represented with its own sprite instead of color blocks. |

---

## 🧩 Project Structure

- **Mob Hierarchy:** `Mob` → `PredatorMob` / `PreyMob` → individual species classes  
- **Environmental Enums:** `TimeOfDay`, `Weather`, `Season`  
- **Management Systems:** `BreedingManager`, `DiseaseHandler`, `Field`, `SimulatorView`

The project emphasizes modularity, maintainability, and performance, using **enums**, **interfaces**, and **abstract classes** to allow future extension (new mobs, plants, or mechanics).

---

## ⚙️ Installation

### 🪟 Windows Users
1. Download the latest installer from the link below:
   
   👉 [**Download Predator-Prey Simulation Installer for Windows (.msi)**](https://github.com/shiyoukh/predator-prey-simulation/releases/download/v1.0.0/PredatorPreySimulation-1.0.0.msi)
   
   👉 [**Download Predator-Prey Simulation Installer for Mac (.dmg)**](https://github.com/shiyoukh/predator-prey-simulation/releases/download/v1.0.0/PredatorPreySimulation-MACOSX-1.0.0.dmg)
   
2. Run the `.msi` / `.dmg` file.
3. Once installed, open from **Start Menu → Predator Prey Simulation**.

### 💻 Developers
1. Clone this repo:
   ```bash
   git clone https://github.com/shiyoukh/predator-prey-simulation.git
