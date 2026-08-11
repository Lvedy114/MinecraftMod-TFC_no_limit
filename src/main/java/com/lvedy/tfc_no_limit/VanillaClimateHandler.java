package com.lvedy.tfc_no_limit;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.dries007.tfc.network.ChunkWatchPacket;
import net.dries007.tfc.network.UpdateClimateModelPacket;
import net.dries007.tfc.util.climate.BiomeBasedClimateModel;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.events.SelectClimateModelEvent;
import net.dries007.tfc.util.tracker.WorldTracker;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.chunkdata.LerpFloatLayer;

import com.lvedy.tfc_no_limit.climate.VanillaOverworldClimateModel;

/**
 * 负责让使用原版地形生成的世界启用群峦式气候：
 * <ul>
 *     <li>在世界加载选择气候模型时，为原版地形的主世界替换为 {@link VanillaOverworldClimateModel}</li>
 *     <li>为原版区块补生成 TFC 的区块气候数据（降雨/降雨方差/温度），使农田湿度、动物生成等直接读取
 *     {@code ChunkData} 的 TFC 机制在原版世界中也能获得与气候模型一致的数据</li>
 *     <li>在玩家开始观测区块时，将上述数据同步给客户端</li>
 * </ul>
 */
public final class VanillaClimateHandler
{
    public static void register()
    {
        final IEventBus bus = NeoForge.EVENT_BUS;
        // TFC 于 HIGHEST 优先级为群峦世界设置模型；我们在 LOWEST 兜底，仅替换仍为默认值（BiomeBased）的模型
        bus.addListener(EventPriority.LOWEST, VanillaClimateHandler::onSelectClimateModel);
        bus.addListener(VanillaClimateHandler::onChunkLoad);
        bus.addListener(VanillaClimateHandler::onChunkWatchSent);
    }

    private static void onSelectClimateModel(SelectClimateModelEvent event)
    {
        final ServerLevel level = event.level();
        if (Config.isVanillaTfcClimateEnabled()
            && level.dimension() == Level.OVERWORLD
            && !(level.getChunkSource().getGenerator() instanceof ChunkGeneratorExtension)
            && event.getModel() instanceof BiomeBasedClimateModel)
        {
            event.setModel(new VanillaOverworldClimateModel(level, Config.getVanillaTemperatureAverage(), Config.getVanillaTemperatureVariance()));
            TfcNoLimit.LOGGER.info("Enabled TFC-style climate for the vanilla-terrain overworld (average temperature {}°C, variance {}°C²)",
                Config.getVanillaTemperatureAverage(), Config.getVanillaTemperatureVariance());
        }
    }

    private static void onChunkLoad(ChunkEvent.Load event)
    {
        if (event.getLevel() instanceof ServerLevel level
            && WorldTracker.get(level).getClimateModel() instanceof VanillaOverworldClimateModel model)
        {
            final ChunkAccess chunk = event.getChunk();
            final ChunkData data = ChunkData.get(chunk);
            if (data.status() == ChunkData.Status.EMPTY)
            {
                final ClimateLayers layers = ClimateLayers.of(model, chunk.getPos());
                data.generatePartial(layers.rainfall(), layers.rainVariance(), layers.baseGroundwater(), layers.temperature(), ForestType.GRASSLAND);
                chunk.setUnsaved(true);
            }
        }
    }

    private static void onChunkWatchSent(ChunkWatchEvent.Sent event)
    {
        final ServerLevel level = event.getLevel();
        if (WorldTracker.get(level).getClimateModel() instanceof VanillaOverworldClimateModel model)
        {
            // TFC 自身只在 ChunkData.Status.FULL 时发送该包；原版世界的区块数据只会到 PARTIAL，由我们补发
            final ChunkData data = ChunkData.get(event.getChunk());
            if (data.status() == ChunkData.Status.PARTIAL)
            {
                final ClimateLayers layers = ClimateLayers.of(model, event.getPos());
                PacketDistributor.sendToPlayer(event.getPlayer(), new ChunkWatchPacket(
                    event.getPos(), layers.rainfall(), layers.rainVariance(), layers.baseGroundwater(), layers.temperature(), ForestType.GRASSLAND
                ));
            }
        }
    }

    /**
     * 配置重载时重新为所有维度选择气候模型，并重新同步给客户端，使开关与温度设置无需重启世界即可生效。
     */
    public static void refreshClimateModels(MinecraftServer server)
    {
        for (ServerLevel level : server.getAllLevels())
        {
            Climate.chooseModelForWorld(level);
            PacketDistributor.sendToPlayersInDimension(level, new UpdateClimateModelPacket(Climate.get(level)));
        }
    }

    /**
     * 一组区块角点采样的气候数据层。四个角点值的排列顺序与 {@link LerpFloatLayer} 的插值约定一致。
     */
    private record ClimateLayers(LerpFloatLayer rainfall, LerpFloatLayer rainVariance, LerpFloatLayer baseGroundwater, LerpFloatLayer temperature)
    {
        static ClimateLayers of(VanillaOverworldClimateModel model, ChunkPos pos)
        {
            final int x = pos.getMinBlockX(), z = pos.getMinBlockZ();
            return new ClimateLayers(
                new LerpFloatLayer(
                    model.getAverageRainfall(x, z),
                    model.getAverageRainfall(x, z + 16),
                    model.getAverageRainfall(x + 16, z),
                    model.getAverageRainfall(x + 16, z + 16)
                ),
                new LerpFloatLayer(
                    model.getRainVariance(x, z),
                    model.getRainVariance(x, z + 16),
                    model.getRainVariance(x + 16, z),
                    model.getRainVariance(x + 16, z + 16)
                ),
                new LerpFloatLayer(0, 0, 0, 0),
                new LerpFloatLayer(
                    model.getSeaLevelTemperature(x, z),
                    model.getSeaLevelTemperature(x, z + 16),
                    model.getSeaLevelTemperature(x + 16, z),
                    model.getSeaLevelTemperature(x + 16, z + 16)
                )
            );
        }
    }

    private VanillaClimateHandler() {}
}
