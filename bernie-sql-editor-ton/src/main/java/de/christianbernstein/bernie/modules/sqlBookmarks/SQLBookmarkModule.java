package de.christianbernstein.bernie.modules.sqlBookmarks;

import de.christianbernstein.bernie.sdk.db.H2Repository;
import de.christianbernstein.bernie.sdk.db.H2RepositoryConfiguration;
import de.christianbernstein.bernie.sdk.module.IEngine;
import de.christianbernstein.bernie.sdk.module.Module;
import de.christianbernstein.bernie.ses.bin.Centralized;
import de.christianbernstein.bernie.ses.bin.ITon;
import lombok.NonNull;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Christian Bernstein
 */
public class SQLBookmarkModule implements ISQLBookmarksModule {

    private Centralized<H2Repository<Bookmark, String>> repository = null;

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        ISQLBookmarksModule.super.boot(api, module, manager);
        this.repository = api.db(Bookmark.class);
    }

    @Override
    public @NonNull ISQLBookmarksModule me() {
        return this;
    }

    @Override
    public void add(Bookmark bookmark) {
        this.repository.get().save(bookmark);
    }

    @Override
    public void remove(String id) {
        this.repository.get().session(session -> session.doWork(connection -> {
            @SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
            @Language("H2")
            String sql = "delete from bookmarks where id='%s'";
            connection.prepareStatement(String.format(sql, id)).executeUpdate();
        }));
    }

    @Override
    public void edit(String id, Bookmark replacement) {
        this.repository.get().update(bookmark -> replacement, id);
    }

    @Override
    public List<Bookmark> getAllVisibleBookmarks(String creatorID, String projectID) {
        @SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
        @Language("H2")
        String sql = "select distinct * from bookmarks where (scope='%s') or (scope='%s' and creatorID='%s') or (scope='%s' and projectID='%s') or (scope='%s' and projectID='%s' and creatorId='%s')";
        return this.repository.get().nq(sql);
    }
}
