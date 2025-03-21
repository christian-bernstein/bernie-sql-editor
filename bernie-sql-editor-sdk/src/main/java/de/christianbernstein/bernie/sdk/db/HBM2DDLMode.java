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

package de.christianbernstein.bernie.sdk.db;

import lombok.Getter;

/**
 * @author Christian Bernstein
 */
@Getter
public enum HBM2DDLMode {

    CREATE("create"), UPDATE("update"), NOT_SET("");

    private final String hbnPropertyVal;

    HBM2DDLMode(String hbnPropertyVal) {
        this.hbnPropertyVal = hbnPropertyVal;
    }
}
