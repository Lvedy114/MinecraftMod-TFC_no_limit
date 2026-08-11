package com.lvedy.tfc_no_limit.climate;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.ClimateModelType;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;

/**
 * 用于原版地形世界的群峦式气候模型。
 * <p>
 * 原版世界没有 TFC 的区块气候数据（{@code ChunkData} 由 TFC 区块生成器填充），因此本模型将
 * {@link OverworldClimateModel} 中所有依赖 {@code ChunkData} 的查询，替换为基于世界种子噪声的实现：
 * <ul>
 *     <li>年均温 = 可配置平均值 + 纬度三角波（振幅由可配置方差推出）+ 局部噪声，与 TFC 区域气候同构</li>
 *     <li>年降雨 = 250mm ± 经度三角波 + 局部噪声，夹在 [0, 500]，与 TFC 降雨噪声一致</li>
 *     <li>降雨方差（雨季分布）= 局部噪声，范围 [-1, 1]</li>
 * </ul>
 * 其余即时温度/降雨、降雨事件、雷暴、风、雾等逻辑全部由父类接管，与群峦世界行为一致。
 * <p>
 * 模型通过 {@link TNLClimateModels#VANILLA_OVERWORLD} 的类型编解码器同步到客户端，
 * 客户端依据相同的种子与配置值重建出完全一致的噪声场。
 */
