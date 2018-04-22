package com.robindrew.trading.igindex.trader.igindex;

import static com.robindrew.common.dependency.DependencyFactory.setDependency;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robindrew.common.mbean.IMBeanRegistry;
import com.robindrew.common.mbean.annotated.AnnotatedMBeanRegistry;
import com.robindrew.common.properties.map.type.EnumProperty;
import com.robindrew.common.properties.map.type.FileProperty;
import com.robindrew.common.properties.map.type.IProperty;
import com.robindrew.common.properties.map.type.StringProperty;
import com.robindrew.common.service.component.AbstractIdleComponent;
import com.robindrew.trading.igindex.platform.IIgSession;
import com.robindrew.trading.igindex.platform.IgCredentials;
import com.robindrew.trading.igindex.platform.IgEnvironment;
import com.robindrew.trading.igindex.platform.IgSession;
import com.robindrew.trading.igindex.platform.IgTradingPlatform;
import com.robindrew.trading.igindex.platform.rest.IIgRestService;
import com.robindrew.trading.igindex.platform.rest.IgRestService;
import com.robindrew.trading.igindex.platform.rest.executor.getmarketnavigation.IMarketNavigationCache;
import com.robindrew.trading.igindex.trader.igindex.connection.ConnectionManager;
import com.robindrew.trading.igindex.trader.igindex.connection.IConnectionManager;
import com.robindrew.trading.igindex.trader.igindex.session.SessionManager;
import com.robindrew.trading.log.TransactionLog;
import com.robindrew.trading.platform.ITradingPlatform;

public class IgIndexComponent extends AbstractIdleComponent {

	private static final Logger log = LoggerFactory.getLogger(IgIndexComponent.class);

	private static final IProperty<String> propertyApiKey = new StringProperty("igindex.api.key");
	private static final IProperty<String> propertyUsername = new StringProperty("igindex.username");
	private static final IProperty<String> propertyPassword = new StringProperty("igindex.password");
	private static final IProperty<IgEnvironment> propertyEnvironment = new EnumProperty<>(IgEnvironment.class, "igindex.environment");
	private static final IProperty<File> propertyTransactionLogDir = new FileProperty("transaction.log.dir");

	@Override
	protected void startupComponent() throws Exception {
		IMBeanRegistry registry = new AnnotatedMBeanRegistry();

		String apiKey = propertyApiKey.get();
		String username = propertyUsername.get();
		String password = propertyPassword.get();
		IgEnvironment environment = propertyEnvironment.get();
		File transactionLogDir = propertyTransactionLogDir.get();

		IgCredentials credentials = new IgCredentials(apiKey, username, password);

		log.info("Creating Session");
		log.info("Environment: {}", environment);
		log.info("User: {}", credentials.getUsername());
		IgSession session = new IgSession(credentials, environment);
		setDependency(IIgSession.class, session);

		log.info("Creating Session Manager");
		SessionManager sessionManager = new SessionManager(session);
		registry.register(sessionManager);

		log.info("Creating Transaction Log");
		TransactionLog transactionLog = new TransactionLog(transactionLogDir);
		transactionLog.start();

		log.info("Creating REST Service");
		IgRestService rest = new IgRestService(session, transactionLog);
		setDependency(IIgRestService.class, rest);
		setDependency(IMarketNavigationCache.class, rest.getMarketNavigationCache());

		log.info("Creating Trading Platform");
		IgTradingPlatform platform = new IgTradingPlatform(rest);
		setDependency(ITradingPlatform.class, platform);

		log.info("Creating Connection manager");
		IConnectionManager connectionManager = new ConnectionManager(rest, platform);
		registry.register(connectionManager);
		setDependency(IConnectionManager.class, connectionManager);

		log.info("Logging in ...");
		connectionManager.login();
	}

	@Override
	protected void shutdownComponent() throws Exception {
		// TODO: Cancel all subscriptions here
	}

}
