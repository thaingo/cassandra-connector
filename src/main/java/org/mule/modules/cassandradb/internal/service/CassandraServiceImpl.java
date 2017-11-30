package org.mule.modules.cassandradb.internal.service;

import com.datastax.driver.core.Session;
import org.apache.commons.lang3.StringUtils;
import org.mule.connectors.commons.template.service.DefaultConnectorService;
import org.mule.modules.cassandradb.api.CreateTableInput;
import org.mule.modules.cassandradb.internal.config.CassandraConfig;
import org.mule.modules.cassandradb.internal.connection.CassandraConnection;
import org.mule.modules.cassandradb.api.CreateKeyspaceInput;
import org.mule.modules.cassandradb.internal.util.builders.HelperStatements;


public class CassandraServiceImpl extends DefaultConnectorService<CassandraConfig, CassandraConnection> implements CassandraService{

    public CassandraServiceImpl(CassandraConfig config, CassandraConnection connection) {
        super(config, connection);
    }

    @Override
    public boolean createKeyspace(CreateKeyspaceInput input) {
        String queryString = HelperStatements.createKeyspaceStatement(input).getQueryString();
        return getCassandraSession().execute(queryString).wasApplied();
    }

    @Override
    public boolean dropKeyspace(String keyspaceName) {
        String queryString = HelperStatements.dropKeyspaceStatement(keyspaceName).getQueryString();
        return getCassandraSession().execute(queryString).wasApplied();
    }

    @Override
    public boolean createTable(CreateTableInput input) {
        String keyspaceName = StringUtils.isNotBlank(input.getKeyspaceName()) ? input.getKeyspaceName() : getCassandraSession().getLoggedKeyspace();
        String queryString = HelperStatements.createTable(keyspaceName, input).getQueryString();
        return getCassandraSession().execute(queryString).wasApplied();
    }

    private Session getCassandraSession() {
        return getConnection().getCassandraSession();
    }
}
