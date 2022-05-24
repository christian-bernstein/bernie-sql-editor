package de.christianbernstein.bernie.ses.tests;

import de.christianbernstein.bernie.sdk.misc.Utils;
import de.christianbernstein.bernie.ses.annotations.AutoExec;
import de.christianbernstein.bernie.ses.bin.FanoutProcedure;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class PerformanceTest {

    @SuppressWarnings("SqlNoDataSourceInspection")
    @AutoExec(mode = "benchmark", run = false)
    private final FanoutProcedure performanceTest = ton -> System.err.printf("%d op/s%n", Utils.measureThroughput("db pull throughput test", Duration.of(10, ChronoUnit.SECONDS), () -> ton.dbModule().pull(ton.userModule().root().getUserData(), "ton", "show tables from INFORMATION_SCHEMA")));

}
