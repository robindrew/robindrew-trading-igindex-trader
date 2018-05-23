package com.robindrew.trading.igindex.trader.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.robindrew.common.collect.Paginator;
import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.trading.igindex.platform.IIgIndexSession;
import com.robindrew.trading.igindex.platform.rest.IIgIndexRestService;
import com.robindrew.trading.igindex.platform.rest.executor.getmarketnavigation.cache.IMarketNavigationCache;
import com.robindrew.trading.igindex.platform.rest.executor.getmarketnavigation.response.Market;
import com.robindrew.trading.igindex.platform.rest.executor.getmarketnavigation.response.MarketNavigation;
import com.robindrew.trading.igindex.platform.rest.executor.getmarketnavigation.response.Node;

public class InstrumentsPage extends AbstractServicePage {

	public InstrumentsPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

		String search = request.getString("search", "");

		int id = request.getInteger("id", 0);
		int parentId = request.getInteger("parentId", 0);
		String name = request.getString("name", "Instruments");
		boolean refresh = request.getBoolean("refresh", false);

		IIgIndexSession session = getDependency(IIgIndexSession.class);
		dataMap.put("user", session.getCredentials().getUsername());
		dataMap.put("environment", session.getEnvironment());

		IIgIndexRestService rest = getDependency(IIgIndexRestService.class);

		if (!search.isEmpty()) {
			List<Market> markets = rest.searchMarkets(search);
			dataMap.put("search", search);
			dataMap.put("markets", markets);
		} else {

			IMarketNavigationCache cache = getDependency(IMarketNavigationCache.class);
			MarketNavigation navigation = rest.getMarketNavigation(id, refresh);
			navigation.setId(id);
			navigation.setName(name);
			navigation.setParentId(parentId);

			List<MarketNavigation> ancestors = getAncestors(navigation, cache);
			dataMap.put("id", id);
			dataMap.put("name", name);
			dataMap.put("navigation", navigation);
			dataMap.put("ancestors", ancestors);

			List<Node> nodes = navigation.getNodes();
			if (nodes != null) {
				Paginator<Node> paginator = new Paginator<>(nodes);
				List<List<Node>> pages = paginator.getPages(15);
				dataMap.put("nodes", pages);
			}
		}

	}

	private List<MarketNavigation> getAncestors(MarketNavigation navigation, IMarketNavigationCache cache) {
		LinkedList<MarketNavigation> list = new LinkedList<>();
		list.addFirst(navigation);

		while (navigation.getParentId() != navigation.getId()) {
			navigation = cache.get(navigation.getParentId());
			if (navigation == null) {
				break;
			}
			list.addFirst(navigation);
		}
		return list;
	}
}
