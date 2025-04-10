package me.willkroboth.platformspawner;

import me.willkroboth.platformspawner.exceptions.MaterialLoadException;
import me.willkroboth.platformspawner.exceptions.ShapeLoadException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public record Shape(Material[][] layout, Optional<Float> defaultTimeout) {
    // Loading shapes from the config file
    public static Map<String, Shape> loadShapes(PlatformSpawner plugin, ConfigurationSection shapesConfig) {
        Map<String, Shape> shapes = new HashMap<>();

        Set<String> names = shapesConfig.getKeys(false);
        for (String name : names) {
            ConfigurationSection shapeData = shapesConfig.getConfigurationSection(name);
            assert shapeData != null;

            try {
                Material[][] layout = loadLayout(plugin, name, shapeData);

                Optional<Float> defaultTimeout = loadTimeout(plugin, name, shapeData);

                shapes.put(name, new Shape(layout, defaultTimeout));
            } catch (ShapeLoadException loadException) {
                plugin.logError(
                        "Shape \"" + name + "\" cannot be loaded",
                        loadException.getMessage(),
                        "Skipping this shape"
                );
            }
        }

        return shapes;
    }

    private static Material[][] loadLayout(PlatformSpawner plugin, String shapeName, ConfigurationSection shapeData) throws ShapeLoadException {
        List<String> layoutData = shapeData.getStringList("layout");
        if (layoutData.size() == 0) throw new ShapeLoadException("Missing 'layout' data");

        ConfigurationSection materialData = shapeData.getConfigurationSection("materials");
        if (materialData == null) throw new ShapeLoadException("Missing 'materials' data");

        Map<Character, Material> materials = loadMaterials(plugin, shapeName, materialData);
        if(materials.size() == 0) throw new ShapeLoadException("No valid materials found");

        int height = layoutData.size();
        int width = 0;
        for (String layer : layoutData) {
            int newWidth = layer.length();
            if (newWidth > width) width = newWidth;
        }

        Material[][] layout = new Material[height][width];
        for (int y = 0; y < layoutData.size(); y++) {
            String layer = layoutData.get(y);
            for (int x = 0; x < layer.length(); x++) {
                char key = layer.charAt(x);
                if(!materials.containsKey(key)) throw new ShapeLoadException("Unknown material key \"" + key + "\" in layout");

                layout[y][x] = materials.get(key);
            }
        }

        return layout;
    }

    private static Map<Character, Material> loadMaterials(PlatformSpawner plugin, String shapeName, ConfigurationSection materialData) {
        Map<Character, Material> materials = new HashMap<>();
        for(String materialKey : materialData.getKeys(false)) {
            try {
                char key = getMaterialKey(materialKey);

                String material = materialData.getString(materialKey);
                assert material != null;
                Material materialType = getMaterialType(material);

                materials.put(key, materialType);
            } catch (MaterialLoadException loadException) {
                plugin.logError(
                        "Shape \"" + shapeName + "\" was given an invalid material \"" + materialKey + "\"",
                        loadException.getMessage(),
                        "Skipping this material"
                );
            }
        }
        return materials;
    }

    private static char getMaterialKey(String rawKey) throws MaterialLoadException {
        if (rawKey.length() != 1) throw new MaterialLoadException("Material key can only be 1 character long");
        return rawKey.charAt(0);
    }

    private static Material getMaterialType(String material) throws MaterialLoadException {
        Material materialType;
        if (material.equalsIgnoreCase("none")) {
            materialType = null;
        } else {
            materialType = Material.matchMaterial(material);
            if (materialType == null)
                throw new MaterialLoadException("Material type \"" + material + "\" could not be found");
        }
        return materialType;
    }

    private static Optional<Float> loadTimeout(PlatformSpawner plugin, String shapeName, ConfigurationSection shapeData) {
        String value = shapeData.getString("defaultTimeout");
        if(value == null) return Optional.empty();

        float time;
        try {
            time = Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            plugin.logWarning(
                    "Shape \"" + shapeName + "\" was given an invalid defaultTimeout \"" + value + "\"",
                    "Given value is not a number",
                    "No default timeout will be available"
            );
            return Optional.empty();
        }

        if(time < 0) {
            plugin.logWarning(
                    "Shape \"" + shapeName + "\" was given an invalid defaultTimeout \"" + time + "\"",
                    "Given time cannot be negative",
                    "No default timeout will be available"
            );
            return Optional.empty();
        }

        return Optional.of(time);
    }
}
