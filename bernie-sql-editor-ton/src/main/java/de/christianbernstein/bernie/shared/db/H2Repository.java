package de.christianbernstein.bernie.shared.db;

import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import lombok.Getter;
import lombok.NonNull;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class H2Repository<T, ID extends Serializable> implements IRepository<T, ID> {

    @NonNull
    private final Class<T> type;

    @NonNull
    private final H2RepositoryConfiguration repositoryConfiguration;

    @Nullable
    private SessionFactory sessionFactory;

    @Getter
    private String tableName;

    private Configuration configuration;

    public H2Repository(@NonNull Class<T> type, @NonNull H2RepositoryConfiguration repositoryConfiguration) {
        this.type = type;
        this.repositoryConfiguration = repositoryConfiguration;
        this.updateTableName();
        this.initHibernateConfiguration();
    }

    public void update(UnaryOperator<T> updater, ID id) {
        this.session(session -> {
            T elem = this.get(id);
            session.evict(elem);
            elem = updater.apply(elem);
            session.update(elem);
        });
    }

    public @NonNull SessionFactory getSessionFactory() {
        if (this.sessionFactory == null) {
            try {
                this.sessionFactory = configuration.buildSessionFactory();
            } catch (final Exception e) {
                e.printStackTrace();
                this.shutdown();
            }
        }
        return this.sessionFactory;
    }

    public @NonNull H2Repository<T, ID> shutdown() {
        if (this.sessionFactory != null) {
            this.sessionFactory.close();
        }
        return this;
    }

    @Override
    public @NotNull T save(@NonNull T entity) {
        Transaction transaction;
        try (final Session session = this.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(entity);
            session.flush();
            transaction.commit();
        } catch (final Exception e) {
            e.printStackTrace();
            // if (transaction != null) {
            //     transaction.rollback();
            // }
        }
        return entity;
    }

    @Contract("_ -> this")
    @Override
    @SafeVarargs
    public @NonNull final H2Repository<T, ID> save(@NotNull @NonNull final T... entities) {
        for (final T entity : entities) {
            this.save(entity);
        }
        return this;
    }

    @SuppressWarnings({"unused", "SqlNoDataSourceInspection"})
    @Override
    public void dropData() {
        this.session(session -> session.doWork(connection -> connection.prepareStatement("truncate table %s".replace("%s", this.tableName)).executeUpdate()));
    }

    @Override
    public T get(@NotNull ID id) {
        try (final Session session = this.getSessionFactory().openSession()) {
            return session.load(this.type, id);
        }
    }

    @Override
    public List<T> getAll() {
        @Language("H2")
        @SuppressWarnings("SqlNoDataSourceInspection")
        final String query = "select * from %s";
        return this.nq(query);
    }

    public List<T> filter(Predicate<T> filter) {
        return this.getAll().stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<T> hq(@NonNull @Language("HQL") String hibernateQuery) {
        hibernateQuery = hibernateQuery.replaceAll("%s", this.tableName);
        try (final Session session = this.getSessionFactory().openSession()) {
            return session.createQuery(hibernateQuery, this.type).list();
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<T> nq(@NonNull @Language("H2") String nativeQuery) {
        nativeQuery = nativeQuery.replaceAll("%s", this.tableName);
        try (final Session session = this.getSessionFactory().openSession()) {
            return session.createNativeQuery(nativeQuery, this.type).list();
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    @NonNull
    public H2Repository<T, ID> session(Consumer<Session> action) {
        try (final Session session = this.getSessionFactory().openSession()) {
            action.accept(session);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private void updateTableName() {
        if (this.type.isAnnotationPresent(Table.class)) {
            final Table table = this.type.getAnnotation(Table.class);
            this.tableName = table.name();
        } else if (this.type.isAnnotationPresent(Entity.class)) {
            final Entity entity = this.type.getAnnotation(Entity.class);
            this.tableName = entity.name();
        } else {
            ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, String.format(
                    "Type '%s' isn't annotated with neither @Table not @Entity, falling back to canonical java class name", this.type
            ));
            this.tableName = this.type.getCanonicalName();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void initHibernateConfiguration() {
        this.configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.connection.url", "jdbc:h2:%1%2".replace("%1", this.repositoryConfiguration.getDatabaseDir()).replace("%2", this.repositoryConfiguration.getDatabase()))
                .setProperty("hibernate.connection.username", this.repositoryConfiguration.getUsername())
                .setProperty("hibernate.connection.password", this.repositoryConfiguration.getPassword())

                .setProperty("hibernate.enable_lazy_load_no_trans", "true")

                .addAnnotatedClass(this.type);
        if (this.repositoryConfiguration.getHbm2DDLMode() != HBM2DDLMode.NOT_SET) {
            this.configuration.setProperty("hibernate.hbm2ddl.auto", this.repositoryConfiguration.getHbm2DDLMode().getHbnPropertyVal());
        }
        if (this.type.isAnnotationPresent(H2RepositoryEntity.class)) {
            final H2RepositoryEntity entity = this.type.getAnnotation(H2RepositoryEntity.class);
            for (final Class<?> relatedClass : entity.relatedClasses()) {
                this.configuration.addAnnotatedClass(relatedClass);
            }
        }
        this.repositoryConfiguration.getAnnotatedClasses().forEach(configuration::addAnnotatedClass);
    }
}
