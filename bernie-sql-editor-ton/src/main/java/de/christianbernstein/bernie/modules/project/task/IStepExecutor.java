package de.christianbernstein.bernie.modules.project.task;

import lombok.NonNull;

/**
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface IStepExecutor {

    void execute(@NonNull StepContext ctx);

}
