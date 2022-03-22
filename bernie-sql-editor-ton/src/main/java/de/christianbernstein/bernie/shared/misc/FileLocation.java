package de.christianbernstein.bernie.shared.misc;

import lombok.*;

/**
 * @author Christian Bernstein
 */
@Data()
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class FileLocation {

    private String location;

    private FileLocationType type;


}
