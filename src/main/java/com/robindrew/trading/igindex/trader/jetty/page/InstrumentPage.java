package com.robindrew.trading.igindex.trader.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.common.text.Strings;
import com.robindrew.trading.provider.igindex.platform.IIgSession;
import com.robindrew.trading.provider.igindex.platform.rest.IIgRestService;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarkets.Markets;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getpositions.MarketPosition;
import com.robindrew.trading.trade.TradeDirection;

public class InstrumentPage extends AbstractServicePage {

	public InstrumentPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

		String epic = request.get("epic");
		boolean refresh = request.getBoolean("refresh", false);

		String action = request.get("action", "");
		BigDecimal size = request.getBigDecimal("size", null);
		int stopLoss = request.getInt("stopLoss", 0);
		int stopProfit = request.getInt("stopProfit", 0);

		// Trade!
		if (!action.isEmpty()) {
			TradeDirection direction = TradeDirection.valueOf(action);
			openPosition(epic, direction, size, stopLoss, stopProfit);
		}

		IIgSession session = getDependency(IIgSession.class);
		dataMap.put("user", session.getCredentials().getUsername());
		dataMap.put("environment", session.getEnvironment());

		IIgRestService rest = getDependency(IIgRestService.class);
		Markets markets = rest.getMarkets(epic, refresh);
		List<MarketPosition> positions = filter(epic, rest.getPositionList());

		dataMap.put("epic", epic);
		dataMap.put("markets", markets);
		dataMap.put("positions", positions);
		dataMap.put("json", Strings.json(markets, true));
	}

	private List<MarketPosition> filter(String epic, List<MarketPosition> positionList) {
		List<MarketPosition> list = new ArrayList<>();
		for (MarketPosition position : positionList) {
			if (position.getEpic().equals(epic)) {
				list.add(position);
			}
		}
		return list;
	}

	private void openPosition(String epic, TradeDirection direction, BigDecimal size, int stopLoss, int stopProfit) {
		IIgRestService rest = getDependency(IIgRestService.class);
		rest.openPosition(epic, direction, size, stopLoss, stopProfit < 1 ? null : stopProfit);
	}
}
