package de.christianbernstein.bernie.modules.project;

import lombok.NonNull;

import java.util.function.UnaryOperator;

/**
 * @author Christian Bernstein
 */
public interface IProjectTaskContext {

    String getTaskID();

    IProjectModule module();

    ProjectTask getTaskData(boolean reload);

    IProjectTaskContext updateTask(@NonNull UnaryOperator<ProjectTask> updater);

    IProjectTaskContext setStatus(ProjectTaskStatus status);

    default IProjectTaskContext finishWithSuccess() {
        return this.setStatus(ProjectTaskStatus.SUCCESS);
    }

    // IProjectTaskContext delete();
}
