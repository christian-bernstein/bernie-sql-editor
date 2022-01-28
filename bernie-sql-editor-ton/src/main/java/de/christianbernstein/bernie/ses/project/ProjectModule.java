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

package de.christianbernstein.bernie.ses.project;

import de.christianbernstein.bernie.ses.bin.Centralized;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import de.christianbernstein.bernie.ses.project.in.ListProjectPacketData;
import de.christianbernstein.bernie.ses.project.out.ListProjectResponsePacketData;
import de.christianbernstein.bernie.shared.db.H2Repository;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.shared.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.shared.module.IEngine;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("all")
public class ProjectModule implements IProjectModule {

    private static Optional<ProjectModule> instance = Optional.empty();

    @Discoverer(packetID = "ListProjectPacketData", datatype = ListProjectPacketData.class, protocols = Constants.centralProtocolName)
    private static final IPacketHandlerBase<ListProjectPacketData> listProjectHandler = (data, endpoint, socket, packet, server) -> {
        instance.ifPresentOrElse(module -> {
            final List<ProjectData> projects = module.getProjectsFromOwner(Shortcut.useSLI(endpoint).getSessionID());
            endpoint.respond(new ListProjectResponsePacketData(projects), packet.getId());
        }, () -> {
            System.err.println("project module instance is empty");
        });
    };

    private static Optional<ITon> ton = Optional.empty();

    private Centralized<H2Repository<ProjectData, UUID>> projectRepository;

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IProjectModule.super.boot(api, module, manager);
        ton = Optional.of(api);
        instance = Optional.of(this);
        this.projectRepository = api.db(ProjectData.class);
    }

    /**
     * todo select the projects by native query, not just get all.
     */
    @Override
    public List<ProjectData> getProjectsFromOwner(@NonNull UUID ownerUserID) {
        return this.projectRepository.get().hq("from ProjectData");
    }

    @Override
    public void createProject(@NonNull ProjectCreationData data) {
        this.projectRepository.get().save(ProjectData.builder()
                .title(data.getTitle())
                .creatorUserID(data.getCreatorUserID())
                .stator(data.isStator())
                .edits(0)
                .id(UUID.randomUUID())
                .lastEdited(new Date())
                .build());
    }

    @Override
    public boolean containsProject(@NonNull UUID id) {
        return this.projectRepository.get().get(id) != null;
    }
}
