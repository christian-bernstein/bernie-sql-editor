package de.christianbernstein.bernie.modules.cdn;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.sdk.module.Dependency;
import de.christianbernstein.bernie.sdk.module.IBaseModuleClass;
import de.christianbernstein.bernie.sdk.module.Module;
import de.christianbernstein.bernie.sdk.module.ModuleDefinition;
import lombok.NonNull;

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
