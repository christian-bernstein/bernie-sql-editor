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
import de.christianbernstein.bernie.shared.db.H2Repository;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.module.Module;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author Christian Bernstein
 */
public class UserModule implements IUserModule {

    private final UserModuleConfig configuration = UserModuleConfig.defaultConfiguration;

    private H2Repository<UserData, String> repository;

    private IEngine<ITon> engine;

    private IUser rootUser;

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IUserModule.super.boot(api, module, manager);
        this.engine = manager;
        this.repository = new H2Repository<>(UserData.class, this.configuration.getRepositoryConfiguration());
        this.createRootUser();

        System.err.println(this.plainCreateAccount(UserData.builder()
                .userEntrySetupDate(new Date())
                .lastActive(new Date())
                .username("Christian")
                .firstname("Christian")
                .lastname("Bernstein")
                .password("root")
                .id(UUID.randomUUID().toString())
                .build()));
    }

    @Override
    public void uninstall(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IUserModule.super.uninstall(api, module, manager);
        this.engine = null;
    }

    /**
     * todo fix -> returns only true
     */
    @Override
    public boolean hasAccount(String id) {
        final AtomicInteger len = new AtomicInteger();
        // Count the amount of occurrences of id in the table
        this.repository.session(session -> session.doWork(connection -> {
            // final ResultSet set = connection.prepareStatement("select count(*) as \"len\" from %s".replace("%s", this.repository.getTableName())).executeQuery();
            @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
            final ResultSet set = connection.prepareStatement("select count(*) as \"len\" from %s where id='%val'".replace("%s", this.repository.getTableName()).replace("%val", id)).executeQuery();
            if (set.next()) {
                len.set(set.getInt("len"));
            } else {
                // todo make better error description
                throw new RuntimeException("Cannot resolve account id");
            }
        }));
        // Check consistency in table & interpret result
        if (len.get() == 1) {
            return true;
        } else if (len.get() == 0) {
            return false;
        } else {
            throw new IllegalStateException("Database inconsistency " + len.get());
        }
    }

    @Override
    public UserCreationResult plainCreateAccount(@NotNull UserData data) {
        // Check if the uuid does already exist (It has to be unique)
        if (this.hasAccount(data.getId())) {
            return UserCreationResult.UUID_ALREADY_TAKEN;
        }
        // Check if the username does already exist (It has to be unique)
        if (this.getUserDataOfUsername(data.getUsername()) != null) {
            return UserCreationResult.USERNAME_ALREADY_TAKEN;
        }
        // After checking for consistency, insert the user data into the repository
        try {
            this.repository.save(data);
            return UserCreationResult.OK;
        } catch (final Exception e) {
            e.printStackTrace();
            return UserCreationResult.INTERNAL_ERROR;
        }
    }

    @Override
    public void plainDeleteUser(String uuid) {
        try {
            this.repository.session(session -> session.delete(this.getUserDataOf(uuid)));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IUser root() {
        if (this.rootUser == null) {
            this.rootUser = this.getUser(this.configuration.getRootUsername());
        }
        return this.rootUser;
    }

    @Override
    public UserData getUserDataOfUsername(String username) {
        @SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
        final List<UserData> result = this.repository.nq("select top 1 * from %s where username='" + username + "'");
        if (result.size() != 1) {
            return null;
        } else {
            return result.get(0);
        }
    }

    @Override
    public UserData getUserDataOf(String id) {
        return this.repository.get(id);
    }

    @Override
    public IUser getUserOfUsername(String username) {
        final UserData userData = this.getUserDataOfUsername(username);
        return new User(this.engine, userData.getId());
    }

    @Override
    public IUser getUser(String id) {
        // todo remove get user data => not required
        final UserData userData = this.getUserDataOf(id);
        return new User(this.engine, userData.getId());
    }

    @Override
    public void updateUserData(String id, @NotNull Function<UserData, UserData> updater) {
        UserData data = this.getUserDataOf(id);
        data = updater.apply(data);
        this.plainDeleteUser(id);
        this.plainCreateAccount(data);
    }

    private void createRootUser() {
        this.plainCreateAccount(UserData.builder()
                .userEntrySetupDate(new Date())
                .lastActive(new Date())
                .username(this.configuration.getRootUsername())
                .firstname(this.configuration.getRootUsername())
                .lastname("")
                .password(this.configuration.getRootPassword())
                .id(UUID.randomUUID().toString())
                .build());
    }

    @Override
    public @NonNull IUserModule me() {
        return this;
    }
}
