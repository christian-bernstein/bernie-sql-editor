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

package de.christianbernstein.bernie.sdk.tailwind;

import de.christianbernstein.bernie.sdk.asm.ASMDefaults;
import de.christianbernstein.bernie.sdk.asm.PublicAPIToClassTransformerTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

// TODO: Handle public api edge cases -> IPublicAPI, PublicAPI
// TODO: 04.08.2021 Add optional recompile of pattern classes, if they can be resolved somehow.
@Getter
@ToString
@Accessors(fluent = true)
public class Proteus<Public extends IPublicAPI<Public>> implements IEnhancedProteus<Public> {

    @NonNull
    private final PrivateAPI privateAPI;

    // todo add this to the underlying interface
    @NonNull
    private final ProteusConfig config;

    private Class<? extends Public> publicAPIClass;

    private Class<? extends Public> publicAPIPatternClass;

    private Public publicAPI;

    public Proteus(@NonNull final Class<Public> publicAPIClass, @NonNull final PrivateAPI privateAPI, @NonNull final ProteusConfig config) {
        this.config = config;
        this.publicAPIClass = publicAPIClass;
        this.privateAPI = privateAPI;
        if (config.autoLoadOnConstruct()) {
            this.load();
        }
    }

    /**
     * @see Proteus#Proteus(Class, PrivateAPI, ProteusConfig) Actual constructor logic
     */
    public Proteus(@NonNull final Class<Public> publicAPIClass, @NonNull final PrivateAPI privateAPI) {
        this(publicAPIClass, privateAPI, ProteusConfig.builder().build());
    }

    public Proteus(@NonNull final Public publicAPI, @NonNull final PrivateAPI privateAPI, @NonNull final ProteusConfig config) {
        this.config = config;
        this.publicAPI = publicAPI;
        this.privateAPI = privateAPI;
        if (config.autoLoadOnConstruct()) {
            this.load();
        }
    }

    /**
     * @see Proteus#Proteus(IPublicAPI, PrivateAPI, ProteusConfig) Actual constructor logic
     */
    public Proteus(@NonNull final Public publicAPI, @NonNull final PrivateAPI privateAPI) {
        this(publicAPI, privateAPI, ProteusConfig.builder().build());
    }

    @Override
    public @NonNull IProteus<Public> me() {
        return this;
    }

    @Override
    public @NonNull PrivateAPI internal() {
        return this.privateAPI;
    }

    @Override
    public @NonNull Public external() {
        return this.publicAPI;
    }

    @Override
    public @NonNull IProteus<Public> load() {
        this.loadPrivateAPI();
        if (this.publicAPI != null) {
            // The API is already loaded, make sure, that the API-class is set as well.
            if (this.publicAPIClass == null) {
                try {
                    //noinspection unchecked
                    this.publicAPIClass = (Class<? extends Public>) this.publicAPI.getClass();
                } catch (final ClassCastException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // No API instance present, try to create a new one. (Class invoking or Runtime compiling)
            if (this.publicAPIClass.isInterface()) {
                // The class is just a pattern, it needs to be compiled

                // todo implement lazy load check
                if (this.config().fastboot()) {
                    // Try to load the class, instead of compiling it
                    if (this.checkAvailabilityOfPublicAPIClass()) {
                        // todo load class
                    }
                }

                this.generatePublicAPIClass();
            }
        }
        // todo handle return val of "false"
        this.loadPublicAPI();
        return this;
    }

    private void loadPrivateAPI() {
        final Class<? extends PrivateAPI> privateAPIClass = this.privateAPI.getClass();
        for (final Field field : privateAPIClass.getDeclaredFields()) {
            if (Gate.class.isAssignableFrom(field.getType())) {
                if (field.isAnnotationPresent(GateDefinition.class)) {
                    final GateDefinition definition = field.getAnnotation(GateDefinition.class);
                    try {
                        field.setAccessible(true);
                        final Gate<?, ?> gate = (Gate<?, ?>) field.get(this.privateAPI);
                        this.privateAPI.registerGate(APIGate.builder().id(definition.id()).gate(gate).build());
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean checkAvailabilityOfPublicAPIClass() {
        final String expectedClassName = String.format(this.config().publicAPIClassNamingPattern(), this.publicAPIClass.getSimpleName());
        try {
            Class.forName(expectedClassName, false, ClassLoader.getSystemClassLoader());
        } catch (final ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean loadPublicAPI() {
        // Initiate the public api class
        try {
            final Constructor<? extends Public> constructor = this.publicAPIClass.getConstructor(IProteus.class);
            constructor.setAccessible(true);
            this.publicAPI = constructor.newInstance(this);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        // Set the proteus api variable in the api to instance of this
        this.publicAPI.proteus(this);
        return true;
    }

    @SuppressWarnings("unchecked")
    private void generatePublicAPIClass() {
        final Class<? extends IPublicAPI<?>> transformedPublicAPIClass = Objects.requireNonNull(ASMDefaults.toClassTransformerVersionDynamic.getRecommended())
                .transform(new PublicAPIToClassTransformerTask()
                        .proteus(this)
                        .patternClass(this.publicAPIClass())
                        .packageName(null)
                        .implementationClassName(String.format(this.config().publicAPIClassNamingPattern(), this.publicAPIClass.getSimpleName()))



                        // .classFileLocation("/asm/")
                        .classFileLocation("./asm/")
                );
        this.publicAPIPatternClass = this.publicAPIClass();
        @SuppressWarnings("unchecked")
        final Class<? extends Public> aClass = (Class<? extends Public>) transformedPublicAPIClass;
        this.publicAPIClass = aClass;
    }
}
