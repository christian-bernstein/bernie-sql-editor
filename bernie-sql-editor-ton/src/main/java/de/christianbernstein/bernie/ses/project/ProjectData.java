/*
 * Copyright (C) 2021 Christian Bernstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package de.christianbernstein.bernie.ses.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@Setter
@Getter
@Entity(name = "ProjectData")
@SuppressWarnings({"SqlResolve", "JpaDataSourceORMInspection"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProjectData {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    private String creatorUserID;

    private String title;

    private String description;

    private boolean stator;

    private Date lastEdited;

    private int edits;

}
