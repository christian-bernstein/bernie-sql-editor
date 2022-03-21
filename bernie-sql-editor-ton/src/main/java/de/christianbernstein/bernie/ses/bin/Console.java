package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.ses.annotations.AutoExec;
import de.christianbernstein.bernie.ses.annotations.Threaded;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.modules.project.ProjectAlreadyExistException;
import de.christianbernstein.bernie.modules.project.ProjectCreationData;
import de.christianbernstein.bernie.modules.project.ProjectData;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ISession;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ParamAnnotations.Flow;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ParamAnnotations.Param;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.Statement;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.misc.Resource;
import de.christianbernstein.bernie.shared.misc.Utils;
import de.christianbernstein.bernie.shared.module.Lifecycle;
import de.christianbernstein.bernie.shared.module.Module;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.christianbernstein.bernie.shared.gloria.GloriaAPI.ExecutorAnnotations.Command;
import static de.christianbernstein.bernie.shared.gloria.GloriaAPI.IntrinsicParameterAnnotations.APISession;
import static de.christianbernstein.bernie.shared.gloria.GloriaAPI.IntrinsicParameterAnnotations.APIStatement;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("unused")
public class Console {

    @UseTon
    private static ITon ton;

    @Threaded
    private static ExecutorService main;

    @AutoExec
    private static void console() {
        final GloriaAPI.IGloria gloria = new GloriaAPI.Gloria("ton-zentral-io");

        // todo create annotation for adding the console classes
        gloria.registerMethodsInClass(Console.class, true);
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

    @Command(literal = "debug", aliases = {"d", "test"}, type = Command.Type.JUNCTION)
    private void debug() {}

    @Command(path = "debug", literal = "monitor", aliases = "m")
    private void monitor(@APIStatement Statement statement, @APISession ISession session, @Flow String command) {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ton-zentral-io", String.format("Command '%s' took %sms",
                command,
                Utils.durationMonitoredExecution(() -> {
                    statement.getApi().submit(command, statement.getSender(), GloriaAPI.IGloria.DEFAULT_INBOUND_HANDLER_IDENTIFIER);
                }).toMillis()
        ));
    }

    @Command(literal = "end")
    @Command(path = "debug", literal = "shutdown", aliases = "s")
    private void shutdown() {
        ton.shutdown();
    }


    @Command(path = "debug", literal = "listModules", aliases = "lM")
    private void listModules(@Param(mandatory = false, name = "filter") String filterLifecycle) {
        Function<Lifecycle, String> lifecycleDisplay = lifecycle -> {
            switch (lifecycle) {
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
