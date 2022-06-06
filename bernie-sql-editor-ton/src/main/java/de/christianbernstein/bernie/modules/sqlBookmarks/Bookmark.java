package de.christianbernstein.bernie.modules.sqlBookmarks;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "bookmarks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Bookmark {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    private String sql;

    private String creatorID;

    @Nullable
    private String projectID;

    private String note;

    @Enumerated(EnumType.STRING)
    private BookmarkScope scope;

}
