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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;


/**
 * @author Christian Bernstein
 */
public interface ISerialAdapter {

    ISerialAdapter defaultGsonSerialAdapter = new ISerialAdapter() {

        private final Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:'Z'")
                .disableHtmlEscaping()
                .serializeNulls()
                .setPrettyPrinting()
                .addDeserializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        final Expose expose = fieldAttributes.getAnnotation(Expose.class);
                        if (expose != null) {
                            return !expose.deserialize();
                        } else return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {
                        return false;
                    }
                })
                .create();

        @Override
        public <T> T deserialize(@NonNull String serial, @Nullable Class<T> type) {
            return this.gson.fromJson(serial, type);
        }

        @Override
        public <T> String serialize(@NonNull Object object, @Nullable Class<T> type) {
            return this.gson.toJson(object, type == null ? object.getClass() : type);
        }

        @Override
        public <T> String serialize(@NonNull Object object, Type type) {
            return this.gson.toJson(object, type);
        }
    };

    <T> T deserialize(@NonNull String serial, @Nullable Class<T> type);

    <T> String serialize(@NonNull Object object, @Nullable Class<T> type);

    <T> String serialize(@NonNull Object object, Type type);
}
