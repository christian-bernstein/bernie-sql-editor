package de.christianbernstein.bernie.modules.project.task;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@Accessors(fluent = true)
public class StepContext {

    private final Task task;

}
