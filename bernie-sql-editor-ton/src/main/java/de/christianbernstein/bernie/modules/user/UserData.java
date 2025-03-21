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

package de.christianbernstein.bernie.modules.user;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@ToString
@Entity
@Table(name = "user_data")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserData {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    private String username;

    private String firstname;

    private String lastname;

    private String password;

    private String email;

    private Date userEntrySetupDate;

    private Date lastActive;

}
