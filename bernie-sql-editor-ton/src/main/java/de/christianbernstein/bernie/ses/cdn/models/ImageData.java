package de.christianbernstein.bernie.ses.cdn.models;

import lombok.Data;

/**
 * @author Christian Bernstein
 */
@Data
public class ImageData {

    private ImageDataType type;

    private String src;

}
