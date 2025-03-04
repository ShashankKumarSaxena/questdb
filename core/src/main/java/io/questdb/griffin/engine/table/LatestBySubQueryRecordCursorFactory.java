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

package io.questdb.griffin.engine.table;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.TableUtils;
import io.questdb.cairo.sql.Record;
import io.questdb.cairo.sql.*;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.std.IntHashSet;
import io.questdb.std.IntList;
import io.questdb.std.Misc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LatestBySubQueryRecordCursorFactory extends AbstractTreeSetRecordCursorFactory {
    private final int columnIndex;
    private final Function filter;
    private final Record.CharSequenceFunction func;
    private final RecordCursorFactory recordCursorFactory;
    // this instance is shared between factory and cursor
    // factory will be resolving symbols for cursor and if successful
    // symbol keys will be added to this hash set
    private final IntHashSet symbolKeys = new IntHashSet();

    public LatestBySubQueryRecordCursorFactory(
            @NotNull CairoConfiguration configuration,
            @NotNull RecordMetadata metadata,
            @NotNull DataFrameCursorFactory dataFrameCursorFactory,
            int columnIndex,
            @NotNull RecordCursorFactory recordCursorFactory,
            @Nullable Function filter,
            boolean indexed,
            @NotNull Record.CharSequenceFunction func,
            @NotNull IntList columnIndexes
    ) {
        super(metadata, dataFrameCursorFactory, configuration);
        if (indexed) {
            if (filter != null) {
                this.cursor = new LatestByValuesIndexedFilteredRecordCursor(columnIndex, rows, symbolKeys, null, filter, columnIndexes);
            } else {
                this.cursor = new LatestByValuesIndexedRecordCursor(columnIndex, symbolKeys, null, rows, columnIndexes);
            }
        } else {
            if (filter != null) {
                this.cursor = new LatestByValuesFilteredRecordCursor(columnIndex, rows, symbolKeys, null, filter, columnIndexes);
            } else {
                this.cursor = new LatestByValuesRecordCursor(columnIndex, rows, symbolKeys, null, columnIndexes);
            }
        }
        this.columnIndex = columnIndex;
        this.recordCursorFactory = recordCursorFactory;
        this.filter = filter;
        this.func = func;
    }

    @Override
    public boolean recordCursorSupportsRandomAccess() {
        return true;
    }

    @Override
    protected void _close() {
        super._close();
        recordCursorFactory.close();
        Misc.free(filter);
    }

    @Override
    protected AbstractDataFrameRecordCursor getCursorInstance(
            DataFrameCursor dataFrameCursor,
            SqlExecutionContext executionContext
    ) throws SqlException {
        StaticSymbolTable symbolTable = dataFrameCursor.getSymbolTable(columnIndex);
        symbolKeys.clear();
        try (RecordCursor cursor = recordCursorFactory.getCursor(executionContext)) {
            final Record record = cursor.getRecord();
            while (cursor.hasNext()) {
                int symbolKey = symbolTable.keyOf(func.get(record, 0));
                if (symbolKey != SymbolTable.VALUE_NOT_FOUND) {
                    symbolKeys.add(TableUtils.toIndexKey(symbolKey));
                }
            }
        }

        return super.getCursorInstance(dataFrameCursor, executionContext);
    }
}
