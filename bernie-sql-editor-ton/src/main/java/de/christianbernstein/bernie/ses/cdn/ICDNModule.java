package de.christianbernstein.bernie.ses.cdn;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.cdn.models.UserPublicProfileData;
import de.christianbernstein.bernie.shared.module.Dependency;
import de.christianbernstein.bernie.shared.module.IBaseModuleClass;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
public interface ICDNModule extends IBaseModuleClass<ITon> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> cdnModule = Module.<ITon>builder()
            .name("cdn_module")
            .dependency(Dependency.builder().module("user_module").build())
            .build()
            .$(module -> module.getShardManager().install(CDNModule.class));

    CDNResponse request(@NonNull CDNRequest request);

    void registerResolverFromAnnotation(ICDNResolver<?> resolver, CDN annotation);
}
