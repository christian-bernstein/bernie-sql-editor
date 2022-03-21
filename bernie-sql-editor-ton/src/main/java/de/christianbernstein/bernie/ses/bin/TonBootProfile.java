package de.christianbernstein.bernie.ses.bin;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class TonBootProfile {

    @Singular
    private final List<String> names;

    private final Supplier<IMain<Ton>> mainSupplier;

}
