package de.christianbernstein.bernie.modules.project;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectAlreadyExistException extends Exception {

    private final UUID projectOwnerUUID;

    private final String title;

}
