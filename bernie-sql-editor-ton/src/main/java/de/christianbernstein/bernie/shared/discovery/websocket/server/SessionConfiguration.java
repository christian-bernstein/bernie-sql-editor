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

package de.christianbernstein.bernie.shared.discovery.websocket.server;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * @author Christian Bernstein
 */
@Getter
@Builder
@Accessors(fluent = true)
public class SessionConfiguration {

    public static final SessionConfiguration defaultConfiguration = SessionConfiguration.builder().build();

    // todo convert to Duration
    @NonNull
    @Builder.Default
    private final TimeUnit responseTimeoutUnit = TimeUnit.SECONDS;

    @Builder.Default
    private final long responseTimeoutAmount = 2;

    @Builder.Default
    private final Duration sessionRenewalAmount = Duration.of(10, ChronoUnit.SECONDS);
}
