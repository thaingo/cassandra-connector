package org.mule.modules.cassandradb.automation.functional;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.modules.cassandradb.automation.util.TestsConstants;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataService;
import org.mule.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import org.mule.runtime.api.metadata.resolving.MetadataResult;

import javax.inject.Inject;
import java.io.File;

import static com.google.common.collect.Iterables.find;
import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mule.modules.cassandradb.automation.functional.TestDataBuilder.deleteRowsFlowName;
import static org.mule.modules.cassandradb.automation.functional.TestDataBuilder.insertFlowName;
import static org.mule.modules.cassandradb.automation.functional.TestDataBuilder.metadataKeyName;
import static org.mule.modules.cassandradb.automation.functional.TestDataBuilder.updateFlowName;
import static org.mule.runtime.api.component.location.Location.builder;
import static org.mule.runtime.api.metadata.MetadataKeyBuilder.newKey;

public class MetadataTestCase extends AbstractTestCases{

    public static final String FAIL_MESSAGE = "No assertions file was found for metadata key =  '%s'. It was created in the file %s. Please move it into src/test/resources/datasense/%s and re-run the test.";
    public static final String PATH_TEMPLATE = "/datasense/%s/%s-%s.json";

    private static Location location;
    @Inject
    protected MetadataService metadataService;
    private File serializedMetadataFile;
    private MetadataKey metadataKey = newKey(metadataKeyName).withDisplayName(metadataKeyName).build();
    @Before
    public void setUpMetadata() throws InterruptedException {
        getCassandraService().createTable(TestDataBuilder.getBasicCreateTableInput(TestDataBuilder.getMetadataColumns(), getCassandraProperties().getKeyspace(), TestsConstants.TABLE_NAME_2));
        //required delay to make sure the setup is ok
        sleep(5000);
    }

    protected File createSerializedMetadataFile(String operation){
        File metadataFile = new File(getClass().getResource("/").getFile(), format(PATH_TEMPLATE, getMetadataCategory(), operation, metadataKey.getId()));
        metadataFile.getParentFile().mkdirs();
        return metadataFile;
    }

    public void assertMetadataContents(File serializedMetadataFile, String inputField) throws Exception {
        MetadataResult<ComponentMetadataDescriptor<OperationModel>> result = metadataService.getOperationMetadata(location, metadataKey);
        assertThat(result.isSuccess(), is(true));
        assertThat(result.getFailures(), hasSize(equalTo(0)));
        JSONObject actualMetadataJson = new JSONObject(find(result.get().getModel().getAllParameterModels(), input -> input.getName().equals(inputField)).getType());
        if (serializedMetadataFile.createNewFile()) {
            write(serializedMetadataFile, actualMetadataJson.toString());
            fail(format(FAIL_MESSAGE, metadataKey.getId(), serializedMetadataFile.getAbsolutePath(), getMetadataCategory()));
        } else {
            assertThat(actualMetadataJson.toMap(), is(new JSONObject(readFileToString(serializedMetadataFile)).toMap()));
        }
    }

    public void testInputMetadata(String flowName, String inputField) throws Exception {
        location = builder().globalName(flowName).addProcessorsPart().addIndexPart(0).build();
        assertMetadataContents(createSerializedMetadataFile(flowName), inputField);
    }

    @Test
    public void testCassandraMetadataResolverInputMetadata() throws Exception {
        testInputMetadata(insertFlowName, "entity");
    }

    @Test
    public void testCassandraOnlyWithFiltersInputMetadata() throws Exception {
        testInputMetadata(deleteRowsFlowName, "payload");
    }

    @Test
    public void testCassadraWithFiltersInputMetadata() throws Exception {
        testInputMetadata(updateFlowName, "entityToUpdate");
    }

    @After
    public void tearDownMetadata(){
        getCassandraService().dropTable(TestsConstants.TABLE_NAME_2, getCassandraProperties().getKeyspace());
    }

    public String getMetadataCategory() {
        return "invokemetadataresolver";
    }
}
