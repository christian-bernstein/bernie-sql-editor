package de.christianbernstein.bernie.modules.config;

import org.intellij.lang.annotations.Language;

/**
 * @author Christian Bernstein
 */
public class ConfigElement<T> implements IConfigElement<T> {

    @Override
    public IConfig getConfig() {
        return null;
    }

    @Override
    public String getElementName() {
        return null;
    }

    @Override
    public T get() {
        return null;
    }

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    @Override
    public void update(T newValue) {
        this.getConfig().getRepository().session(session -> session.doWork(connection -> {
            @Language("H2") final String h2 = "update %s set %s = '%s' where id = %s";
            connection.prepareStatement(String.format(h2, this.getConfig().getConfigName(), this.getElementName(), newValue, this.getConfig().getAssociatedUserID())).executeUpdate();
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
}
