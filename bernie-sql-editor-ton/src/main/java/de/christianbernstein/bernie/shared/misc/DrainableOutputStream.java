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

package de.christianbernstein.bernie.shared.misc;

import lombok.NonNull;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Christian Bernstein
 */
public class DrainableOutputStream extends FilterOutputStream {

    private final ByteArrayOutputStream buffer;

    public DrainableOutputStream(@NonNull OutputStream out) {
        super(out);
        this.buffer = new ByteArrayOutputStream();
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
        this.buffer.write(b);
        super.write(b);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        this.buffer.write(b, off, len);
        super.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        this.buffer.write(b);
        super.write(b);
    }

    public byte[] toByteArray() {
        return this.buffer.toByteArray();
    }

    public String toString() {
        return new String(this.buffer.toByteArray());
    }
}
