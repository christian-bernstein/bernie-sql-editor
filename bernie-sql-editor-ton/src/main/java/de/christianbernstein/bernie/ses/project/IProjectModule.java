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

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.module.Dependency;
import de.christianbernstein.bernie.shared.module.IBaseModuleClass;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
public interface IProjectModule extends IBaseModuleClass<ITon> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> projectModule = Module.<ITon>builder()
            .name("project_module")
            .dependency(Dependency.builder().module("user_module").build())
            .build()
            .$(module -> module.getShardManager().install(ProjectModule.class));

    List<ProjectData> getProjectsFromOwner(@NonNull UUID ownerUserID);

    void createProject(@NonNull ProjectCreationData data) throws ProjectAlreadyExistException;

    boolean containsProject(@NonNull UUID id);

}
