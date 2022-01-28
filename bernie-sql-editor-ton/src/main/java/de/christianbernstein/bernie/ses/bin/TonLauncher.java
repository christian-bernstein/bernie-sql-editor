/*
 * Copyright (C) 2021 Christian Bernstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.ses.AutoExec;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ExecutorAnnotations.Command;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ISession;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.IntrinsicParameterAnnotations.APISession;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.IntrinsicParameterAnnotations.APIStatement;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ParamAnnotations.Flow;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.Statement;
import de.christianbernstein.bernie.ses.project.ProjectCreationData;
import de.christianbernstein.bernie.ses.project.ProjectData;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.misc.Utils;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This is the bootstrap class for the embedded ton server.
 *
 * @author Christian Bernstein
 */
public class TonLauncher {

    @SuppressWarnings("all")
    private static Optional<ITon> ton = Optional.empty();

    public static void main(String[] args) {
        new Ton().$(iTon -> TonLauncher.ton = Optional.of(iTon)).start(TonConfiguration.builder()
                .mode(TonMode.MOCK)
                .workingDirectory("./ton/")
                .jraPhaseOrder(new String[][]{{Constants.constructJRAPhase}, {Constants.useTonJRAPhase}, {Constants.moduleJRAPhase}, {Constants.flowJRAPhase}, {Constants.autoEcexJRAPhase}})
                .build()
        );
    }

    @AutoExec
    private static void console() {
        final GloriaAPI.IGloria gloria = new GloriaAPI.Gloria("ton-zentral-io");

        gloria.registerMethodsInClass(TonLauncher.class, true);
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
                Utils.durationMonitoredExecution(() -> statement.getApi().submit(command, statement.getSender(), GloriaAPI.IGloria.DEFAULT_INBOUND_HANDLER_IDENTIFIER)).toMillis()
        ));
    }

    @Command(path = "debug", literal = "shutdown")
    private void shutdown() {
        ton.orElseThrow().shutdown();
    }

    @Command(path = "debug", literal = "getProjects", aliases = "gP")
    private void getProjects(@APIStatement Statement statement, @APISession ISession session) {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ton-zentral-io", String.format("Projects: '%s'", ton
                .orElseThrow()
                .projectModule()
                .getProjectsFromOwner(ton.orElseThrow().userModule().root().getID())
                .stream()
                .map(ProjectData::toString)
                .collect(Collectors.joining(", "))
        ));
    }

    @Command(path = "debug", literal = "createProject", aliases = "cP")
    private void createProject(@NonNull String title, boolean stator) {
        ton.orElseThrow().projectModule().createProject(ProjectCreationData.builder()
                .creatorUserID(ton.orElseThrow().userModule().root().getID())
                .title(title)
                .stator(stator)
                .build());
    }
}
