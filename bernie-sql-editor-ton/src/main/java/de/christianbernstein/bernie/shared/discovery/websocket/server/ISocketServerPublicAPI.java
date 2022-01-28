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


import de.christianbernstein.bernie.shared.tailwind.Bridge;
import de.christianbernstein.bernie.shared.tailwind.IPublicAPI;

public interface ISocketServerPublicAPI extends IPublicAPI<ISocketServerPublicAPI> {

    @Bridge("on-message")
    void onMessage(OnMessageSocketContext ctx);

    @Bridge("on-error")
    void onError(OnErrorSocketContext ctx);

    @Bridge("on-open")
    void onOpen(OnOpenSocketContext ctx);

    @Bridge("on-stop")
    void onStop(OnStopSocketContext ctx);

    @Bridge("on-start")
    void onStart();

}
