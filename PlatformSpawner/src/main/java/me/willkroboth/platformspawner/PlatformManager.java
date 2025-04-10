package me.willkroboth.platformspawner;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.NativeCommandExecutor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlatformManager {
    private final PlatformSpawner plugin;
    private final Map<String, Shape> shapes;
    private final Set<ReconstructionOrder> pendingReconstructions = new HashSet<>();
    private final Map<IntegerLocation, ReconstructionOrder> locationControllers = new HashMap<>();

    public PlatformManager(PlatformSpawner plugin, Map<String, Shape> shapes) {
        this.plugin = plugin;
        this.shapes = shapes;
    }

    public void registerCommands() {
        // spawnplatform single <shape> <timeout>
        // spawnplatform chain <count> <interval> <yOffset> <shape> <timeout>
        CommandTree base = new CommandTree("spawnplatform").withPermission("platformspawner.command");

        Argument<?> singleBranch;
        base.then((singleBranch = new LiteralArgument("single")));

        Argument<?> chainBranch;
        base.then(
                new LiteralArgument("chain").then(
                        new IntegerArgument("count", 1).then(
                                new FloatArgument("interval").then(
                                        (chainBranch = new IntegerArgument("offset"))
                                )
                        )
                )
        );

        for (String name : shapes.keySet()) {
            Shape shape = shapes.get(name);

            NativeCommandExecutor singlePlatformExecutor = getSinglePlatformExecutor(shape);
            NativeCommandExecutor chainPlatformExecutor = getChainPlatformExecutor(shape);

            LiteralArgument singleShapeBranch;
            singleBranch.then((singleShapeBranch = new LiteralArgument(name)));

            LiteralArgument chainShapeBranch;
            chainBranch.then((chainShapeBranch = new LiteralArgument(name)));


            if (shape.defaultTimeout().isPresent()) {
                // If a default timeout is available, then that argument doesn't have to be included
                singleShapeBranch.executesNative(singlePlatformExecutor);
                chainShapeBranch.executesNative(chainPlatformExecutor);
            }
            singleShapeBranch.then(new FloatArgument("timeout").executesNative(singlePlatformExecutor));
            chainShapeBranch.then(new FloatArgument("timeout").executesNative(chainPlatformExecutor));
        }

        base.register();
    }

    private NativeCommandExecutor getSinglePlatformExecutor(Shape shape) {
        // spawnplatform single <shape> <timeout>
        return (sender, args) -> {
            Location location = sender.getLocation();
            if(location.getWorld() == null) throw CommandAPI.failWithString("No world defined");

            // shape.defaultTimeout().get() is safe because either the timeout argument is given, or defaultTimeout is present
            //  The branch without timeout is only executable if the defaultTimeout is present
            float timeout = args.<Float>getOptionalUnchecked("timeout").orElseGet(() -> shape.defaultTimeout().get());

            createTemporaryPlatform(shape, location, timeout);
        };
    }

    private NativeCommandExecutor getChainPlatformExecutor(Shape shape) {
        // spawnplatform chain <count> <interval> <yOffset> <shape> <timeout>
        return (sender, args) -> {
            CommandSender target = sender.getCallee();
            if(!(target instanceof Entity entity)) throw CommandAPI.failWithString("Command must be run on an entity!");

            // NPE when unboxing will not happen, CommandAPI ensures these required arguments are not null
            int count = args.getUnchecked("count");
            float interval = args.getUnchecked("interval");
            int yOffset = args.getUnchecked("offset");

            // shape.defaultTimeout().get() is safe because either the timeout argument is given, or defaultTimeout is present
            //  The branch without timeout is only executable if the defaultTimeout is present
            float timeout = args.<Float>getOptionalUnchecked("timeout").orElseGet(() -> shape.defaultTimeout().get());

            new BukkitRunnable() {
                int remaining = count;
                @Override
                public void run() {
                    // Create platform
                    createTemporaryPlatform(shape, entity.getLocation().add(0, yOffset, 0), timeout);
                    remaining--;
                    if(remaining <= 0) this.cancel();
                }
                // First platform comes immediately, so delay 0
                // Multiply seconds by 20 to get ticks
            }.runTaskTimer(this.plugin, 0, (long) (interval * 20));
        };
    }

    public void createTemporaryPlatform(Shape shape, Location location, float timeout) {
        // Build platform
        ReconstructionOrder reconstruction = buildLayout(shape.layout(), location);
        pendingReconstructions.add(reconstruction);

        // Wait for delay
        new BukkitRunnable() {
            @Override
            public void run() {
                // Destroy platform and replace with original blocks
                reconstruct(reconstruction);
                pendingReconstructions.remove(reconstruction);
            }
            // Multiply seconds by 20 to get ticks
        }.runTaskLater(this.plugin, (long) (timeout * 20));
    }

    private ReconstructionOrder buildLayout(Material[][] layout, Location location) {
        int height = layout.length;
        int width = layout[0].length;
        Map<IntegerLocation, BlockState> locations = new HashMap<>();

        World world = location.getWorld();
        assert world != null;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Material material = layout[y][x];
                // If null, we don't need to worry about this block
                if(material == null) continue;

                // Find block
                Location blockLocation = getLocation(location, width, height, x, y);
                IntegerLocation integerLocation = IntegerLocation.fromLocation(blockLocation);
                Block block = world.getBlockAt(blockLocation);

                // Save old block data
                BlockState previousBlock;
                ReconstructionOrder currentController = locationControllers.get(integerLocation);
                if(currentController != null) {
                    // Steal previous block from other platform
                    previousBlock = currentController.previousBlocks().remove(integerLocation);
                } else {
                    // No other platforms are controlling this block, we're okay to take it
                    previousBlock = block.getState();
                }
                locations.put(integerLocation, previousBlock);

                // Create new block
                block.setType(material);
            }
        }

        ReconstructionOrder reconstruction = new ReconstructionOrder(locations);
        for (IntegerLocation blockLocation : locations.keySet()) {
            locationControllers.put(blockLocation, reconstruction);
        }
        return reconstruction;
    }

    private Location getLocation(Location origin, int width, int height, int x, int y) {
        return origin.clone().add(x - (double) width/2 + 0.5, 0, y - (double) height /2 + 0.5);
    }

    private void reconstruct(ReconstructionOrder reconstruction) {
        Map<IntegerLocation, BlockState> previousBlocks = reconstruction.previousBlocks();

        for (Map.Entry<IntegerLocation, BlockState> entry : previousBlocks.entrySet()) {
            // Reset block
            entry.getValue().update(true);

            // Release control
            locationControllers.remove(entry.getKey());
        }
    }

    public void clearPlatforms() {
        for (ReconstructionOrder reconstruction : pendingReconstructions) {
            reconstruct(reconstruction);
        }
    }
}
