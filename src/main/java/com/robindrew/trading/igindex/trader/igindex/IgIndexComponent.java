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
import com.robindrew.trading.igindex.platform.IIgIndexSession;
import com.robindrew.trading.igindex.platform.IgIndexCredentials;
import com.robindrew.trading.igindex.platform.IgIndexEnvironment;
import com.robindrew.trading.igindex.platform.IgIndexSession;
import com.robindrew.trading.igindex.platform.IgIndexTradingPlatform;
import com.robindrew.trading.igindex.platform.rest.IIgIndexRestService;
import com.robindrew.trading.igindex.platform.rest.IgIndexRestService;
import com.robindrew.trading.igindex.platform.rest.executor.getmarketnavigation.cache.IMarketNavigationCache;
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
	private static final IProperty<IgIndexEnvironment> propertyEnvironment = new EnumProperty<>(IgIndexEnvironment.class, "igindex.environment");
	private static final IProperty<File> propertyTransactionLogDir = new FileProperty("transaction.log.dir");

	@Override
	protected void startupComponent() throws Exception {
		IMBeanRegistry registry = new AnnotatedMBeanRegistry();

		String apiKey = propertyApiKey.get();
		String username = propertyUsername.get();
		String password = propertyPassword.get();
		IgIndexEnvironment environment = propertyEnvironment.get();
		File transactionLogDir = propertyTransactionLogDir.get();

		IgIndexCredentials credentials = new IgIndexCredentials(apiKey, username, password);

		log.info("Creating Session");
		log.info("Environment: {}", environment);
		log.info("User: {}", credentials.getUsername());
		IgIndexSession session = new IgIndexSession(credentials, environment);
		setDependency(IIgIndexSession.class, session);

		log.info("Creating Session Manager");
		SessionManager sessionManager = new SessionManager(session);
		registry.register(sessionManager);

		log.info("Creating Transaction Log");
		TransactionLog transactionLog = new TransactionLog(transactionLogDir);
		transactionLog.start();

		log.info("Creating REST Service");
		IgIndexRestService rest = new IgIndexRestService(session, transactionLog);
		setDependency(IIgIndexRestService.class, rest);
		setDependency(IMarketNavigationCache.class, rest.getMarketNavigationCache());

		log.info("Creating Trading Platform");
		IgIndexTradingPlatform platform = new IgIndexTradingPlatform(rest);
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
