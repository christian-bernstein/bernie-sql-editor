package net;

import de.christianbernstein.bernie.sdk.discovery.websocket.*;
import de.christianbernstein.bernie.sdk.discovery.websocket.packets.Primitives;
import de.christianbernstein.bernie.sdk.discovery.websocket.server.ServerConfiguration;
import de.christianbernstein.bernie.sdk.discovery.websocket.server.StandaloneSocketServer;

import com.sun.management.OperatingSystemMXBean;
import lombok.*;
import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.util.ArrayUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class Main {

    @Getter
    @Setter
    @PacketMeta(dataID = "SysVolumeChangePacketData", protocol = "base")
    @AllArgsConstructor
    public static class SysVolumeChangePacketData extends PacketData {

        private boolean toggleMute;

        private boolean mute;

        private byte soundOffset;

    }

    @PacketMeta(dataID = "PerformanceStatsPacketData", protocol = "base")
    @AllArgsConstructor
    @Builder
    public static class PerformanceStatsPacketData extends PacketData {

        private double cpuLoad;

        private double physicalRam;

        private double freeRam;

        private double gpuPowerDraw;

        private double gpuPowerLimit;

        private double gpuPowerMaxLimit;

        private double gpuPowerMinLimit;

        private double gpuFanSpeedPercentage;

        private double gpuUtilization;

        private double outputAudioVolume;

        private double outputAudioMute;

    }

    public static Protocol baseProtocol = Protocol.builder()
            .id("main")
            .attachment(SessionProtocolData.builder()
                    .protocolID("main")
                    .isBaseProtocol(false)
                    .build()
                    .loadFromClass(TaskManagerProtocol.class))
            .build();

    public static String cmd(String... commands) {
       try {
           final ProcessBuilder pb = new ProcessBuilder(Stream.of(new String[]{"cmd.exe", "/c"}, commands).flatMap(Stream::of).toArray(String[]::new));
           pb.redirectErrorStream(true);
           final Process process = pb.start();
           BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
           return in.lines().collect(Collectors.joining(" "));
       } catch (final Exception e) {
           e.printStackTrace();
           return null;
       }
    }

    public static void main(String[] args) throws InterruptedException {
        final List<Consumer<PerformanceStatsPacketData>> statConsumer = new ArrayList<>();

        final StandaloneSocketServer server = new StandaloneSocketServer(ServerConfiguration.builder()
                .defaultProtocol(Main.baseProtocol)
                .setDefaultProtocolOnPostEstablish(true)
                .masterLogSwitch(true)
                .address(new InetSocketAddress(25565)).build());

        server.onPostEstablish((event, doc) -> {
            statConsumer.add(packet -> event.session().push(packet));
        });

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                final double cpuLoad = Double.parseDouble(Objects.requireNonNull(cmd("wmic cpu get loadpercentage")).split(" +")[1]);
                final double physicalRam = Double.parseDouble(Objects.requireNonNull(cmd("wmic ComputerSystem get TotalPhysicalMemory")).split(" +")[1]);
                final double freeRam = Double.parseDouble(Objects.requireNonNull(cmd("wmic OS get FreePhysicalMemory")).split(" +")[1]) * 1000;
                final double gpuPowerDraw = Double.parseDouble(Objects.requireNonNull(cmd("nvidia-smi --format=csv --query-gpu=power.draw")).split(" +")[2]);
                final double gpuPowerLimit = Double.parseDouble(Objects.requireNonNull(cmd("nvidia-smi --format=csv --query-gpu=power.limit")).split(" +")[2]);
                final double gpuPowerMaxLimit = Double.parseDouble(Objects.requireNonNull(cmd("nvidia-smi --format=csv --query-gpu=power.max_limit")).split(" +")[2]);
                final double gpuPowerMinLimit = Double.parseDouble(Objects.requireNonNull(cmd("nvidia-smi --format=csv --query-gpu=power.min_limit")).split(" +")[2]);
                final double gpuFanSpeedPercentage = Double.parseDouble(Objects.requireNonNull(cmd("nvidia-smi --format=csv --query-gpu=fan.speed")).split(" +")[2]);
                final double gpuUtilization = Double.parseDouble(Objects.requireNonNull(cmd("nvidia-smi --format=csv --query-gpu=utilization.gpu")).split(" +")[2]);
                final double outputAudioVolume = Double.parseDouble(Objects.requireNonNull(cmd("svcl.exe /Stdout /GetPercent \"Kopfhörer\"")));
                final double outputAudioMute = Double.parseDouble(Objects.requireNonNull(cmd("svcl.exe /Stdout /GetMute \"Kopfhörer\"")));

                final PerformanceStatsPacketData packet = PerformanceStatsPacketData.builder()
                        .cpuLoad(cpuLoad)
                        .physicalRam(physicalRam)
                        .freeRam(freeRam)
                        .gpuPowerDraw(gpuPowerDraw)
                        .gpuPowerLimit(gpuPowerLimit)
                        .gpuPowerMaxLimit(gpuPowerMaxLimit)
                        .gpuPowerMinLimit(gpuPowerMinLimit)
                        .gpuFanSpeedPercentage(gpuFanSpeedPercentage)
                        .gpuUtilization(gpuUtilization)
                        .outputAudioVolume(outputAudioVolume)
                        .outputAudioMute(outputAudioMute)
                        .build();

                statConsumer.forEach(con -> con.accept(packet));
            } catch (final Exception e) {
                e.printStackTrace();
            }


        }, 1, 1, TimeUnit.SECONDS);

        server.start();
        server.syncOpen();
    }
}