public class VanillaOverworldClimateModel extends OverworldClimateModel
{
    public static final StreamCodec<ByteBuf, VanillaOverworldClimateModel> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, c -> c.climateSeed,
        ByteBufCodecs.FLOAT, c -> c.averageTemperature,
        ByteBufCodecs.FLOAT, c -> c.temperatureVariance,
        VanillaOverworldClimateModel::new
    );

    // TFC 世界生成默认的温帯/降雨带间距（20km），原版世界没有该世界设置，沿用默认值
    private static final float TEMPERATURE_SCALE = 20_000f;
    private static final float RAINFALL_SCALE = 20_000f;

    // TFC 区域气候噪声以 128 格（1 grid）为采样单位，换算到方块坐标系
    private static final float GRID_WIDTH_IN_BLOCK = 128f;

    // 与 TFC 相同的降雨带均值与振幅（0 ~ 500mm）
    private static final float RAINFALL_MIDPOINT = 250f;
    private static final float RAINFALL_AMPLITUDE = 250f;

    // TFC 中局部温度噪声振幅（±3°C）与纬度振幅（±25°C）的比例
    private static final float LOCAL_TEMPERATURE_NOISE_FACTOR = 3f / 25f;

    private static final long TEMPERATURE_NOISE_SALT = 5941203928471023L;
    private static final long RAINFALL_NOISE_SALT = 4912309485102938L;
    private static final long RAIN_VARIANCE_NOISE_SALT = 7720394857612345L;

    // 与世界种子相同的加盐方式，保证同一世界的雨期规律与 TFC 世界一致
    private static final long CLIMATE_SEED_SALT = 719283741234L;

    private final float averageTemperature;
    private final float temperatureVariance;
    private final float latitudeAmplitude;

    private final Noise2D temperatureNoise;
    private final Noise2D rainfallNoise;
    private final Noise2D rainVarianceNoise;

    public VanillaOverworldClimateModel(ServerLevel level, float averageTemperature, float temperatureVariance)
    {
        this(LinearCongruentialGenerator.next(level.getSeed(), CLIMATE_SEED_SALT), averageTemperature, temperatureVariance);
    }

    protected VanillaOverworldClimateModel(long climateSeed, float averageTemperature, float temperatureVariance)
    {
        super(climateSeed, TEMPERATURE_SCALE);
        this.averageTemperature = averageTemperature;
        this.temperatureVariance = temperatureVariance;
        // 三角波在 [-A, A] 上的方差为 A²/3，反解出纬度振幅 A = sqrt(3·variance)
        this.latitudeAmplitude = (float) Math.sqrt(3.0 * Math.max(temperatureVariance, 0));

        this.temperatureNoise = new OpenSimplex2D(LinearCongruentialGenerator.next(climateSeed, TEMPERATURE_NOISE_SALT))
            .octaves(2)
            .spread(0.15f / GRID_WIDTH_IN_BLOCK)
            .scaled(-1f, 1f);
        this.rainfallNoise = new OpenSimplex2D(LinearCongruentialGenerator.next(climateSeed, RAINFALL_NOISE_SALT))
            .octaves(2)
            .spread(0.15f / GRID_WIDTH_IN_BLOCK)
            .scaled(-80f, 40f);
        this.rainVarianceNoise = new OpenSimplex2D(LinearCongruentialGenerator.next(climateSeed, RAIN_VARIANCE_NOISE_SALT))
            .octaves(2)
            .spread(0.1f / GRID_WIDTH_IN_BLOCK)
            .scaled(-1f, 1f);
    }

    @Override
    public ClimateModelType<?> type()
    {
        return TNLClimateModels.VANILLA_OVERWORLD.get();
    }

    /**
     * 海平面年均温。可配置的平均值 + 纬度三角波（方差可配置）+ 局部噪声。
     */
    public float getSeaLevelTemperature(int x, int z)
    {
        final float latitude = triangle(1f / (2f * TEMPERATURE_SCALE), z);
        return averageTemperature
            + latitudeAmplitude * latitude
            + LOCAL_TEMPERATURE_NOISE_FACTOR * latitudeAmplitude * (float) temperatureNoise.noise(x, z);
    }

    /**
     * 年均降雨量（mm/年），与 TFC 同构：250mm 均值 + 经度三角波 + 局部噪声，夹在 [0, 500]。
     */
    public float getAverageRainfall(int x, int z)
    {
        final float longitude = triangle(1f / (2f * RAINFALL_SCALE), x);
        return Mth.clamp(
            RAINFALL_MIDPOINT + RAINFALL_AMPLITUDE * longitude + (float) rainfallNoise.noise(x, z),
            MIN_AVERAGE_RAINFALL, MAX_AVERAGE_RAINFALL
        );
    }

    /**
     * 年降雨方差，范围 [-1, 1]。正值为夏季多雨，负值为冬季多雨。
     */
    public float getRainVariance(int x, int z)
    {
        return Mth.clamp(1.75f * (float) rainVarianceNoise.noise(x, z), -1f, 1f);
    }

    @Override
    public float getAverageTemperature(LevelReader level, BlockPos pos)
    {
        return Helpers.adjustAverageTemperatureByElevation(pos.getY(), getSeaLevelTemperature(pos.getX(), pos.getZ()), SEA_LEVEL);
    }

    @Override
    public float getInstantTemperature(LevelReader level, BlockPos pos, long calendarTicks, int daysInMonth)
    {
        // 与父类逻辑一致，仅将 ChunkData 海平面温度替换为噪声温度
        final Month currentMonth = ICalendar.getMonthOfYear(calendarTicks, daysInMonth);
        final float delta = ICalendar.getFractionOfMonth(calendarTicks, daysInMonth);
        final float monthFactor = Mth.lerp(delta, currentMonth.getTemperatureModifier(), currentMonth.next().getTemperatureModifier());

        final float monthTemperature = calculateMonthlyTemperature(pos.getZ(), monthFactor);
        final float dailyTemperature = calculateDailyTemperature(calendarTicks, daysInMonth, pos.getZ());

        return adjustTemperatureByElevation(pos.getY(), getSeaLevelTemperature(pos.getX(), pos.getZ()), monthTemperature, dailyTemperature);
    }

    @Override
    public float getAverageRainfall(LevelReader level, BlockPos pos)
    {
        return getAverageRainfall(pos.getX(), pos.getZ());
    }

    @Override
    public float getRainfallVariance(LevelReader level, BlockPos pos)
    {
        return getRainVariance(pos.getX(), pos.getZ());
    }

    @Override
    public float getInstantRainfall(LevelReader level, BlockPos pos, long calendarTicks, int daysInMonth)
    {
        // 与父类逻辑一致，仅将 ChunkData 数据替换为噪声数据
        final float rainVariance = getRainVariance(pos.getX(), pos.getZ());
        final float rainAverage = getAverageRainfall(pos.getX(), pos.getZ());
        final float fractionOfYear = ICalendar.getFractionOfYear(calendarTicks, daysInMonth);

        return rainVariance == 0 ? rainAverage : Helpers.triangle(rainVariance * rainAverage, rainAverage, 1f, fractionOfYear + 0.75f);
    }

    @Override
    public float getBaseGroundwater(LevelReader level, BlockPos pos)
    {
        // 原版地形没有 TFC 的河流分区数据，不提供地下水加成
        return 0;
    }

    @Override
    public float getAverageGroundwater(LevelReader level, BlockPos pos)
    {
        return getAverageRainfall(level, pos);
    }

    @Override
    public float getInstantGroundwater(LevelReader level, BlockPos pos, long calendarTicks, int daysInMonth)
    {
        return getInstantRainfall(level, pos, calendarTicks, daysInMonth);
    }

    /**
     * 与 TFC 区域生成器使用的三角波相同，输出 [-1, 1]，周期为 {@code 1 / frequency}。
     */
    private static float triangle(float frequency, float value)
    {
        return Math.abs(4f * frequency * value + 1f - 4f * Mth.floor(frequency * value + 0.75f)) - 1f;
    }
}
