package de.christianbernstein.bernie.ses.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * todo implement
 *
 * @author Christian Bernstein
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {

    /**
     * The identifier of the column (the column name)
     */
    private String id;

}
