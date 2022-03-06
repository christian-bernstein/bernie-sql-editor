package resource.models;

import lombok.*;

/**
 * @author Christian Bernstein
 */
@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Builder.Default
    private String name = "Franz";

    @Builder.Default
    private int age = 42;

}
