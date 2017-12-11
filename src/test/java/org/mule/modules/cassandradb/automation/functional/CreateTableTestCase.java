/**
 * (c) 2003-2017 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.cassandradb.automation.functional;


import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;
import org.junit.After;
import org.junit.Test;
import org.mule.modules.cassandradb.api.CreateTableInput;
import org.mule.modules.cassandradb.automation.util.TestsConstants;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mule.modules.cassandradb.automation.functional.TestDataBuilder.getBasicCreateTableInput;
import static org.mule.modules.cassandradb.automation.functional.TestDataBuilder.getColumns;
import static org.mule.modules.cassandradb.automation.functional.TestDataBuilder.getCompositePrimaryKey;
import static org.mule.modules.cassandradb.automation.util.TestsConstants.TABLE_NAME_1;


public class CreateTableTestCase extends AbstractTestCases {

    @After
    public void tearDown() {
        getCassandraService().dropTable(TestsConstants.TABLE_NAME_1, getKeyspaceFromProperties());
        getCassandraService().dropTable(TestsConstants.TABLE_NAME_2, getKeyspaceFromProperties());
    }

    @Test
    public void testCreateTableWithSuccess() throws Exception {
        CreateTableInput basicCreateTableInput = getBasicCreateTableInput(getColumns(), getKeyspaceFromProperties(), TestsConstants.TABLE_NAME_1);
        assertTrue(createTable(basicCreateTableInput));

        Thread.sleep(SLEEP_DURATION);
        TableMetadata tableMetadata = fetchTableMetadata(getKeyspaceFromProperties(), TABLE_NAME_1);
        assertNotNull(tableMetadata);
    }

    @Test
    public void testCreateTableWithCompositePKWithSuccess() throws Exception {
        CreateTableInput basicCreateTableInput = getBasicCreateTableInput(getCompositePrimaryKey(), getKeyspaceFromProperties(), TestsConstants.TABLE_NAME_2);
        assertTrue(createTable(basicCreateTableInput));
    }

    boolean createTable(CreateTableInput basicCreateTableInput) throws Exception {
        return (boolean) flowRunner("createTable-flow")
                .withPayload(basicCreateTableInput)
                .run()
                .getMessage()
                .getPayload()
                .getValue();
    }
}