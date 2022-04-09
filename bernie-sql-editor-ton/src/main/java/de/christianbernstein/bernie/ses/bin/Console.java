package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.sdk.misc.ConsoleColors;
import de.christianbernstein.bernie.ses.annotations.AutoExec;
import de.christianbernstein.bernie.ses.annotations.Threaded;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.modules.project.ProjectAlreadyExistException;
import de.christianbernstein.bernie.modules.project.ProjectCreationData;
import de.christianbernstein.bernie.modules.project.ProjectData;
import de.christianbernstein.bernie.sdk.gloria.GloriaAPI;
import de.christianbernstein.bernie.ses.annotations.CommandClass;
import de.christianbernstein.bernie.sdk.gloria.GloriaAPI.IGloria;
import de.christianbernstein.bernie.sdk.gloria.GloriaAPI.ISession;
import de.christianbernstein.bernie.sdk.gloria.GloriaAPI.ParamAnnotations.Flow;
import de.christianbernstein.bernie.sdk.gloria.GloriaAPI.ParamAnnotations.Param;
import de.christianbernstein.bernie.sdk.gloria.GloriaAPI.Statement;
import de.christianbernstein.bernie.sdk.misc.ConsoleLogger;
import de.christianbernstein.bernie.sdk.misc.Resource;
import de.christianbernstein.bernie.sdk.misc.Utils;
import de.christianbernstein.bernie.sdk.module.Lifecycle;
import de.christianbernstein.bernie.sdk.module.Module;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.christianbernstein.bernie.sdk.gloria.GloriaAPI.ExecutorAnnotations.Command;
import static de.christianbernstein.bernie.sdk.gloria.GloriaAPI.IntrinsicParameterAnnotations.APISession;
import static de.christianbernstein.bernie.sdk.gloria.GloriaAPI.IntrinsicParameterAnnotations.APIStatement;

/**
 * @author Christian Bernstein
 */
@CommandClass
@SuppressWarnings("unused")
public class Console {

    public static final List<ConsoleCommandRegisterRequest> requests = new ArrayList<>();

    @UseTon
    private static ITon ton;

    @Threaded
    private static ExecutorService main;

