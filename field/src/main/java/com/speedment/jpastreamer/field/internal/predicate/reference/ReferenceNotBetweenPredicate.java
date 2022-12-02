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
package com.speedment.jpastreamer.field.internal.predicate.reference;

import com.speedment.jpastreamer.field.trait.HasArg0;
import com.speedment.jpastreamer.field.trait.HasArg1;
import com.speedment.jpastreamer.field.trait.HasReferenceValue;
import com.speedment.jpastreamer.field.internal.predicate.AbstractFieldPredicate;
import com.speedment.jpastreamer.field.predicate.Inclusion;
import com.speedment.jpastreamer.field.predicate.trait.HasInclusion;

import java.util.function.Predicate;

import static com.speedment.jpastreamer.field.predicate.PredicateType.NOT_BETWEEN;
import static java.util.Objects.requireNonNull;

/**
 *
 * @param <ENTITY>  the entity type
 * @param <V>       the value type
 *
 * @author  Per Minborg
 * @since   2.2.0
 */
public final class ReferenceNotBetweenPredicate<ENTITY, V extends Comparable<? super V>>
        extends AbstractFieldPredicate<ENTITY,
        HasReferenceValue<ENTITY, V>>
        implements HasInclusion,
        HasArg0<V>,
        HasArg1<V> {

    private final V start;
    private final V end;
    private final Inclusion inclusion;

    public ReferenceNotBetweenPredicate(
            final HasReferenceValue<ENTITY, V> referenceField,
            final V start,
            final V end,
            final Inclusion inclusion
    ) {
        super(NOT_BETWEEN, referenceField, entityPredicate(referenceField, start, end, inclusion));
        this.start     = start;
        this.end       = end;
        this.inclusion = requireNonNull(inclusion);
    }

    private static <ENTITY, D, V extends Comparable<? super V>> Predicate<ENTITY> entityPredicate(HasReferenceValue<ENTITY, V> referenceField, V start, V end, Inclusion inclusion) {
        return entity -> {
            final V fieldValue = referenceField.get(entity);

            switch (inclusion) {
                case START_EXCLUSIVE_END_EXCLUSIVE :
                    return startExclusiveEndExclusive(start, end, fieldValue);

                case START_EXCLUSIVE_END_INCLUSIVE :
                    return startExclusiveEndInclusive(start, end, fieldValue);

                case START_INCLUSIVE_END_EXCLUSIVE :
                    return startInclusiveEndExclusive(start, end, fieldValue);

                case START_INCLUSIVE_END_INCLUSIVE :
                    return starInclusiveEndInclusive(start, end, fieldValue);

                default : throw new IllegalStateException("Inclusion unknown: " + inclusion);
            }
        };
    }

    private static <V extends Comparable<? super V>> boolean starInclusiveEndInclusive(V start, V end, V fieldValue) {
        if (fieldValue == null) {
            return start == null || end == null;
        } else if (start == null || end == null) {
            return false;
        }
        return !(start.compareTo(fieldValue) <= 0 && end.compareTo(fieldValue) >= 0);
    }

    private static <V extends Comparable<? super V>> boolean startInclusiveEndExclusive(V start, V end, V fieldValue) {
        if (fieldValue == null) {
            return start == null && end != null;
        } else if (start == null || end == null) {
            return false;
        }
        return !(start.compareTo(fieldValue) <= 0 && end.compareTo(fieldValue) > 0);
    }

    private static <V extends Comparable<? super V>> boolean startExclusiveEndInclusive(V start, V end, V fieldValue) {
        if (fieldValue == null) {
            return start != null && end == null;
        } else if (start == null || end == null) {
            return false;
        } else return !(start.compareTo(fieldValue) < 0 && end.compareTo(fieldValue) >= 0);
    }

    private static <V extends Comparable<? super V>> boolean startExclusiveEndExclusive(V start, V end, V fieldValue) {
        if (fieldValue == null) {
            return false;
        } else if (start == null || end == null) {
            return false;
        } else return !(start.compareTo(fieldValue) < 0 && end.compareTo(fieldValue) > 0);
    }

    @Override
    public V get0() {
        return start;
    }

    @Override
    public V get1() {
        return end;
    }

    @Override
    public Inclusion getInclusion() {
        return inclusion;
    }

    @Override
    public ReferenceBetweenPredicate<ENTITY, V> negate() {
        return new ReferenceBetweenPredicate<>(getField(), start, end, inclusion);
    }


}
