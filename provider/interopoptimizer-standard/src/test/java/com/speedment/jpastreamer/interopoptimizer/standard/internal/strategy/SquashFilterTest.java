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
package com.speedment.jpastreamer.interopoptimizer.standard.internal.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.speedment.jpastreamer.pipeline.Pipeline;
import com.speedment.jpastreamer.pipeline.intermediate.IntermediateOperation;

import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class SquashFilterTest extends SquashTest<String, SquashFilter<?>> {

    @Override
    SquashFilter<?> getSquashInstance() {
        return new SquashFilter<>(operationFactory);
    }

    @Override
    Class<String> getEntityClass() {
        return String.class;
    }

    @Override
    protected Stream<PipelineTestCase<String>> pipelines() {
        return Stream.of(
            noFilter(),
            filterN(1), filterN(2), filterN(3), filterN(10), filterN(100),
            filterOther(),
            filter2Other2(),
            filter2OtherFilter2()
        );
    }

    private PipelineTestCase<String> noFilter() {
        final Pipeline<String> noFilter = createPipeline(
            operationFactory.createSkip(1)
        );

        final Pipeline<String> noFilterExpected = createPipeline(
            operationFactory.createSkip(1)
        );

        return new PipelineTestCase<>("No Filter", noFilter, noFilterExpected);
    }

    private PipelineTestCase<String> filterN(int n) {
        IntermediateOperation<?, ?>[] operations = IntStream.range(0, n).mapToObj(i -> operationFactory.createFilter(x -> i % 2 == 0)).toArray(IntermediateOperation[]::new);

        final Pipeline<String> filter = createPipeline(operations);

        final Pipeline<String> filterExpected = createPipeline(operationFactory.createFilter(x -> n == 1));

        return new PipelineTestCase<>("Filter " + n, filter, filterExpected);
    }

    private PipelineTestCase<String> filterOther() {
        final Pipeline<String> filterOther = createPipeline(
            operationFactory.createFilter(x -> true),
            operationFactory.createSkip(1)
        );

        final Pipeline<String> filterOtherExpected = createPipeline(
            operationFactory.createFilter(x -> true),
            operationFactory.createSkip(1)
        );

        return new PipelineTestCase<>("Filter, Other", filterOther, filterOtherExpected);
    }

    private PipelineTestCase<String> filter2Other2() {
        final Pipeline<String> filter2Other2 = createPipeline(
            operationFactory.createFilter(x -> true), operationFactory.createFilter(x -> true),
            operationFactory.createSkip(1), operationFactory.createSkip(1)
        );

        final Pipeline<String> filter2Other2Expected = createPipeline(
            operationFactory.createFilter(x -> true),
            operationFactory.createSkip(1), operationFactory.createSkip(1)
        );

        return new PipelineTestCase<>("Filter 2, Other 2", filter2Other2, filter2Other2Expected);
    }

    private PipelineTestCase<String> filter2OtherFilter2() {
        final Pipeline<String> filter2OtherFilter2 = createPipeline(
            operationFactory.createFilter(x -> true), operationFactory.createFilter(x -> true),
            operationFactory.createLimit(1),
            operationFactory.createFilter(x -> true), operationFactory.createFilter(x -> false)
        );

        final Pipeline<String> filter2OtherFilter2Expected = createPipeline(
            operationFactory.createFilter(x -> true),
            operationFactory.createLimit(1),
            operationFactory.createFilter(x -> false)
        );

        return new PipelineTestCase<>("Filter 2, Other, Filter 2", filter2OtherFilter2, filter2OtherFilter2Expected);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void assertArguments(Object[] expected, Object[] actual) {
        if (expected[0] instanceof Predicate && actual[0] instanceof Predicate) {
            final Predicate expectedPredicate = (Predicate) expected[0];
            final Predicate actualPredicate = (Predicate) actual[0];

            assertEquals(expectedPredicate.test(null), actualPredicate.test(null));
        } else {
            super.assertArguments(expected, actual);
        }
    }
}
