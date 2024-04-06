/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.examples.java.basics;

import org.apache.flink.table.examples.utils.ExampleOutputTestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/** Test for Java {@link StreamSQLExample}. */
class StreamSQLExampleITCase extends ExampleOutputTestBase {
    private static Logger log = LoggerFactory.getLogger(StreamSQLExampleITCase.class);

    @Test
    void testExample() throws Exception {
        StreamSQLExample.main(new String[0]);
        final String consoleOutput = getOutputString();
        log.info(consoleOutput);
        assertThat(consoleOutput)
                .contains("Order{user=1, product='beer', amount=3}")
                .contains("Order{user=4, product='beer', amount=1}")
                .contains("Order{user=1, product='diaper', amount=4}");
    }
}
