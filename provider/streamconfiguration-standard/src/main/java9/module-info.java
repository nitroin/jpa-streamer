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
module jpastreamer.streamconfiguration.standard {
    requires transitive jpastreamer.streamconfiguration;

    exports com.speedment.jpastreamer.streamconfiguration.standard;

    // Todo: Enable this
    // provides com.speedment.jpastreamer.application.JPAStreamBuilderFactory with com.speedment.jpastreamer.application.standard.StandardJPAStreamBuilderFactory;
}
