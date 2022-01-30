package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.ses.AutoExec;
import de.christianbernstein.bernie.ses.UseTon;
import de.christianbernstein.bernie.ses.project.ProjectCreationData;
import de.christianbernstein.bernie.ses.project.ProjectData;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ISession;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ParamAnnotations.Flow;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.Statement;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.misc.Utils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.christianbernstein.bernie.shared.gloria.GloriaAPI.ExecutorAnnotations.*;
import static de.christianbernstein.bernie.shared.gloria.GloriaAPI.IntrinsicParameterAnnotations.*;

/**
 * @author Christian Bernstein
 */
public class Console {

    @UseTon
    private static ITon ton;

    @AutoExec
    private static void console() {
        final GloriaAPI.IGloria gloria = new GloriaAPI.Gloria("ton-zentral-io");

        // todo create annotation for adding the console classes
        gloria.registerMethodsInClass(Console.class, true);
        Supplier<String> lineRetriever;

        if (System.console() != null) {
            lineRetriever = () -> System.console().readLine();
        } else {
            lineRetriever = () -> {
                final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                try {
                    return br.readLine();
                } catch(final IOException e) {
                    e.printStackTrace();
                    return null;
                }
            };
        }

        while (gloria.getSessionManager().getStaticSession().getSessionData().getOr("keep-online", true)) {
            String line = lineRetriever.get();
            if (line != null && !line.isBlank()) {
                gloria.submit(line.trim());
            }
        }
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

    @Command(path = "debug", literal = "shutdown")
    private void shutdown() {
        ton.shutdown();
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
    private void createProject(@NonNull String title, boolean stator) {
        ton.projectModule().createProject(ProjectCreationData.builder()
                .creatorUserID(ton.userModule().root().getID())
                .title(title)
                .stator(stator)
                .build());
    }
}
