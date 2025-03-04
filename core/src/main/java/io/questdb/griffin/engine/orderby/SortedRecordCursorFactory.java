/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2022 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.griffin.engine.orderby;

import io.questdb.cairo.AbstractRecordCursorFactory;
import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.RecordSink;
import io.questdb.cairo.sql.RecordCursor;
import io.questdb.cairo.sql.RecordCursorFactory;
import io.questdb.cairo.sql.RecordMetadata;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.engine.RecordComparator;
import org.jetbrains.annotations.NotNull;

public class SortedRecordCursorFactory extends AbstractRecordCursorFactory {
    private final RecordCursorFactory base;
    private final SortedRecordCursor cursor;

    public SortedRecordCursorFactory(
            @NotNull CairoConfiguration configuration,
            @NotNull RecordMetadata metadata,
            @NotNull RecordCursorFactory base,
            @NotNull RecordSink recordSink,
            @NotNull RecordComparator comparator
    ) {
        super(metadata);
        RecordTreeChain chain = new RecordTreeChain(
                metadata,
                recordSink,
                comparator,
                configuration.getSqlSortKeyPageSize(),
                configuration.getSqlSortKeyMaxPages(),
                configuration.getSqlSortValuePageSize(),
                configuration.getSqlSortValueMaxPages()
        );
        this.base = base;
        this.cursor = new SortedRecordCursor(chain);
    }

    @Override
    public RecordCursor getCursor(SqlExecutionContext executionContext) throws SqlException {
        this.cursor.of(base.getCursor(executionContext), executionContext);
        return cursor;
    }

    @Override
    public boolean recordCursorSupportsRandomAccess() {
        return true;
    }

    @Override
    public boolean usesCompiledFilter() {
        return base.usesCompiledFilter();
    }

    @Override
    protected void _close() {
        base.close();
        cursor.close();
    }
}
