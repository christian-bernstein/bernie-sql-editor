package de.christianbernstein.bernie.modules.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Christian Bernstein
 */
@Setter
@Getter
@Entity(name = "Biography")
@SuppressWarnings({"SqlResolve", "JpaDataSourceORMInspection"})
@NoArgsConstructor
@AllArgsConstructor
public class BiographyMapping {

    @Id
    @Column(name = "id", nullable = false)
    private String userID;

    private String biography;
}
