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

package com.speedment.jpastreamer.merger.standard.internal.criteria.strategy;

import static com.speedment.jpastreamer.pipeline.intermediate.IntermediateOperationType.DISTINCT;
import static java.util.Objects.requireNonNull;

import com.speedment.jpastreamer.criteria.Criteria;
import com.speedment.jpastreamer.merger.standard.internal.reference.IntermediateOperationReference;
import com.speedment.jpastreamer.merger.standard.internal.tracker.MergingTracker;
import com.speedment.jpastreamer.pipeline.intermediate.IntermediateOperation;
import com.speedment.jpastreamer.pipeline.intermediate.IntermediateOperationType;

public enum DistinctCriteriaModifier implements CriteriaModifier {

    INSTANCE;

    @Override
    public <T> void modifyCriteria(
        final IntermediateOperationReference operationReference,
        final Criteria<T> criteria,
        final MergingTracker mergingTracker
    ) {
        requireNonNull(operationReference);
        requireNonNull(criteria);
        requireNonNull(mergingTracker);

        final IntermediateOperation<?, ?> operation = operationReference.get();

        final IntermediateOperationType operationType = operation.type();

        if (operationType != DISTINCT) {
            return;
        }

        criteria.getQuery().distinct(true);

        mergingTracker.markAsMerged(operationType);
        mergingTracker.markForRemoval(operationReference.index());
    }
}
