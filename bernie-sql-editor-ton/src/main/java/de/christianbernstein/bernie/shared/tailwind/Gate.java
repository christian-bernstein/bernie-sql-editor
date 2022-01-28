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

package de.christianbernstein.bernie.shared.tailwind;

import de.christianbernstein.bernie.shared.document.Document;
import de.christianbernstein.bernie.shared.document.IDocument;
import de.christianbernstein.bernie.shared.misc.BoolAccumulator;
import de.christianbernstein.bernie.shared.misc.ILink;
import de.christianbernstein.bernie.shared.misc.Tuple;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * // TODO: 02.08.2021 Implement autoclosing executor-service
 *
 * @author Christian Bernstein
 */
@Data
@Builder
@Accessors(fluent = true)
@SuppressWarnings("all")
@NoArgsConstructor
@AllArgsConstructor
public class Gate<In, Out> {

    @NonNull
    @Builder.Default
    private ILink.Static<ExecutorService> asyncExecutorSupplier = new ILink.Static<>(Executors::newSingleThreadExecutor);

    @NonNull
    @Builder.Default
    private ILink<IDocument<?>> defaultParameterSupplier = Document::new;

    @NonNull
    @Builder.Default
    private ILink<In> defaultInputSupplier = () -> null;

    @NonNull
    @Builder.Default
    private ILink<List<IGatekeeper<In>>> inputGatekeeperSupplier = ArrayList::new;

    @NonNull
    @Builder.Default
    private IFactory<Gate<In, Out>, List<IGatekeeper<Out>>> outboundGatekeeperSupplier = gate -> new ArrayList<>();

    @NonNull
    @Builder.Default
    private IFactory<Gate<In, Out>, IBackingService<In, Out>> service = gate -> (in, parameters, gate1) -> null;

    @NonNull
    public CompletableFuture<Out> async(In in, @NonNull IDocument<?> parameters) {
        final CompletableFuture<Out> future = new CompletableFuture<>();
        this.asyncExecutorSupplier.get().execute(() -> {
            // Validate inbounded data
            if (!this.validateGatekeepers(in, this.inputGatekeeperSupplier().get())) {
                future.cancel(true);
            }
            // Execute the de.christianbernstein.bernie.utilities.tailwind.gate service
            Out out = null;
            try {
                out = this.service.produce(this).process(in, parameters, this);
            } catch (final Exception e) {
                future.cancel(true);
            }
            // Validate outbounded data
            if (this.validateGatekeepers(out, this.outboundGatekeeperSupplier().produce(this))) {
                // Return value is valid
                future.complete(out);
            } else {
                // Return value is invalid
                future.cancel(false);
            }
        });

        // TODO: 03.08.2021 Implement configurable shutdown
        if (true) {
            future.thenRun(() -> {
                this.asyncExecutorSupplier.get().shutdown();
                this.asyncExecutorSupplier.modify(executorService -> null);
            });
        }

        return future;
    }

    @NonNull
    public CompletableFuture<Out> async(In in) {
        return this.async(in, this.defaultParameterSupplier.get());
    }

    @NonNull
    public CompletableFuture<Out> async() {
        return this.async(this.defaultInputSupplier.get(), this.defaultParameterSupplier.get());
    }

    public Out sync(In in, @NonNull IDocument<?> parameters) {
        // Validate inbounded data
        if (!this.validateInput(in)) {
            // todo throw something
            System.err.println("Input isn't valid");
            return null;
        }
        // Execute the de.christianbernstein.bernie.utilities.tailwind.gate service
        Out out = null;
        try {
            out = this.service.produce(this).process(in, parameters, this);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
        // Validate outbounded data
        if (this.validateGatekeepers(out, this.outboundGatekeeperSupplier().produce(this))) {
            // Return value is valid
            return out;
        } else {
            // Return value is invalid
            // todo throw something
            return null;
        }
    }

    public Out sync(In in) {
        return this.sync(in, this.defaultParameterSupplier.get());
    }

    public Out sync() {
        return this.sync(this.defaultInputSupplier.get(), this.defaultParameterSupplier.get());
    }

    public boolean validateInput(In in) {
        return BoolAccumulator.builder()
                .switchType(BoolAccumulator.SwitchType.ONE_WAY_TO_FALSE)
                .conditions(this.inputGatekeeperSupplier.get().stream().map(keeper -> (Supplier<Boolean>) () -> keeper.apply(in)).collect(Collectors.toList()))
                .build().get();
    }

    public <T> boolean validateGatekeepers(T data, @NotNull List<IGatekeeper<T>> gatekeepers) {
        return BoolAccumulator.builder()
                .switchType(BoolAccumulator.SwitchType.ONE_WAY_TO_FALSE)
                .conditions(gatekeepers.stream().map(keeper -> (Supplier<Boolean>) () -> keeper.apply(data)).collect(Collectors.toList()))
                .build().get();
    }

    // todo implement
    @RequiredArgsConstructor
    public static class Proxy<ProxyIn, ProxyOut, In, Out> extends Gate<ProxyIn, ProxyOut> {

        @NonNull
        private final Gate<In, Out> gate;

    }

    public static class VxBGate<InX, InY> extends Gate<Tuple.Couple<InX, InY>, Void> {
    }

    public static class BxBGate<InX, InY, OutX, OutY> extends Gate<Tuple.Couple<InX, InY>, Tuple.Couple<OutX, OutY>> {
    }
}
