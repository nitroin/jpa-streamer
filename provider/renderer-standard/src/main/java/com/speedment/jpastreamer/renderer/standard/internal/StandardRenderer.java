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
package com.speedment.jpastreamer.renderer.standard.internal;

import static java.util.Objects.requireNonNull;

import com.speedment.jpastreamer.criteria.Criteria;
import com.speedment.jpastreamer.criteria.CriteriaFactory;
import com.speedment.jpastreamer.interopoptimizer.IntermediateOperationOptimizerFactory;
import com.speedment.jpastreamer.merger.CriteriaMerger;
import com.speedment.jpastreamer.merger.MergerFactory;
import com.speedment.jpastreamer.merger.QueryMerger;
import com.speedment.jpastreamer.pipeline.Pipeline;
import com.speedment.jpastreamer.pipeline.intermediate.IntermediateOperation;
import com.speedment.jpastreamer.pipeline.terminal.TerminalOperationType;
import com.speedment.jpastreamer.projection.Projection;
import com.speedment.jpastreamer.renderer.RenderResult;
import com.speedment.jpastreamer.renderer.Renderer;
import com.speedment.jpastreamer.rootfactory.RootFactory;
import com.speedment.jpastreamer.streamconfiguration.StreamConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

final class StandardRenderer implements Renderer {

    private final EntityManager entityManager;
    private final CriteriaFactory criteriaFactory;

    private final IntermediateOperationOptimizerFactory intermediateOperationOptimizerFactory;

    private final MergerFactory mergerFactory;

    StandardRenderer(final EntityManagerFactory entityManagerFactory) {
        this(entityManagerFactory::createEntityManager); 
    }

    StandardRenderer(final Supplier<EntityManager> entityManagerSupplier) {
        this.entityManager = requireNonNull(entityManagerSupplier).get();
        this.criteriaFactory = RootFactory.getOrThrow(CriteriaFactory.class, ServiceLoader::load);
        this.intermediateOperationOptimizerFactory = RootFactory.getOrThrow(IntermediateOperationOptimizerFactory.class, ServiceLoader::load);
        this.mergerFactory = RootFactory.getOrThrow(MergerFactory.class, ServiceLoader::load);
    }
    
    StandardRenderer(final EntityManager entityManager) {
        this.entityManager = entityManager; 
        this.criteriaFactory = RootFactory.getOrThrow(CriteriaFactory.class, ServiceLoader::load);
        this.intermediateOperationOptimizerFactory = RootFactory.getOrThrow(IntermediateOperationOptimizerFactory.class, ServiceLoader::load);
        this.mergerFactory = RootFactory.getOrThrow(MergerFactory.class, ServiceLoader::load);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, T, S extends BaseStream<T, S>> RenderResult<E, T, S> render(final Pipeline<E> pipeline, final StreamConfiguration<E> streamConfiguration) {
        optimizePipeline(pipeline);

        final Class<E> entityClass = pipeline.root();

        final CriteriaMerger criteriaMerger = mergerFactory.createCriteriaMerger();
        final QueryMerger queryMerger = mergerFactory.createQueryMerger();

        final Criteria<E, E> criteria = criteriaFactory.createCriteria(entityManager, entityClass);
        criteria.getRoot().alias(pipeline.root().getSimpleName());

        // Makes SonarType happy:
        final Optional<Projection<E>> oSelections = streamConfiguration.selections();
        if (oSelections.isPresent()) {
            final Projection<E> projection = oSelections.get();
            final Path<?>[] columns = projection.fields().stream().map(field -> criteria.getRoot().get(field.columnName())).toArray(Path[]::new);
            final CompoundSelection<E> selection = criteria.getBuilder().construct(projection.entityClass(), columns);

            criteria.getQuery().select(selection);
        } else {
            criteria.getQuery().select(criteria.getRoot());
        }

        streamConfiguration.joins()
                .forEach(joinConfiguration -> criteria.getRoot().fetch(joinConfiguration.field().columnName(), joinConfiguration.joinType()));

        criteriaMerger.merge(pipeline, criteria);

        if (pipeline.terminatingOperation().type() == TerminalOperationType.COUNT && pipeline.intermediateOperations().isEmpty()) {
            final Criteria<E, Long> countCriteria = createCountCriteria(criteria);

            final TypedQuery<Long> typedQuery = entityManager.createQuery(countCriteria.getQuery());

            countCriteria.getQueryParameters().forEach(
                queryParameter -> typedQuery.setParameter(queryParameter.getParameterExpression(), queryParameter.getValue())
            );

            return (RenderResult<E, T, S>) new StandardRenderResult<>(
                    entityClass,
                    typedQuery.getResultStream(),
                    pipeline.terminatingOperation()
            );
        }

        final TypedQuery<E> typedQuery = entityManager.createQuery(criteria.getQuery());

        criteria.getQueryParameters().forEach(
            queryParameter -> typedQuery.setParameter(queryParameter.getParameterExpression(), queryParameter.getValue())
        );

        streamConfiguration.hints().forEach((hintName, value) -> typedQuery.setHint(hintName, value));

        queryMerger.merge(pipeline, typedQuery);

        final Stream<E> baseStream = typedQuery.getResultStream();
        final S replayed = replay(baseStream, pipeline);

        return new StandardRenderResult<>(
                entityClass,
                replayed,
                pipeline.terminatingOperation()
        );
    }

    private <T> Criteria<T, Long> createCountCriteria(final Criteria<T, T> criteria) {
        final CriteriaQuery<T> criteriaQuery = criteria.getQuery();

        final Criteria<T, Long> countCriteria = criteriaFactory.createCriteria(
            entityManager,
            criteriaQuery.getResultType(),
            Long.class
        );

        criteria.getQueryParameters().forEach(countCriteria::addQueryParameter);

        countCriteria.getRoot().alias(criteria.getRoot().getAlias());

        final CriteriaQuery<Long> countQuery = countCriteria.getQuery();

        countQuery.select(countCriteria.getBuilder().count(countCriteria.getRoot()));

        if (criteriaQuery.getRestriction() != null) {
            countQuery.where(criteriaQuery.getRestriction());
        }

        countQuery.distinct(criteriaQuery.isDistinct());
        countQuery.orderBy(criteria.getQuery().getOrderList());

        return countCriteria;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <E, T, S extends BaseStream<T, S>> S replay(final Stream<E> stream, final Pipeline<E> pipeline) {
        return pipeline.intermediateOperations().stream()
                .sequential()
                .reduce(
                        (S) stream,
                        (S s, IntermediateOperation io) -> (S) io.function().apply(s),
                        (a, b) -> a
                );

        /*
        S decorated = (S) stream;
        for (IntermediateOperation intermediateOperation : pipeline.intermediateOperations()) {
            decorated = (S) intermediateOperation.function().apply(decorated);
        }
        return decorated;
        */
    }

    private <T> void optimizePipeline(final Pipeline<T> pipeline) {
        intermediateOperationOptimizerFactory.stream().forEach(intermediateOperationOptimizer -> intermediateOperationOptimizer.optimize(pipeline));
    }

    @Override
    public void close() {
        entityManager.close();
    }
}
