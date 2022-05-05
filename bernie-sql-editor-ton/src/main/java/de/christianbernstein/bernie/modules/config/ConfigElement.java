package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.modules.db.DBUtilities;
import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.misc.Utils;
import lombok.*;
import org.intellij.lang.annotations.Language;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@AllArgsConstructor
public class ConfigElement<T> implements IConfigElement<T> {

    @Setter
    private Supplier<IConfig> configSupplier;

    @Getter
    private String elementName;

    private Object value;

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    @Override
    public T get() {
        final AtomicReference<T> val = new AtomicReference<>();

        this.getConfig().getRepository().session(session -> session.doWork(connection -> {
            @Language("H2") final String h2 = "select %s from %s where id = '%s'";
            final ResultSet query = connection.prepareStatement(String.format(h2,
                    this.getElementName(),
                    this.getConfig().getConfigName(),
                    this.getConfig().getAssociatedUserID()
            )).executeQuery();
            final List<Document> documents = DBUtilities.resultSetToList(query);
            final String columnName = this.getElementName().toUpperCase(Locale.ROOT);
            final Document col = documents.stream().filter(document -> document.containsKey(columnName)).findAny().orElseThrow();
            val.set(col.get(columnName));
        }));
        return val.get();
        // return (T) this.value;
    }

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    @Override
    public void update(T newValue) {
        this.getConfig().getRepository().session(session -> session.doWork(connection -> {
            @Language("H2") final String h2 = "update %s set %s = '%s' where id = '%s'";
            connection.prepareStatement(String.format(h2,
                    this.getConfig().getConfigName(),
                    this.getElementName(),
                    newValue,
                    this.getConfig().getAssociatedUserID()
            )).executeUpdate();
        }));
    }

    @Override
    public void update(IConfigElementUpdater<T> updater) {
        this.update(updater.update(this.getConfig(), this, this.get()));
    }

    @Override
    public String getSQlDatatype() {
        return null;
    }

    @Override
    public boolean isUsingSQLDatatypeLength() {
        return false;
    }

    @Override
    public short getSQLDatatypeLength() {
        return 0;
    }

    @Override
    public void setConfig(Supplier<IConfig> configSupplier) {
        this.configSupplier = configSupplier;
    }

    @Override
    public IConfig getConfig() {
        return this.configSupplier.get();
    }
}
