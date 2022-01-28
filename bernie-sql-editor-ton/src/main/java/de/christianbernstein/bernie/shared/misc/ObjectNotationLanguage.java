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

package de.christianbernstein.bernie.shared.misc;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * @author Christian Bernstein
 */
public enum ObjectNotationLanguage {

    JSON(false, new ISerialAdapter() {
        @Override
        public <T> T deserialize(@NonNull String serial, @Nullable Class<T> type) {
            return Utils.getGSON().fromJson(serial, type);
        }

        @Override
        public <T> String serialize(@NonNull Object object, @Nullable Class<T> type) {
            assert type != null;
            return Utils.getGSON().toJson(object, type);
        }

        @Override
        public <T> String serialize(@NonNull Object object, Type type) {
            return Utils.getGSON().toJson(object, type);
        }
    }, "json"),

    YAML(true, new ISerialAdapter() {
        @Override
        public <T> T deserialize(@NonNull String serial, @Nullable Class<T> type) {
            return Utils.getYAML().load(serial);
        }

        @Override
        public <T> String serialize(@NonNull Object object, @Nullable Class<T> type) {
            return Utils.getYAML().dump(object);
        }

        @Override
        public <T> String serialize(@NonNull Object object, Type type) {
            throw new UnsupportedOperationException("not implemented yet");
        }
    }, "yaml");

    @Getter
    private final boolean classlessTranslation;

    @Getter
    private final ISerialAdapter serialAdapter;

    @Getter
    private final String fileExtension;

    ObjectNotationLanguage(boolean classlessTranslation, @NonNull ISerialAdapter serialAdapter, String fileExtension) {
        this.classlessTranslation = classlessTranslation;
        this.serialAdapter = serialAdapter;
        this.fileExtension = fileExtension;
    }
}
