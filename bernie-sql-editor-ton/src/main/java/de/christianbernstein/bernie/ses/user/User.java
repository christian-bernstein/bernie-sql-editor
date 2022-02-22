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

package de.christianbernstein.bernie.ses.user;

import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.module.IEngine;
import lombok.Data;

import java.util.function.Function;

/**
 * @author Christian Bernstein
 */
@Data
public class User implements IUser {

    private final IEngine<ITon> engine;

    private final String userID;

    @Override
    public String getID() {
        return this.userID;
    }

    @Override
    public UserData getUserData() {
        System.err.println("get user data of: " + this.userID);
        // todo why does it use the id to get a user by its username
        return this.engine.api().userModule().getUserDataOf(this.userID);
    }

    @Override
    public IEngine<ITon> getRelatedEngine() {
        return this.engine;
    }

    @Override
    public void updateUserData(Function<UserData, UserData> updater) {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public UserProfileData getProfileData() {
        final UserData data = this.getUserData();

        System.err.println("user profile data: " + data);

        return UserProfileData.builder()
                .id(data.getId())
                .firstname(data.getFirstname())
                .lastname(data.getLastname())
                .username(data.getUsername())
                .lastActive(data.getLastActive())
                .userEntrySetupDate(data.getUserEntrySetupDate())
                .build();
    }
}
