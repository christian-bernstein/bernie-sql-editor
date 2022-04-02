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

package de.christianbernstein.bernie.sdk.discovery.websocket;

import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.document.IDocument;
import de.christianbernstein.bernie.sdk.misc.IFluently;
import de.christianbernstein.bernie.sdk.misc.ILambdaNamespace;
import de.christianbernstein.bernie.sdk.misc.LambdaNamespace;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@Accessors(fluent = true)
@SuppressWarnings("UnusedReturnValue")
public class Protocol implements IFluently<Protocol> {

    @Builder.Default
    private final IDocument<?> state = Document.empty();

    @Builder.Default
    private final ILambdaNamespace<IDocument<?>> opcode = new LambdaNamespace<>();

    private final String id;

    private final IProtocolActionHandler initHandler, closeHandler;

    private ProtocolController controller;

    private Object attachment;

    @SuppressWarnings("unchecked")
    public <T> T attachment() {
        return (T) this.attachment;
    }

    @NonNull
    public Protocol attachment(Object attachment) {
        this.attachment = attachment;
        return this;
    }

    @NonNull
    public Protocol init() {
        if (this.initHandler() != null) {
            this.initHandler().handle(this.controller(), this);
        }
        return this;
    }

    @NonNull
    public Protocol close() {
        if (this.closeHandler() != null) {
            this.closeHandler().handle(this.controller(), this);
        }
        return this;
    }

    @Override
    public @NonNull Protocol me() {
        return this;
    }
}
