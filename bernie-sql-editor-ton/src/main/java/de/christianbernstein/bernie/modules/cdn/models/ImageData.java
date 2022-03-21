package de.christianbernstein.bernie.modules.cdn.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Christian Bernstein
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {

    private ImageDataType type;

    private String src;

}
