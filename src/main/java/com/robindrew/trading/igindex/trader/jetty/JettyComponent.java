package com.robindrew.trading.igindex.trader.jetty;

import com.google.common.base.Supplier;
import com.robindrew.common.html.Bootstrap;
import com.robindrew.common.http.servlet.executor.IHttpExecutor;
import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.service.component.jetty.JettyVelocityComponent;
import com.robindrew.common.service.component.jetty.handler.MatcherHttpHandler;
import com.robindrew.common.service.component.jetty.handler.page.BeanAttributePage;
import com.robindrew.common.service.component.jetty.handler.page.BeanConsolePage;
import com.robindrew.common.service.component.jetty.handler.page.BeanOperationPage;
import com.robindrew.common.service.component.jetty.handler.page.BeanViewPage;
import com.robindrew.common.service.component.jetty.handler.page.IndexPage;
import com.robindrew.common.service.component.jetty.handler.page.SystemPage;
import com.robindrew.common.template.ITemplateLocator;
import com.robindrew.common.template.velocity.VelocityTemplateLocatorSupplier;
import com.robindrew.trading.igindex.trader.jetty.page.AccountsPage;
import com.robindrew.trading.igindex.trader.jetty.page.HistoryPage;
import com.robindrew.trading.igindex.trader.jetty.page.InfoPage;
import com.robindrew.trading.igindex.trader.jetty.page.InstrumentPage;
import com.robindrew.trading.igindex.trader.jetty.page.InstrumentsPage;
import com.robindrew.trading.igindex.trader.jetty.page.PositionPage;
import com.robindrew.trading.igindex.trader.jetty.page.PositionsPage;

public class JettyComponent extends JettyVelocityComponent {

	@Override
	protected Supplier<ITemplateLocator> getTemplateLocator() {
		return new VelocityTemplateLocatorSupplier();
	}

	@Override
	protected void populate(MatcherHttpHandler handler) {

		// Register standard pages
		handler.uri("/", newIndexPage(getContext(), "site/common/Index.html"));
		handler.uri("/System", new SystemPage(getContext(), "site/common/System.html"));
		handler.uri("/BeanConsole", new BeanConsolePage(getContext(), "site/common/BeanConsole.html"));
		handler.uri("/BeanView", new BeanViewPage(getContext(), "site/common/BeanView.html"));
		handler.uri("/BeanAttribute", new BeanAttributePage(getContext(), "site/common/BeanAttribute.html"));
		handler.uri("/BeanOperation", new BeanOperationPage(getContext(), "site/common/BeanOperation.html"));

		// Register extra pages
		handler.uri("/Accounts", new AccountsPage(getContext(), "site/igindex/trader/Accounts.html"));
		handler.uri("/Position", new PositionPage(getContext(), "site/igindex/trader/Position.html"));
		handler.uri("/Positions", new PositionsPage(getContext(), "site/igindex/trader/Positions.html"));
		handler.uri("/Instrument", new InstrumentPage(getContext(), "site/igindex/trader/Instrument.html"));
		handler.uri("/Instruments", new InstrumentsPage(getContext(), "site/igindex/trader/Instruments.html"));
		handler.uri("/History", new HistoryPage(getContext(), "site/igindex/trader/History.html"));

		handler.resources("/images/.+", "site/igindex/trader");
	}

	private IHttpExecutor newIndexPage(IVelocityHttpContext context, String templateName) {
		IndexPage page = new IndexPage(context, templateName);
		page.addLink("Accounts", "/Accounts", Bootstrap.COLOR_DEFAULT);
		page.addLink("Positions", "/Positions", Bootstrap.COLOR_DEFAULT);
		page.addLink("Instruments", "/Instruments", Bootstrap.COLOR_DEFAULT);
		page.addLink("History", "/History", Bootstrap.COLOR_DEFAULT);
		return page;
	}

}
