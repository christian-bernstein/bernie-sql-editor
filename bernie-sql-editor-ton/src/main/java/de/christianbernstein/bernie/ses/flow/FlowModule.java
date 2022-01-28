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

package de.christianbernstein.bernie.ses.flow;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.flow.in.FlowRequestPacketData;
import de.christianbernstein.bernie.ses.flow.out.FlowResponsePacketData;
import de.christianbernstein.bernie.ses.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.ses.user.IUser;
import de.christianbernstein.bernie.shared.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.shared.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.module.Module;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("all")
public class FlowModule implements IFlowModule {

    private static Optional<FlowModule> instance = Optional.empty();

    private static Optional<ITon> ton = Optional.empty();

    @Discoverer(packetID = "FlowRequestPacketData", datatype = FlowRequestPacketData.class, protocols = Constants.centralProtocolName)
    private static final IPacketHandlerBase<FlowRequestPacketData> flowRequestHandler = (data, endpoint, socket, packet, server) -> {
        // Get socket identifier
        final SocketLaneIdentifyingAttachment sli = endpoint.getAttachments().get(SocketLaneIdentifyingAttachment.ATTACHMENT_NAME);
        // Get user data from the session id
        FlowModule.ton.flatMap(ton -> FlowModule.instance).ifPresent(module -> {
            final ITon api = ton.orElseThrow();
            final String name = data.getName();
            final Map<String, Object> parameters = data.getParameters();
            try {
                if (module.hasFlow(name)) {
                    final IUser user = api.getUserFromSessionID(sli.getSessionID());
                    final Map<String, Object> result = module.run(user, name, parameters);
                    // Flow ran as expected
                    endpoint.respond(new FlowResponsePacketData(result, FlowExecutionResultType.OK, null), packet.getId());
                } else {
                    System.err.printf("No flow named '%S' found \n", name);
                    // No flow mapped to the requested name
                    endpoint.respond(new FlowResponsePacketData(Map.of(), FlowExecutionResultType.NO_FLOW, null), packet.getId());
                }
            } catch (final Exception e) {
                e.printStackTrace();
                // While running flow, an exception was thrown
                endpoint.respond(new FlowResponsePacketData(Map.of(), FlowExecutionResultType.ERROR, e), packet.getId());
            }
        });
    };

    private final Map<String, IFlow> flows = new HashMap<>();

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IFlowModule.super.boot(api, module, manager);
        FlowModule.instance = Optional.of(this);
        FlowModule.ton = Optional.of(api);
    }

    @Override
    public void uninstall(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IFlowModule.super.uninstall(api, module, manager);
        FlowModule.instance = Optional.empty();
        FlowModule.ton = Optional.empty();
    }

    @Override
    public IFlowModule registerModule(String name, IFlow flow) {
        this.flows.putIfAbsent(name, flow);
        return this;
    }

    @Override
    public IFlowModule removeModule(String name) {
        this.flows.remove(name);
        return this;
    }

    @Override
    public Map<String, Object> run(IUser user, String name, Map<String, Object> parameter) {
        if (user != null) {
            if (this.hasFlow(name)) {
                return this.flows.get(name).work(ton.orElseThrow(), user, parameter);
            } else {
                throw new NullPointerException(String.format("No flow called '%s' was found", name));
            }
        } else {
            throw new NullPointerException("Cannot run flow without a user object (Got 'null')");
        }
    }

    @Override
    public boolean hasFlow(String name) {
        return this.flows.containsKey(name);
    }

    @Override
    public @NonNull IFlowModule me() {
        return this;
    }
}
