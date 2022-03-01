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

package de.christianbernstein.bernie.modules.project;

import de.christianbernstein.bernie.ses.bin.Centralized;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.db.H2Repository;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.module.IEngine;
import lombok.NonNull;
import org.hibernate.query.Query;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * todo inform listenigng clients about updates targeting their project
 *  - save connections to a map
 *  - whenever an update occours, push packet to the client informing them about the update
 *  (system like the one in the database module)
 *
 * @author Christian Bernstein
 */
@SuppressWarnings("all")
public class ProjectModule implements IProjectModule {

    private static Optional<ProjectModule> instance = Optional.empty();

    // todo move to discoverer class
    // @Discoverer(packetID = "ListProjectPacketData", datatype = ListProjectPacketData.class, protocols = Constants.centralProtocolName)
    // private static final IPacketHandlerBase<ListProjectPacketData> listProjectHandler = (data, endpoint, socket, packet, server) -> {
    //     instance.ifPresentOrElse(module -> {
    //         final List<ProjectData> projects = module.getProjectsFromOwner(Shortcut.useSLI(endpoint).getSessionID());
    //         endpoint.respond(new ListProjectResponsePacketData(projects), packet.getId());
    //     }, () -> {
    //         System.err.println("project module instance is empty");
    //     });
    // };

    private static Optional<ITon> ton = Optional.empty();

    private Centralized<H2Repository<ProjectData, UUID>> projectRepository;

    private Centralized<H2Repository<ProjectTask, String>> projectTaskRepository;

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IProjectModule.super.boot(api, module, manager);
        ton = Optional.of(api);
        instance = Optional.of(this);
        this.projectRepository = api.db(ProjectData.class);
        this.projectTaskRepository = api.db(ProjectTask.class);
        this.createInternalDatabaseProject();
    }

    /**
     * todo select the projects by native query, not just get all. !IMPORTANT BUG FIX!!!
     */
    @Override
    public List<ProjectData> getProjectsFromOwner(@NonNull String ownerUserID) {
        // return this.projectRepository.get().hq("from ProjectData");
        final AtomicReference<List<ProjectData>> pd  = new AtomicReference<>();
        this.projectRepository.get().session(session -> {
            final Query query = session.createQuery("from ProjectData where creatorUserID = :id");
            query.setParameter("id", ownerUserID);
            pd.set(query.list());
        });
        return pd.get();
    }

    @Override
    public void createProject(@NonNull ProjectCreationData data) throws ProjectAlreadyExistException {
        final String id = data.getId() == null ? UUID.randomUUID().toString() : data.getId();
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, String.format("Try to create a new project with id '%s'", id));
        this.projectRepository.get().save(ProjectData.builder()
                .title(data.getTitle())
                .creatorUserID(data.getCreatorUserID())
                .description(data.getDescription())
                .stator(data.isStator())
                .edits(0)
                .id(id)
                .lastEdited(new Date())
                .build());
    }

    @Override
    public boolean containsProject(@NonNull UUID id) {
        return this.projectRepository.get().get(id) != null;
    }

    @Override
    public void deleteProject(@NonNull UUID id) {
        try {
            this.projectRepository.get().session(session -> session.doWork(connection -> {
                @Language("H2")
                final String sql = "delete from %s where id='%s'";
                final String format = String.format(sql, this.projectRepository.get().getTableName(), id);
                System.out.println("delete :: " + format);
                connection.prepareStatement(format).executeUpdate();
            }));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProjectTask(String taskID, UnaryOperator<ProjectTask> updater) {
        this.projectTaskRepository.get().update(updater, taskID);
    }

    /**
     * todo .filter will get very resource intens, if the number of tasks in the system rises! -> Change to more efficient native sql solution
     */
    @Override
    public List<ProjectTask> getTasksFromProjectID(@NonNull String projectID, int amount, int offset) {
        final List<ProjectTask> tasks = this.projectTaskRepository.get().filter(projectTask -> projectTask.getProjectID().equals(projectID));
        if (offset > tasks.size()) {
            return tasks;
        } else {
            final int startIndex = tasks.size() - offset;
            return tasks.subList(startIndex, startIndex + amount);
        }
    }

    @Override
    public IProjectTaskContext createTask(ProjectTask taskData) {
        try {
            this.projectTaskRepository.get().save(taskData);
            return new ProjectTaskContext(this, taskData.getTaskID(), taskData);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ProjectTask getTask(@NonNull String taskID) {
        return this.projectTaskRepository.get().get(taskID);
    }

    @Override
    public IProjectTaskContext initTaskContext(@NonNull String taskID) {
        throw new UnsupportedOperationException();
    }

    // todo this is a debug method and should not be available in production
    private void createInternalDatabaseProject() {
        try {
            this.createProject(ProjectCreationData.builder()
                    .id("ton")
                    .title("SES internal infrastructure db")
                    .creatorUserID(ton.get().userModule().getUserOfUsername("root").getID())
                    .stator(true)
                    .description("Automatically generated bridge between SES's internal infrastructure and the SQL-Editor panel. **This is a debug project and should never be generated in production mode.**")
                    .build());
        } catch (final ProjectAlreadyExistException e) {
            e.printStackTrace();
        }
    }
}
