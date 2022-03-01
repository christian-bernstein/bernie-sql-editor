package de.christianbernstein.bernie.modules.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.function.UnaryOperator;

/**
 * @author Christian Bernstein
 */
@AllArgsConstructor
public class ProjectTaskContext implements IProjectTaskContext {

    @Getter
    @Accessors(fluent = true)
    private final IProjectModule module;

    @Getter
    private final String taskID;

    private ProjectTask cached;

    @Override
    public ProjectTask getTaskData(boolean reload) {
        if (reload || this.cached == null) {
            return this.module().getTask(this.taskID);
        } else {
            return this.cached;
        }
    }

    @Override
    public IProjectTaskContext updateTask(@NonNull UnaryOperator<ProjectTask> updater) {
        this.module().updateProjectTask(this.taskID, updater);
        return this;
    }

    @Override
    public IProjectTaskContext setStatus(ProjectTaskStatus status) {
        this.module().updateProjectTask(this.taskID, projectTask -> {
            projectTask.setStatus(status);
            return projectTask;
        });
        return this;
    }
}
