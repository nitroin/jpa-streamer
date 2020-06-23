package com.speedment.jpastreamer.builder.standard.internal;

import com.speedment.jpastreamer.pipeline.Pipeline;
import com.speedment.jpastreamer.pipeline.intermediate.IntermediateOperation;
import com.speedment.jpastreamer.pipeline.intermediate.IntermediateOperationType;
import com.speedment.jpastreamer.pipeline.terminal.TerminalOperation;
import com.speedment.jpastreamer.renderer.RenderResult;
import com.speedment.jpastreamer.renderer.Renderer;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StreamBuilderTest {

    private static final Factories FACTORIES = InjectedFactories.INSTANCE;

    @Test
    void filter() {
        final Renderer renderer = new MockRenderer();
        Stream<String> builder = new StreamBuilder<>(FACTORIES, String.class, renderer);

        final long count = builder
                .skip(1)
                .limit(1)
                .count();

        System.out.println("count = " + count);

    }

    @Test
    void first() {
        final Renderer renderer = new MockRenderer();
        Stream<String> builder = new StreamBuilder<>(FACTORIES, String.class, renderer);

        final Optional<String> first = builder
                .filter(s -> s.length() > 0)
                .limit(1)
                .findFirst();

        System.out.println("`first` = " + first);

    }

    @Test
    void consumed() {
        assertThrows(IllegalStateException.class, () -> {
            final Renderer renderer = new MockRenderer();
            Stream<String> builder = new StreamBuilder<>(FACTORIES, String.class, renderer);

            final Optional<String> first = builder
                    .filter(s -> s.length() > 0)
                    .limit(1)
                    .findFirst();

            // We have already consumed the Stream so
            // this should not work.
            final Stream<Integer> illegal = builder.map(String::length);

        });
    }

    private static final class MockRenderer implements Renderer {

        private final Supplier<Stream<String>> source = () -> Stream.of("A", "B", "C");

        // Only works for Stream and not IntStream, etc.

        @Override
        public <T> RenderResult<T> render(Pipeline<T> pipeline) {

            System.out.println(pipeline);

            return new MyRenderResult<>(
                    (Stream<T>) replay(source.get(), (Pipeline<String>)pipeline),
                    pipeline.terminatingOperation()
            );
        }

        @Override
        public void close() {
        }

        <T> BaseStream<?, ?> replay(Stream<T> initialStream, Pipeline<T> pipeLine) {
            BaseStream<?, ?> result = initialStream;
            for (IntermediateOperation intermediateOperation:pipeLine.intermediateOperations()) {
                result = (BaseStream<?,?>)intermediateOperation.function().apply(result);
            }
            return result;
        }

    }

    private static final class MyRenderResult<T> implements RenderResult<T> {

        private final Stream<T> stream;
        private final TerminalOperation<?, ?> terminalOperation;

        public MyRenderResult(Stream<T> stream, TerminalOperation<?, ?> terminalOperation) {
            this.stream = stream;
            this.terminalOperation = terminalOperation;
        }


        @Override
        public Stream<T> stream() {
            return stream;
        }

        @Override
        public TerminalOperation<?, ?> terminalOperation() {
            return terminalOperation;
        }
    }


}