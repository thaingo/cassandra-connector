package org.mule.modules.cassandradb.internal.metadata;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.modules.cassandradb.internal.config.CassandraConfig;
import org.mule.modules.cassandradb.internal.connection.CassandraConnection;
import org.mule.modules.cassandradb.internal.service.CassandraServiceImpl;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mule.metadata.java.api.JavaTypeLoader.JAVA;
import static org.mule.runtime.api.metadata.MetadataKeyBuilder.newKey;

public class MetadataRetriever {

    private static final Logger logger = LoggerFactory.getLogger(MetadataRetriever.class);
    private CassandraConfig config;
    private CassandraConnection connection;

    public MetadataRetriever(CassandraConfig config, CassandraConnection connection){
        this.config = config;
        this.connection = connection;
    }

    public Set<MetadataKey> getMetadataKeys() throws MetadataResolvingException, ConnectionException {
        logger.info("Retrieving metadata keys...");
        return new CassandraServiceImpl(config, connection).getTableNamesFromKeyspace(connection.getCassandraSession().getLoggedKeyspace())
                .stream()
                .map(key -> newKey(key).build())
                .collect(toSet());
    }

    public MetadataType getMetadata(String key){
        logger.info("Retrieving input metadata for the key: {}", key);
        TableMetadata tableMetadata = fetchTableMetadata(connection.getCassandraSession().getLoggedKeyspace(), key);
        BaseTypeBuilder builder = new BaseTypeBuilder(JAVA);

        //build the metadata
        if (tableMetadata != null && tableMetadata.getColumns() != null) {
            for (ColumnMetadata column : tableMetadata.getColumns()) {
                addMetadataField(builder, builder.objectType().id(key), column.getName(), column.getType());
            }
        }

        return builder.build();
    }

    protected ObjectTypeBuilder addMetadataField(BaseTypeBuilder builder, ObjectTypeBuilder typeBuilder, String key, DataType type) {
        switch (type.getName()) {
            case UUID:
            case TIMEUUID:
            case TEXT:
            case VARCHAR:
            case ASCII:
            case INET:
            case FLOAT:
            case DOUBLE:
            case INT:
            case VARINT:
            case SMALLINT:
            case TINYINT:
            case DECIMAL:
            case BIGINT:
                typeBuilder(typeBuilder, key).numberType();
            case BOOLEAN:
                typeBuilder(typeBuilder, key).booleanType();
            case DATE:
                typeBuilder(typeBuilder, key).dateType();
            case TIMESTAMP:
                typeBuilder(typeBuilder, key).dateTimeType();
            case TIME:
                typeBuilder(typeBuilder, key).timeType();
            case LIST:
            case SET:
                typeBuilder(typeBuilder, key).arrayType().of(addMetadataField(builder, builder.objectType().id(key), key, type.getTypeArguments().get(0))).build();
            default:
                typeBuilder(typeBuilder, key).objectType();
            return typeBuilder;
        }
    }

    public BaseTypeBuilder typeBuilder(ObjectTypeBuilder builder, String key){
        return builder.addField().key(key).value();
    }

    public TableMetadata fetchTableMetadata(String keyspaceUsed, String tableName) {
        if (isNotBlank(tableName)) {
            KeyspaceMetadata ksMetadata = connection.getCluster().getMetadata().getKeyspace(keyspaceUsed);
            if (ksMetadata != null) {
                return ksMetadata.getTable(tableName);
            }
        }
        return null;
    }
}
