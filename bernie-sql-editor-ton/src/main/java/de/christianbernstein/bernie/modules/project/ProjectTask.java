package de.christianbernstein.bernie.modules.project;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@Entity(name = "project_task")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTask {

    @Id
    @Column(name = "taskID")
    private String taskID;

    private String projectID;

    private String type;

    private String title;

    private ProjectTaskStatus status;

    private Date timestamp;

    @ElementCollection
    @CollectionTable(name = "project_task_data_mapping", joinColumns = {
            @JoinColumn(name = "data_id", referencedColumnName = "taskID")
    })
    @MapKeyColumn(name = "data_key")
    @Column(name = "data")
    private Map<String, String> data;
}
