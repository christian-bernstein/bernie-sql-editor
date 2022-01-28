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

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * todo replace with a better solution.. this one is pathetic.
 *
 * @author Christian Bernstein
 */
public class BernieSystemPrintAdapter extends PrintStream {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");

    public static BernieSystemPrintAdapter instance;

    private final List<Consumer<String>> printHandlers = new ArrayList<>();

    /**
     * Creates a new print stream, without automatic line flushing, with the
     * specified OutputStream. Characters written to the stream are converted
     * to bytes using the platform's default character encoding.
     *
     * @param out The output stream to which values and objects will be
     *            printed
     * @see PrintWriter#PrintWriter(OutputStream)
     */
    public BernieSystemPrintAdapter(OutputStream out) {
        super(out);
    }

    public static BernieSystemPrintAdapter systemInstall() {
        final PrintStream systemStream = System.out;
        final BernieSystemPrintAdapter adapter = new BernieSystemPrintAdapter(systemStream);
        System.setOut(adapter);
        instance = adapter;
        return adapter;
    }

    @SuppressWarnings("all")
    @Override
    public void print(String s) {
        try {
            this.printHandlers.forEach(handler -> handler.accept(s));
        } catch (StackOverflowError e) {
            return;
        }

        assert s != null;
        final String[] lines = s.split("\\n");
        if (lines.length < 2) {
            super.print(this.createLinePrefix() + s);
        } else {
            super.print(this.createLinePrefix() + lines[0] + "\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                super.print(String.format("%s%s%s\n", this.createLinePrefix(), i == lines.length - 1 ? "'- " : "|- ", line));
            }
        }
    }

    public void registerPrintHandler(Consumer<String> printHandler) {
        this.printHandlers.add(printHandler);
    }

    private String createLinePrefix() {
        final StackTraceElement[] trace = new Exception().getStackTrace();
        final StackTraceElement element = trace[3];
        String elementString = element.toString();
        // todo refactor code -> bug width of > width
        final int width = 40;
        if (elementString.length() > width) {
            final int length = elementString.length();
            elementString = ".." + elementString.substring(length - width - 2, length);
        }
        return String.format("%s | %-" + width + "s | ", FORMAT.format(new Date()), elementString);
    }
}
