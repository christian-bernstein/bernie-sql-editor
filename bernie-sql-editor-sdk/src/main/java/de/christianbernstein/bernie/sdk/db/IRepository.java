package de.christianbernstein.bernie.sdk.db;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.List;

public interface IRepository<T, ID extends Serializable> {

    @NonNull
    T save(@NonNull final T entity);

    Object saveObject(@NonNull final Object object);

    @Nullable
    T load(@NonNull final ID id);

    @Nullable
    T get(@NonNull final ID id);

    List<T> getAll();

    List<T> hq(@NonNull final String hibernateQuery);

    List<T> nq(@NonNull final String nativeQuery);

    @NonNull
    @SuppressWarnings("all")
    H2Repository<T, ID> save(@NonNull final T... entities);

    void dropData();
}
