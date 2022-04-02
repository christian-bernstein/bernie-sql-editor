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

package de.christianbernstein.bernie.sdk.misc;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@Accessors(fluent = true)
@Getter
public class Stopwatch<T> implements IFluently<Stopwatch<T>> {

    private final List<LapEntry<T>> laps = new ArrayList<>();

    private Instant start, end;

    @Override
    public @NonNull Stopwatch<T> me() {
        return this;
    }

    public boolean isStarted() {
        return this.start != null;
    }

    public boolean isRunning() {
        return this.start != null && this.end == null;
    }

    public boolean isFinished() {
        return this.start != null && this.end != null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Stopwatch<T> lap(final T lapData) {
        if (this.isRunning()) {
            final Instant now = Instant.now();
            this.laps.add(new LapEntry<>(
                    now,
                    lapData,
                    Duration.between(this.laps.size() == 0 ? this.start : this.laps.get(this.laps.size() - 1).completionTimestamp(), now))
            );
        }
        return this;
    }

    public Stopwatch<T> start() {
        this.start = Instant.now();
        return this;
    }

    public Stopwatch<T> stop() {
        if (this.isRunning()) {
            this.end = Instant.now();
        }
        return this;
    }

    public Duration sinceStart() {
        if (this.isStarted()) {
            return Duration.between(this.start, Instant.now());
        } else {
            throw new IllegalStateException("Cannot calculate durations without a started (.start()) stopwatch.");
        }
    }

    public Duration duration() {
        if (this.isFinished()) {
            return Duration.between(this.start, this.end);
        } else {
            throw new IllegalStateException("Cannot calculate durations without a started (.start()) and afterwards stopped (.stop()) stopwatch.");
        }
    }

    @Override
    public String toString() {
        return "Stopwatch{" +
                "laps=" + laps +
                ", start=" + start +
                ", end=" + end +
                '}';
    }

    @Data
    public static class LapEntry<T> {

        private final Instant completionTimestamp;

        private final T lapData;

        private final Duration completionDuration;
    }
}
