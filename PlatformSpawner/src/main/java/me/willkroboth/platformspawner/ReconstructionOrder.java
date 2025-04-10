package me.willkroboth.platformspawner;

import org.bukkit.World;
import org.bukkit.block.BlockState;

import java.util.Map;

public record ReconstructionOrder(Map<IntegerLocation, BlockState> previousBlocks) {
}
