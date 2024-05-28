package com.ziloka.NetherBedrockCracker.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Blocks;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class FindCommand {
    private static final SimpleCommandExceptionType NOT_IN_NETHER = new SimpleCommandExceptionType(new LiteralMessage("You are not in the nether."));

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("nethercracker").then(literal("find").executes(ctx -> run(ctx.getSource()))));
    }

    private static int run(FabricClientCommandSource source) throws CommandSyntaxException {
        World world = source.getWorld();
        if (!world.getRegistryKey().getValue().getPath().equals("the_nether")) {
            throw NOT_IN_NETHER.create();
        }

        ChunkPos chunkPos = source.getPlayer().getChunkPos();
        int radius = Math.max(2, source.getClient().options.getClampedViewDistance()) + 3;

        List<BlockPos> blockCandidates = new ObjectArrayList<>(1024);

        for (int chunkX = chunkPos.x - radius, maxX = chunkPos.x + radius; chunkX <= maxX; chunkX++) {
            for (int chunkZ = chunkPos.z - radius, maxZ = chunkPos.z + radius; chunkZ <= maxZ; chunkZ++) {
                Chunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk != null) addBedrockBlocks(chunk, blockCandidates);
            }
        }

        source.sendFeedback(Text.literal(String.format("Found %d bedrocks at y = 4 or y = 123", blockCandidates.size())));

        StringBuilder sb = new StringBuilder(blockCandidates.size() * 12);
        for (BlockPos block : blockCandidates) {
            sb.append(block.getX()).append(' ').append(block.getY()).append(' ').append(block.getZ()).append('\n');
        }
        String str = sb.toString();

        Text text = Texts.bracketed(
                (Text.literal("Click here to copy block info")).styled(style -> style.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.copy.click")))
                        .withInsertion(str)));

        source.sendFeedback(text);

        return Command.SINGLE_SUCCESS;
    }

    private static void addBedrockBlocks(Chunk chunk, List<BlockPos> blockCandidates) {
        ChunkSection topSection = chunk.getSection(chunk.getSectionIndex(4));
        ChunkSection bottomSection = chunk.getSection(chunk.getSectionIndex(123));
        int worldX = chunk.getPos().getStartX();
        int worldZ = chunk.getPos().getStartZ();

        // search every column for the block
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (topSection.getBlockState(x, 4, z).isOf(Blocks.BEDROCK)) {
                    blockCandidates.add(new BlockPos(worldX + x, 4, worldZ + z));
                }

                if (bottomSection.getBlockState(x, 123 & 15, z).isOf(Blocks.BEDROCK)) {
                    blockCandidates.add(new BlockPos(worldX + x, 123, worldZ + z));
                }
            }
        }
    }
}
