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

package de.christianbernstein.bernie.modules.flow.out;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.modules.flow.FlowExecutionResultType;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Getter
@PacketMeta(dataID = "FlowResponsePacketData", protocol = Constants.centralProtocolName)
@AllArgsConstructor
public class FlowResponsePacketData extends PacketData {

    private final Map<String, Object> results;

    private final FlowExecutionResultType type;

    @Nullable
    private final Exception exception;
}
