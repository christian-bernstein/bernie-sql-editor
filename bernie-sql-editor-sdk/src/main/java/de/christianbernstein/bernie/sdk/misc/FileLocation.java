package de.christianbernstein.bernie.sdk.misc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Christian Bernstein
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class FileLocation {

    private String location;

    private FileLocationType type;

}
