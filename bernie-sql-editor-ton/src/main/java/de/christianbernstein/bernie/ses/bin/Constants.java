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

package de.christianbernstein.bernie.ses.bin;

import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("SpellCheckingInspection")
@UtilityClass
public class Constants {

    public final String centralProtocolName = "main";

    public final String loginProtocolName = "login";

    public final String tonEngineID = "ton";

    public final String rootPackage = "de.christianbernstein.bernie.ses.ton";

    public final String useTonJRAPhase = "inject-ton-api";

    public final String moduleJRAPhase = "module";

    public final String autoEcexJRAPhase = "auto-exec";

    public final String flowJRAPhase = "flow";

    public final String constructJRAPhase = "construct-phase";

    public final String defaultDbInitiatorID = "def-db-initiator";
}
