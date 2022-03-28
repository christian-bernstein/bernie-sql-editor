package de.christianbernstein.bernie.shared.misc;

import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Ton;
import lombok.*;

/**
 * @author Christian Bernstein
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class FileLocation {

    private String location;

    private FileLocationType type;

    public String getInterpolated(@NonNull ITon ton) {
        return ton.interpolate(this.location);
    }
}
