package de.christianbernstein.bernie.modules.db;

import de.christianbernstein.bernie.modules.session.Client;
import de.christianbernstein.bernie.shared.misc.SerializedException;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class DBCommandError {

    private String message;

    private String title;

    private String id;

    private List<SerializedException> exceptions;

    private Map<String, Object> data;

    private boolean success;

    private Date timestamp;

    private Client client;

    private SessionCommandType commandType;
}
