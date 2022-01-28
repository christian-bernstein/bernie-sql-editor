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

package de.christianbernstein.bernie.shared.discovery.websocket;

import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Christian Bernstein
 */
public class ProtocolController {

    private final List<Protocol> baseProtocols = new ArrayList<>();

    private final Map<String, IProtocolFactory> protocols = new HashMap<>();

    private final AtomicReference<String> activeProtocolID = new AtomicReference<>();

    private final AtomicReference<Protocol> activeProtocol = new AtomicReference<>();

    private final Queue<String> protocolChangeQueue = new ArrayDeque<>();

    private final List<IProtocolChangeListener> protocolChangeListeners = new ArrayList<>();

    private boolean performingProtocolChange = false;

    @SuppressWarnings("UnusedReturnValue")
    public ProtocolController addProtocolChangeListener(final IProtocolChangeListener listener) {
        this.protocolChangeListeners.add(listener);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ProtocolController registerBase(@NonNull final Protocol base) {
        this.baseProtocols.add(base);
        return this;
    }

    public ProtocolController register(@NonNull final String protocolID, @NonNull final IProtocolFactory factory) {
        this.protocols.put(protocolID, factory);
        return this;
    }

    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public ProtocolController changeProtocol(@NonNull final String protocolID) {
        this.protocolChangeQueue.add(protocolID);
        if (!this.performingProtocolChange) {
            this.chainUpdateProtocols();
        }
        return this;
    }

    private void chainUpdateProtocols() {
        while (this.protocolChangeQueue.size() > 0) {
            this._updateProtocol(this.protocolChangeQueue.poll());
        }
    }

    private void _updateProtocol(@NonNull final String protocolID) {
        this.performingProtocolChange = true;
        try {
            ConsoleLogger.def().log(
                    ConsoleLogger.LogType.INFO,
                    "Protocol Controller",
                    String.format("Change protocol from '%s' to '%s'", this.activeProtocolID.get(), protocolID)
            );

            // Initiate new protocol instance
            final IProtocolFactory factory = protocols.get(protocolID);
            final Protocol newProto = factory.get(this);
            if (newProto.controller() == null) {
                newProto.controller(this);
            }
            // Close old protocol
            final Protocol oldProto = activeProtocol.get();
            if (oldProto != null) {
                oldProto.close();
            }
            // Activate new protocol
            newProto.init();
            activeProtocol.set(newProto);
            activeProtocolID.set(protocolID);
            // Call all the protocol change listeners
            this.protocolChangeListeners.forEach(func -> func.onChange(oldProto != null ? oldProto.id() : null, newProto.id()));
        } finally {
            this.performingProtocolChange = false;
        }
    }

    public List<Protocol> getBaseProtocols() {
        return baseProtocols;
    }

    @Nullable
    public Protocol getActiveProtocol() {
        return this.activeProtocol.get();
    }

    @Nullable
    public String getActiveProtocolID() {
        return activeProtocolID.get();
    }
}
