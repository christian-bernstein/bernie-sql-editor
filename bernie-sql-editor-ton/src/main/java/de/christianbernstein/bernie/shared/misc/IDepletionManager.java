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

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("unused")
public interface IDepletionManager<T> {

    /**
     * Try to register a subject.
     *
     * @param subject     The subjects
     * @param onDepletion A consumer, that gets called if the subjects is depleted
     * @param amount      A generic amount of time
     * @param unit        The unit of 'amount'.
     * @return True if the subject was registered successfully. I.e. if a subject is already scheduled up, it cant be registered twice. So the method return false.
     */
    boolean register(T subject, Consumer<T> onDepletion, long amount, TimeUnit unit);

    Map<T, ScheduledFuture<?>> getSubjects();

    /**
     * Stops the {@link ScheduledExecutorService} and clears all the subjects.
     */
    void stop();

    /**
     * Try to find a matching scheduled subject and remove it.
     *
     * @param subject The object, that should be removed.
     * @return true if an object was actually removed.
     */
    boolean tryRemove(T subject);

    void renew(T subject, long amount, TimeUnit unit);

}
