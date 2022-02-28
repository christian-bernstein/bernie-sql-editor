package de.christianbernstein.bernie.modules.profile;

import de.christianbernstein.bernie.ses.bin.Centralized;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.db.H2Repository;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.module.Module;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
public class ProfileModule implements IProfileModule {

    @Getter
    private Centralized<H2Repository<BiographyMapping, String>> biographyRepo;

    @Override
    public IProfileContext context(@Nullable String viewerID, @NonNull String targetID) {
        return new ProfileContext(new ProfileContextMeta(viewerID, targetID, this));
    }

    @Override
    public void setBiography(@NonNull String targetID, String biography) {
        biographyRepo.get().update(elem -> {
            elem.setBiography(biography);
            return elem;
        }, targetID);
    }

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IProfileModule.super.boot(api, module, manager);
        this.biographyRepo = api.db(BiographyMapping.class);

        this.biographyRepo.get().save(new BiographyMapping("626ff913-9faa-4e3d-9d41-1cd4636213ca", "Hello world!"));
    }
}
