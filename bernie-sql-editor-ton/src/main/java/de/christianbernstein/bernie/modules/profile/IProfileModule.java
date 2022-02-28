package de.christianbernstein.bernie.modules.profile;

import de.christianbernstein.bernie.ses.bin.Centralized;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.db.H2Repository;
import de.christianbernstein.bernie.shared.module.IBaseModuleClass;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
public interface IProfileModule extends IBaseModuleClass<ITon> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> profileModule = Module.<ITon>builder()
            .name("profile_module")
            .build()
            .$(module -> module.getShardManager().install(ProfileModule.class));

    IProfileContext context(@Nullable String viewerID, @NonNull String targetID);

    Centralized<H2Repository<BiographyMapping, String>> getBiographyRepo();

    void setBiography(@NonNull String targetID, String biography);
}
