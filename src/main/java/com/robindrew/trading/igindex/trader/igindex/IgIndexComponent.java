package com.robindrew.trading.igindex.trader.igindex;

import static com.robindrew.common.dependency.DependencyFactory.setDependency;
import static com.robindrew.trading.provider.TradeDataProvider.FXCM;
import static com.robindrew.trading.provider.TradeDataProvider.HISTDATA;
import static com.robindrew.trading.provider.TradeDataProvider.IGINDEX;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robindrew.common.mbean.IMBeanRegistry;
import com.robindrew.common.mbean.annotated.AnnotatedMBeanRegistry;
import com.robindrew.common.properties.map.type.EnumProperty;
import com.robindrew.common.properties.map.type.IProperty;
import com.robindrew.common.properties.map.type.StringProperty;
import com.robindrew.common.service.component.AbstractIdleComponent;
import com.robindrew.trading.igindex.trader.igindex.connection.ConnectionManager;
import com.robindrew.trading.igindex.trader.igindex.connection.IConnectionManager;
import com.robindrew.trading.igindex.trader.igindex.session.SessionManager;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.price.candle.format.pcf.source.IPcfSourceManager;
import com.robindrew.trading.price.candle.format.pcf.source.PcfSourceHistoryService;
import com.robindrew.trading.price.candle.format.pcf.source.file.PcfFileManager;
import com.robindrew.trading.price.history.IHistoryService;
import com.robindrew.trading.provider.ITradeDataProviderSet;
import com.robindrew.trading.provider.TradeDataProviderSet;
import com.robindrew.trading.provider.igindex.platform.IIgSession;
import com.robindrew.trading.provider.igindex.platform.IgCredentials;
import com.robindrew.trading.provider.igindex.platform.IgEnvironment;
import com.robindrew.trading.provider.igindex.platform.IgSession;
import com.robindrew.trading.provider.igindex.platform.IgTradingPlatform;
import com.robindrew.trading.provider.igindex.platform.rest.IIgRestService;
import com.robindrew.trading.provider.igindex.platform.rest.IgRestService;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarketnavigation.IMarketNavigationCache;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarketnavigation.MarketNavigationCache;

public class IgIndexComponent extends AbstractIdleComponent {

	private static final Logger log = LoggerFactory.getLogger(IgIndexComponent.class);

	private static final IProperty<String> propertyApiKey = new StringProperty("igindex.api.key");
	private static final IProperty<String> propertyUsername = new StringProperty("igindex.username");
	private static final IProperty<String> propertyPassword = new StringProperty("igindex.password");
	private static final IProperty<String> propertyHistoricPricesDir = new StringProperty("historic.prices.directory");
	private static final IProperty<IgEnvironment> propertyEnvironment = new EnumProperty<>(IgEnvironment.class, "igindex.environment");

	@Override
	protected void startupComponent() throws Exception {
		IMBeanRegistry registry = new AnnotatedMBeanRegistry();

		String apiKey = propertyApiKey.get();
		String username = propertyUsername.get();
		String password = propertyPassword.get();
		String historicPriceDir = propertyHistoricPricesDir.get();
		IgEnvironment environment = propertyEnvironment.get();

		IgCredentials credentials = new IgCredentials(apiKey, username, password);

		log.info("Creating Session", environment);
		log.info("Environment: {}", environment);
		log.info("User: {}", credentials.getUsername());
		IgSession session = new IgSession(credentials, environment);
		setDependency(IIgSession.class, session);

		log.info("Creating Account Manager");
		SessionManager sessionManager = new SessionManager(session);
		registry.register(sessionManager);

		log.info("Creating REST Service");
		IMarketNavigationCache marketNavigationCache = new MarketNavigationCache();
		IIgRestService rest = new IgRestService(session, marketNavigationCache);
		setDependency(IIgRestService.class, rest);
		setDependency(IMarketNavigationCache.class, marketNavigationCache);

		log.info("Creating Trading Platform");
		IgTradingPlatform platform = new IgTradingPlatform(rest);
		setDependency(ITradingPlatform.class, platform);

		log.info("Creating Connection manager");
		IConnectionManager connectionManager = new ConnectionManager(rest, platform);
		registry.register(connectionManager);
		setDependency(IConnectionManager.class, connectionManager);

		ITradeDataProviderSet providers = TradeDataProviderSet.of(IGINDEX, HISTDATA, FXCM);
		IPcfSourceManager fileManager = new PcfFileManager(new File(historicPriceDir), providers);

		log.info("Creating Historic Price Source");
		IHistoryService source = new PcfSourceHistoryService(fileManager);
		setDependency(IHistoryService.class, source);

		log.info("Logging in ...");
		connectionManager.login();
	}

	@Override
	protected void shutdownComponent() throws Exception {
		// TODO: Cancel all subscriptions here
	}

}