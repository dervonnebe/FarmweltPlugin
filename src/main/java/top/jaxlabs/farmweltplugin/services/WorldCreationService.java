package top.jaxlabs.farmweltplugin.services;

import top.jaxlabs.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;

import java.util.List;
import java.util.Random;

public class WorldCreationService {
    private final Main plugin;
    private final MultiverseCoreApi core;
    private final boolean useMultiverse;
    private final Random random = new Random();

    public WorldCreationService(Main plugin, MultiverseCoreApi core, boolean useMultiverse) {
        this.plugin = plugin;
        this.core = core;
        this.useMultiverse = useMultiverse;
    }

    public void createWorld(String worldName, World.Environment environment, 
                          WorldBorderService borderService, ChunkyIntegrationService chunkyService) {
        if (worldExists(worldName)) {
            plugin.getLogger().info("World already exists");
            return;
        }

        try {
            Long seed = selectSeed(environment);
            String generator = getGeneratorForEnvironment(environment);

            if (generator != null) {
                plugin.getLogger().info("Using generator: " + generator + " for world: " + worldName);
            }

            if (useMultiverse) {
                createWorldWithMultiverse(worldName, environment, seed, generator, borderService, chunkyService);
            } else {
                createWorldWithBukkit(worldName, environment, seed, generator, borderService, chunkyService);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating world: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Long selectSeed(World.Environment environment) {
        boolean useRotatingSeeds = plugin.getConfig().getBoolean("rotating-seeds.enabled", false);
        
        if (useRotatingSeeds) {
            List<Long> seedList = getSeedListForEnvironment(environment);
            if (seedList != null && !seedList.isEmpty()) {
                Long seed = seedList.get(random.nextInt(seedList.size()));
                plugin.getLogger().info("Using rotating seed: " + seed);
                return seed;
            }
        }
        
        return random.nextLong();
    }

    private List<Long> getSeedListForEnvironment(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> plugin.getConfig().getLongList("rotating-seeds.normal-world-seeds");
            case NETHER -> plugin.getConfig().getLongList("rotating-seeds.nether-world-seeds");
            case THE_END -> plugin.getConfig().getLongList("rotating-seeds.end-world-seeds");
            default -> null;
        };
    }

    private String getGeneratorForEnvironment(World.Environment environment) {
        String generator = switch (environment) {
            case NORMAL -> plugin.getConfig().getString("farmwelt-settings.generator");
            case NETHER -> plugin.getConfig().getString("nether-world-settings.generator");
            case THE_END -> plugin.getConfig().getString("end-world-settings.generator");
            default -> null;
        };

        return (generator != null && generator.trim().isEmpty()) ? null : generator;
    }

    private void createWorldWithMultiverse(String worldName, World.Environment environment, 
                                          Long seed, String generator,
                                          WorldBorderService borderService, 
                                          ChunkyIntegrationService chunkyService) {
        CreateWorldOptions options = CreateWorldOptions.worldName(worldName)
                .environment(environment)
                .seed(seed)
                .generateStructures(true);

        if (generator != null) {
            options.generator(generator);
        }

        core.getWorldManager()
                .createWorld(options)
                .onFailure(reason -> plugin.getLogger().warning("Failed to create world: " + worldName + " Reason: " + reason))
                .onSuccess(newWorld -> {
                    plugin.getLogger().info("World created successfully: " + worldName + " (" + environment + ")");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        borderService.setupWorldBorder(world);
                        chunkyService.startChunkyGeneration(world);
                    }
                });
    }

    private void createWorldWithBukkit(String worldName, World.Environment environment, 
                                      Long seed, String generator,
                                      WorldBorderService borderService, 
                                      ChunkyIntegrationService chunkyService) {
        WorldCreator worldCreator = new WorldCreator(worldName)
                .environment(environment)
                .type(WorldType.NORMAL)
                .generateStructures(true)
                .seed(seed);

        if (generator != null) {
            worldCreator.generator(generator);
        }

        World world = worldCreator.createWorld();
        if (world != null) {
            plugin.getLogger().info("World created successfully: " + worldName + " (" + environment + ")");
            borderService.setupWorldBorder(world);
            chunkyService.startChunkyGeneration(world);
        } else {
            plugin.getLogger().warning("Failed to create world: " + worldName);
        }
    }

    private boolean worldExists(String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            return true;
        }
        return new java.io.File(Bukkit.getWorldContainer(), worldName).exists();
    }
}
