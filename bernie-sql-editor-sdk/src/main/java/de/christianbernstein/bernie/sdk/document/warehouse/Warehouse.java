package de.christianbernstein.bernie.sdk.document.warehouse;

import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.misc.ILifecycle;
import de.christianbernstein.bernie.sdk.misc.ObjectNotationLanguage;
import de.christianbernstein.bernie.sdk.misc.Utils;

import java.io.*;
import java.util.stream.Collectors;

public class Warehouse extends Document implements ILifecycle {

    @Override
    public void start() {
        ILifecycle.super.start();

    }

    @Override
    public void stop() {
        ILifecycle.super.stop();

    }

    public static Warehouse load(final String path) {
        final File file = Utils.loadFile(null, path);
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            final String buffer = reader.lines().collect(Collectors.joining("\n"));
            return ObjectNotationLanguage.YAML.getSerialAdapter().deserialize(buffer, Warehouse.class);
        } catch (final IOException e) {
            e.printStackTrace();
            return new Warehouse();
        }
    }

    public static Warehouse save(final Warehouse warehouse, final String path) {
        final File file = Utils.loadFile(null, path);
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            try {
                final String buffer = ObjectNotationLanguage.YAML.getSerialAdapter().serialize(warehouse, Warehouse.class);
                writer.write(buffer);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return warehouse;
    }
}
