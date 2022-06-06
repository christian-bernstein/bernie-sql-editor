package de.christianbernstein.bernie.modules.sqlBookmarks;

import de.christianbernstein.bernie.sdk.misc.IFluently;
import de.christianbernstein.bernie.sdk.module.IBaseModuleClass;
import de.christianbernstein.bernie.ses.bin.ITon;

import java.util.List;

/**
 * @author Christian Bernstein
 */
public interface ISQLBookmarksModule extends IBaseModuleClass<ITon>, IFluently<ISQLBookmarksModule> {

    void add(Bookmark bookmark);

    void remove(String id);

    void edit(String id, Bookmark replacement);

    List<Bookmark> getAllVisibleBookmarks(String creatorID, String projectID);

}
