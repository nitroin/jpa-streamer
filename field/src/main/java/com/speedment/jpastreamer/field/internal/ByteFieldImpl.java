/*
 *
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
package com.speedment.jpastreamer.field.internal;

import com.speedment.common.annotation.GeneratedCode;
import com.speedment.jpastreamer.field.ByteField;
import com.speedment.jpastreamer.field.internal.comparator.ByteFieldComparatorImpl;
import com.speedment.jpastreamer.field.internal.predicate.bytes.*;
import com.speedment.runtime.config.identifier.ColumnIdentifier;
import com.speedment.jpastreamer.field.comparator.ByteFieldComparator;
import com.speedment.jpastreamer.field.comparator.NullOrder;
import com.speedment.jpastreamer.field.internal.method.GetByteImpl;
import com.speedment.jpastreamer.field.method.ByteGetter;
import com.speedment.jpastreamer.field.method.ByteSetter;
import com.speedment.jpastreamer.field.method.GetByte;
import com.speedment.jpastreamer.field.predicate.FieldPredicate;
import com.speedment.jpastreamer.field.predicate.Inclusion;
import com.speedment.jpastreamer.field.predicate.SpeedmentPredicate;
import com.speedment.runtime.typemapper.TypeMapper;

import java.util.Collection;

import static com.speedment.jpastreamer.field.internal.util.CollectionUtil.collectionToSet;
import static java.util.Objects.requireNonNull;

/**
 * Default implementation of the {@link ByteField}-interface.
 * 
 * Generated by com.speedment.sources.pattern.FieldImplPattern
 * 
 * @param <ENTITY> entity type
 * @param <D>      database type
 * 
 * @author Emil Forslund
 * @since  3.0.0
 */
@GeneratedCode(value = "Speedment")
public final class ByteFieldImpl<ENTITY, D> implements ByteField<ENTITY, D> {
    
    private final ColumnIdentifier<ENTITY> identifier;
    private final GetByte<ENTITY, D> getter;
    private final ByteSetter<ENTITY> setter;
    private final TypeMapper<D, Byte> typeMapper;
    private final boolean unique;
    private final String tableAlias;
    
    public ByteFieldImpl(
            ColumnIdentifier<ENTITY> identifier,
            ByteGetter<ENTITY> getter,
            ByteSetter<ENTITY> setter,
            TypeMapper<D, Byte> typeMapper,
            boolean unique) {
        this.identifier = requireNonNull(identifier);
        this.getter     = new GetByteImpl<>(this, getter);
        this.setter     = requireNonNull(setter);
        this.typeMapper = requireNonNull(typeMapper);
        this.unique     = unique;
        this.tableAlias = identifier.getTableId();
    }
    
    private ByteFieldImpl(
            ColumnIdentifier<ENTITY> identifier,
            ByteGetter<ENTITY> getter,
            ByteSetter<ENTITY> setter,
            TypeMapper<D, Byte> typeMapper,
            boolean unique,
            String tableAlias) {
        this.identifier = requireNonNull(identifier);
        this.getter     = new GetByteImpl<>(this, getter);
        this.setter     = requireNonNull(setter);
        this.typeMapper = requireNonNull(typeMapper);
        this.unique     = unique;
        this.tableAlias = requireNonNull(tableAlias);
    }
    
    @Override
    public ColumnIdentifier<ENTITY> identifier() {
        return identifier;
    }
    
    @Override
    public ByteSetter<ENTITY> setter() {
        return setter;
    }
    
    @Override
    public GetByte<ENTITY, D> getter() {
        return getter;
    }
    
    @Override
    public TypeMapper<D, Byte> typeMapper() {
        return typeMapper;
    }
    
    @Override
    public boolean isUnique() {
        return unique;
    }
    
    @Override
    public String tableAlias() {
        return tableAlias;
    }
    
    @Override
    public ByteField<ENTITY, D> tableAlias(String tableAlias) {
        requireNonNull(tableAlias);
        return new ByteFieldImpl<>(identifier, getter, setter, typeMapper, unique, tableAlias);
    }
    
    @Override
    public ByteFieldComparator<ENTITY, D> comparator() {
        return new ByteFieldComparatorImpl<>(this);
    }
    
    @Override
    public ByteFieldComparator<ENTITY, D> reversed() {
        return comparator().reversed();
    }
    
    @Override
    public ByteFieldComparator<ENTITY, D> comparatorNullFieldsFirst() {
        return comparator();
    }
    
    @Override
    public NullOrder getNullOrder() {
        return NullOrder.LAST;
    }
    
    @Override
    public boolean isReversed() {
        return false;
    }
    
    @Override
    public FieldPredicate<ENTITY> equal(Byte value) {
        return new ByteEqualPredicate<>(this, value);
    }
    
    @Override
    public FieldPredicate<ENTITY> greaterThan(Byte value) {
        return new ByteGreaterThanPredicate<>(this, value);
    }
    
    @Override
    public FieldPredicate<ENTITY> greaterOrEqual(Byte value) {
        return new ByteGreaterOrEqualPredicate<>(this, value);
    }
    
    @Override
    public FieldPredicate<ENTITY> between(
            Byte start,
            Byte end,
            Inclusion inclusion) {
        return new ByteBetweenPredicate<>(this, start, end, inclusion);
    }
    
    @Override
    public FieldPredicate<ENTITY> in(Collection<Byte> values) {
        return new ByteInPredicate<>(this, collectionToSet(values));
    }
    
    @Override
    public SpeedmentPredicate<ENTITY> notEqual(Byte value) {
        return new ByteNotEqualPredicate<>(this, value);
    }
    
    @Override
    public SpeedmentPredicate<ENTITY> lessOrEqual(Byte value) {
        return new ByteLessOrEqualPredicate<>(this, value);
    }
    
    @Override
    public SpeedmentPredicate<ENTITY> lessThan(Byte value) {
        return new ByteLessThanPredicate<>(this, value);
    }
    
    @Override
    public SpeedmentPredicate<ENTITY> notBetween(
            Byte start,
            Byte end,
            Inclusion inclusion) {
        return new ByteNotBetweenPredicate<>(this, start, end, inclusion);
    }
    
    @Override
    public SpeedmentPredicate<ENTITY> notIn(Collection<Byte> values) {
        return new ByteNotInPredicate<>(this, collectionToSet(values));
    }
}