    @AutoExec
    private static void console() {
        final IGloria gloria = new GloriaAPI.Gloria(Constants.mainGloriaInstanceID);

        requests.stream().filter(req -> req.getGloriaInstance().equals(gloria.getInstanceIdentifier())).forEach(req -> gloria.registerMethodsInClass(req.getTarget(), req.isAutoInstanceInvoking()));
        gloria.registerMethodsInClass(GloriaAPI.GloriaCommandAddons.UtilityAddon.class, true);

        main.execute(() -> {
            Supplier<String> lineRetriever;

            if (System.console() != null) {
                lineRetriever = () -> System.console().readLine();
            } else {
                lineRetriever = () -> {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    // todo fix https://stackoverflow.com/questions/50394846/interrupt-system-console-readline
                    boolean waiting = true;
                    do {
                        try {
                            if (!br.ready()) {
                                //noinspection BusyWait
                                Thread.sleep(200);
                            } else {
                                waiting = false;
                            }
                        } catch (final IOException e) {
                            e.printStackTrace();
                        } catch (final InterruptedException ignored) {}
                    } while (waiting && !main.isShutdown());

                    try {
                        return br.readLine();
                    } catch(final IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                };
            }

            while (!main.isShutdown() && gloria.getSessionManager().getStaticSession().getSessionData().getOr("keep-online", true)) {
                String line = lineRetriever.get();
                if (line != null && !line.isBlank()) {
                    gloria.submit(line.trim());
                }
            }
        });
    }

    @Command(literal = "debug", description = "All debugging methods are categorized under the 'debug'-command. They wont be executable if ton didn't started in debugging mode.", aliases = {"d"}, type = Command.Type.JUNCTION)
    private void debug() {}

    @Command(path = "debug", literal = "monitor", aliases = "m")
    private void monitor(@APIStatement Statement statement, @APISession ISession session, @Flow String command) {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ton-zentral-io", String.format("Command '%s' took %sms",
                command,
                Utils.durationMonitoredExecution(() -> statement.getApi().submit(command, statement.getSender(), IGloria.DEFAULT_INBOUND_HANDLER_IDENTIFIER)).toMillis()
        ));
    }

    @Command(literal = "end")
    @Command(path = "debug", literal = "shutdown", aliases = "s")
    private void shutdown() {
        ton.shutdown();
    }

    /**
     * todo fix two errors
     */
    @Command(literal = "restart", aliases = {"reboot"})
    private void restart() {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "central-io", "Trying to restart ton…");
        ton.$(ton -> {
            final TonConfiguration configuration = ton.configuration();
            ton.shutdown(false).start(configuration);
        });
    }

    @Command(path = "debug", literal = "listModules", aliases = "lM")
    private void listModules(@Param(mandatory = false, name = "filter") String filterLifecycle) {
        Function<Lifecycle, String> lifecycleDisplay = lifecycle -> {
            switch (lifecycle) {
                // todo replace by ConsoleColor's confined method
                case ENGAGED -> {
                    return String.format("%s%s%s", ConsoleColors.GREEN_BACKGROUND, lifecycle, ConsoleColors.RESET);
                }
                case DISENGAGED -> {
                    return String.format("%s%s%s", ConsoleColors.RED_BACKGROUND_BRIGHT, lifecycle, ConsoleColors.RESET);
                }
                case ENGAGING -> {
                    return String.format("%s%s%s", ConsoleColors.YELLOW_BACKGROUND_BRIGHT, lifecycle, ConsoleColors.RESET);
                }
                case INSTALLED -> {
                    return lifecycle.name();
                }
                default -> {
                    return "ERR";
                }
            }
        };
        Stream<Module<ITon>> stream = ton
                .engine()
                .getModules()
                .stream();
        if (filterLifecycle != null) {
            try {
                // todo check if filterLifeCycle is contained un Lifecycle enum
                final Lifecycle filter = Lifecycle.valueOf(filterLifecycle.toUpperCase(Locale.ROOT));
                stream = stream.filter(mod -> mod.getLifecycle() == filter);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        final List<Module<ITon>> modules = stream.collect(Collectors.toList());
        if (!modules.isEmpty()) {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ton-zentral-io", String.format("Installed modules: '%s'", modules
                    .stream()
                    .map(mod -> String.format("(%s, %s)", mod.getName(), lifecycleDisplay.apply(mod.getLifecycle())))
                    .collect(Collectors.joining(", "))
            ));
        } else {
            ConsoleLogger.def().log(ConsoleLogger.LogType.WARN, "ton-zentral-io", String.format(
                    "No modules found (Filter: '%s')", filterLifecycle
            ));
        }
    }

    /**
     * Get projects from root user
     */
    @Command(path = "debug", literal = "getProjects", aliases = "gP")
    private void getProjects(@APIStatement Statement statement, @APISession ISession session) {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ton-zentral-io", String.format("Projects: '%s'", ton
                .projectModule()
                .getProjectsFromOwner(ton.userModule().root().getID())
                .stream()
                .map(ProjectData::toString)
                .collect(Collectors.joining(", "))
        ));
    }

    @Command(path = "debug", literal = "createProject", aliases = "cP")
    private void createProject(@NonNull String title, boolean stator, @Flow String description) {
        try {
            ton.projectModule().createProject(ProjectCreationData.builder()
                    .creatorUserID(ton.userModule().root().getID())
                    .title(title)
                    .stator(stator)
                    .description(description)
                    .build());
        } catch (ProjectAlreadyExistException e) {
            e.printStackTrace();
        }
    }

    @Command(path = "config", literal = "disengage", tags = "unstable")
    private void disengageConfig() {
        try {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ton-zentral-io", "Disengaging central configuration file.");
            final Resource<TonConfiguration> resource = ton.configResource();
            resource.stop();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Command(path = "config", literal = "engage", tags = "unstable")
    private void engageConfig(boolean populate) {
        try {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ton-zentral-io", "Engaging central configuration file.");
            final Resource<TonConfiguration> resource = ton.configResource();
            resource.wipeCache();
            resource.init(populate, () -> ton.defaultConfiguration());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Command(path = "config", literal = "reset", tags = "unstable")
    private void resetConfig() {
        try {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ton-zentral-io", "Resetting central configuration file to default value.");
            final Resource<TonConfiguration> resource = ton.configResource();
            resource.stop();
            resource.wipeCache();
            resource.init(true, () -> ton.defaultConfiguration());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
