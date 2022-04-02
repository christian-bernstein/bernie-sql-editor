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

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Christian Bernstein
 */
public class API {

    private final List<APIGate> gates = new ArrayList<>();

    @SuppressWarnings("UnusedReturnValue")
    public API registerGate(@NonNull APIGate gate) {
        if (this.gates.stream().filter(apiGate -> apiGate.getId().equals(gate.getId())).findAny().isEmpty()) {
            this.gates.add(gate);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <In, Out> Gate<In, Out> gate(@NonNull String id) {
        final Optional<APIGate> optional = this.gates.stream().filter(gate -> gate.getId().equals(id)).findAny();
        return optional.map(apiGate -> (Gate<In, Out>) apiGate.getGate()).orElse(null);
    }

    public <In, Out> Out sync(@NonNull String id, @NonNull IAPIGateAction<In, Out, Out> action) {
        // final Stopwatch<String> stopwatch = new Stopwatch<String>().start();
        final Gate<In, Out> gate = this.gate(id);
        // stopwatch.lap("got gate");
        if (gate != null) {
            final APIGateActionContext<In, Out> context = new APIGateActionContext<>();
            action.run(context, gate);
            return context.yielded();
        } else {
            return null;
        }
    }

    public <In, Out> CompletableFuture<Out> async(@NonNull String id, @NonNull IAPIGateAction<In, CompletableFuture<Out>, Out> action) {
        final Gate<In, Out> gate = this.gate(id);
        if (gate != null) {
            final APIGateActionContext<In, CompletableFuture<Out>> context = new APIGateActionContext<>();
            action.run(context, gate);
            return context.yielded();
        } else {
            return null;
        }
    }
}
