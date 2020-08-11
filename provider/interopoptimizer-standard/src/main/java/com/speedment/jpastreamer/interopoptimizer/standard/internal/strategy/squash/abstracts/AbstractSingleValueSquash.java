/*
 * Copyright (c) 2006-2020, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.speedment.jpastreamer.interopoptimizer.standard.internal.strategy.squash.abstracts;

import com.speedment.jpastreamer.interopoptimizer.standard.internal.strategy.squash.SingleValueSquash;
import com.speedment.jpastreamer.pipeline.Pipeline;
import com.speedment.jpastreamer.pipeline.intermediate.IntermediateOperation;

import java.util.LinkedList;

public abstract class AbstractSingleValueSquash<S> implements SingleValueSquash<S> {

    @Override
    @SuppressWarnings("unchecked")
    public <T> Pipeline<T> optimize(final Pipeline<T> pipeline) {
        S result = initialValue();

        final LinkedList<IntermediateOperation<?, ?>> intermediateOperations = pipeline.intermediateOperations();

        for (int i = intermediateOperations.size() - 1; i >= 0; i--) {
            final IntermediateOperation<?, ?> intermediateOperation = intermediateOperations.get(i);

            if (intermediateOperation.type() == operationType()) {
                if (intermediateOperation.arguments().length == 0) {
                    if (result != checkValue()) {
                        final IntermediateOperation<?, ?> newOperation = operationProvider()
                                .apply(result);
                        intermediateOperations.add(i + 1, newOperation);

                        result = resetValue();
                    }
                    continue;
                }

                if (valueClass().isAssignableFrom(intermediateOperation.arguments()[0].getClass())) {
                    S value = (S) intermediateOperation.arguments()[0];
                    result = squash().apply(value, result);

                    intermediateOperations.remove(i);
                }

                continue;
            }

            if (result != checkValue()) {
                final IntermediateOperation<?, ?> newOperation = operationProvider().apply(result);
                intermediateOperations.add(i + 1, newOperation);

                result = resetValue();
            }
        }

        if (result != checkValue()) {
            final IntermediateOperation<?, ?> newOperation = operationProvider().apply(result);
            intermediateOperations.addFirst(newOperation);
        }

        return pipeline;
    }
}
