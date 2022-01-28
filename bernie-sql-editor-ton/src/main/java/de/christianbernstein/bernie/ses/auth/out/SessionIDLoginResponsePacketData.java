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

package de.christianbernstein.bernie.ses.auth.out;

import de.christianbernstein.bernie.ses.auth.SessionIDCheckResultType;
import de.christianbernstein.bernie.ses.user.UserProfileData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
@Getter
@ToString
@PacketMeta(dataID = "SessionIDLoginResponsePacketData", protocol = "login")
@AllArgsConstructor
public class SessionIDLoginResponsePacketData extends PacketData {

    private final SessionIDCheckResultType type;

    @Nullable
    private final UserProfileData profileData;

}
