/*
 * Copyright (C) 2022 Christian Bernstein
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

import de.christianbernstein.bernie.ses.bin.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@AllArgsConstructor
public class ProjectCreationData {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String creatorUserID;

    private String title;

    private String description;

    private boolean stator;

    @Builder.Default
    private String dbFactoryID = Constants.defaultDbFactoryID;

    @Builder.Default
    private Map<String, Object> dbFactoryParams = new HashMap<>();
}
