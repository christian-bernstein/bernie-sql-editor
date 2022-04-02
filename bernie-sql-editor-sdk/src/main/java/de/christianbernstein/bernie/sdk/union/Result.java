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

package de.christianbernstein.bernie.sdk.union;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Optional;

/**
 * @author Christian Bernstein
 */
@RequiredArgsConstructor()
public class Result {

    @Getter
    private final ResultState state;

    @Getter
    private final String status;

    @Getter
    protected String type = ResultTypes.BASE.value;

    public static <T extends Result> Optional<T> translate(Result base, Class<T> target) {
        try {
            return Optional.of(target.cast(base));
        } catch (final ClassCastException e) {
            return Optional.empty();
        }
    }

    public boolean isSuccess() {
        return this.state == ResultState.SUCCESS;
    }

    public enum ResultState {
        SUCCESS, WARNING, FAILED, UNKNOWN
    }

    public enum ResultTypes {

        BASE("base"),
        ERROR("error");

        @Getter
        @Setter
        private String value;

        ResultTypes(String value) {
            this.value = value;
        }
    }

    public static class ErrorResult extends Result {

        @Getter
        private final Exception exception;

        public ErrorResult(ResultState state, String status, Exception exception) {
            super(state, status);
            this.exception = exception;
            this.type = ResultTypes.ERROR.name();
        }
    }
}
