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

package io.questdb.griffin.engine.functions.math;

import io.questdb.griffin.AbstractGriffinTest;
import io.questdb.griffin.SqlException;
import org.junit.Test;

public class FloorFunctionFactoryTest extends AbstractGriffinTest {


    @Test
    public void testDoubleNegative() throws SqlException {
        assertQuery(
                "floor\n" +
                        "-14.0\n",
                "select floor(-13.1)",
                null,
                true,
                true
        );
    }

    @Test
    public void testDoublePositive() throws SqlException {
        assertQuery(
                "floor\n" +
                        "13.0\n",
                "select floor(13.1)",
                null,
                true,
                true
        );
    }

    @Test
    public void testFloatNegative() throws SqlException {
        assertQuery(
                "floor\n" +
                        "-14.0000\n",
                "select floor(-13.1f)",
                null,
                true,
                true
        );
    }

    @Test
    public void testFloatPositive() throws SqlException {
        assertQuery(
                "floor\n" +
                        "13.0000\n",
                "select floor(13.1f)",
                null,
                true,
                true
        );
    }

    @Test
    public void testNaN() throws SqlException {
        assertQuery(
                "floor\n" +
                        "NaN\n",
                "select floor(NaN)",
                null,
                true,
                true
        );
    }
}
