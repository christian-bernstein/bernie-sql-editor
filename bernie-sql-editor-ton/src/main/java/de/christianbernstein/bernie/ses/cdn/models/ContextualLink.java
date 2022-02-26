package de.christianbernstein.bernie.ses.cdn.models;

import lombok.Data;

/**
 * @author Christian Bernstein
 */
@Data
public class ContextualLink {

    private String context;

    private String link;

}
