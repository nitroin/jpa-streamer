/*
 * JPAstreamer - Express JPA queries with Java Streams
 * Copyright (c) 2020-2022, Speedment, Inc. All Rights Reserved.
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * See: https://github.com/speedment/jpa-streamer/blob/master/LICENSE
 */
package com.speedment.jpastreamer.streamconfiguration;

import com.speedment.jpastreamer.field.Field;
import com.speedment.jpastreamer.projection.Projection;
import com.speedment.jpastreamer.rootfactory.RootFactory;

import jakarta.persistence.criteria.JoinType;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * StreamConfiguration instances are used
 * to specify certain properties of a Stream.
 * <p>
 * Instances are guaranteed to be immutable and
 * therefore inherently thread-safe.
 *
 * @param <T> the entity type
 */
public interface StreamConfiguration<T> {

    /**
     * Returns the entity class that is to appear in
     * a future Stream.
     *
     * @return the entity class that is to appear in
     * a future Stream
     */
    Class<T> entityClass();

    /**
     * Returns the fields that shall be joined in
     * a future stream.
     * <p>
     * Joining fields <em>prevents N + 1 select problems</em>
     * in cases fields in the Set are to be used
     * by stream consumers.
     *
     * @return the fields that shall be joined in
     * a future stream
     */
    Set<JoinConfiguration<T>> joins();

    /**
     * Creates and returns a new StreamConfiguration configured with
     * the provided {@code field} so that it will be
     * eagerly joined when producing elements in the future Stream
     * using {@link JoinType#LEFT}.
     * <p>
     * This prevents the N+1 problem if the field is accessed in
     * elements in the future Stream.
     * </p>
     *
     * @param field to join
     * @return a new StreamConfiguration configured with
     * the provided {@code field} so that it will be
     * eagerly joined when producing elements in the future Stream
     * using {@link JoinType#LEFT}
     */
    default StreamConfiguration<T> joining(final Field<T> field) {
        return joining(field, JoinType.LEFT);
    }

    /**
     * Creates and returns a new StreamConfiguration configured with
     * the provided {@code field} so that it will be
     * eagerly joined when producing elements in the future Stream
     * using the provided {@code joinType}.
     * <p>
     * This prevents the N+1 problem if the field is accessed in
     * elements in the future Stream.
     * </p>
     *
     * @param field to join
     * @return a new StreamConfiguration configured with
     * the provided {@code field} so that it will be
     * eagerly joined when producing elements in the future Stream
     * using the provided {@code joinType}
     */
    StreamConfiguration<T> joining(final Field<T> field, final JoinType joinType);

    /**
     * Returns the projected columns to use when creating entities or
     * {@link Optional#empty()} if no projection should be used.
     * <p>
     * A corresponding entity constructor must exist.
     *
     * @return the projected columns to use when creating entities or
     * {@link Optional#empty()} if no projection should be used
     */
    Optional<Projection<T>> selections();

    /**
     * Selects the projected columns to initialize when creating
     * <em>initial</em> entities in a future stream.
     * <p>
     * If this method is never called, all columns will be selected.
     * <p>
     * Un-selected columns will be set to their default values (e.g. null or 0)
     * <p>
     * A corresponding entity constructor must exist. For example,
     * if a Person with columns {@code int id} and {@code String name}
     * (and potentially many other columns) and the columns {@code id}
     * and {@code name} are used for projection, then the entity
     * Person must have a constructor:
     * <pre>{@code
     *     public Person(int id, String name) {
     *         setId(id);
     *         setName(name);
     *     }
     * }</pre>
     *
     * @return a new StreamConfiguration configured with
     * the provided {@code projection} so that it will use
     * the projected columns to initialize when creating
     * <em>initial</em> entities in a future stream
     */
    StreamConfiguration<T> selecting(final Projection<T> projection);

    /**
     * Returns the map with the query hints that will be configured in
     * a future Stream.
     *
     * @return the map containing all the query hints
     */
    Map<String, Object> hints();

    /**
     * Add a query hint.
     * 
     * <p>
     * This method is useful when you need to customize how a query will be executed
     * by the underlying persistence provider.
     * <p>
     * 
     * @return a new StreamConfiguration configured with
     * the provided {@code hintName} and its {@code value}
     */
    StreamConfiguration<T> withHint(final String hintName, final Object value);

    /**
     * Creates and returns a new StreamConfiguration that can be used
     * to configure streams.
     * <p>
     * The method guarantees that the instances (and subsequent derived
     * instances) are immutable. The method further guarantees that
     * any object obtained from instances (or subsequent derived instances)
     * are immutable or unmodifiable.
     *
     * @param <T>         The element type (type of a class token)
     * @param entityClass a class token for an entity class (annotated with {@code @Entity})
     * @return a new JPAStreamerBuilder
     */
    static <T> StreamConfiguration<T> of(final Class<T> entityClass) {
        return RootFactory
                .getOrThrow(StreamConfigurationFactory.class, ServiceLoader::load)
                .createStreamConfiguration(entityClass);

    }

    interface JoinConfiguration<T> {
        /**
         * Returns the {@link Field} for this JoinConfiguration.
         * <p>
         * The field will be eagerly joined into the initial stream
         * of entities.
         *
         * @return the {@link Field} for this JoinConfiguration
         */
        Field<T> field();

        /**
         * Returns the {@link JoinType} for this JoinConfiguration.
         *
         * @return the {@link JoinType} for this JoinConfiguration
         */
        JoinType joinType();
    }

}
