package com.robindrew.trading.igindex.trader.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;

import java.util.Map;

import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.common.text.Strings;
import com.robindrew.trading.igindex.platform.IIgIndexSession;
import com.robindrew.trading.igindex.platform.rest.IIgIndexRestService;
import com.robindrew.trading.igindex.platform.rest.executor.getpositions.MarketPosition;

public class PositionPage extends AbstractServicePage {

	public PositionPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

		String dealId = request.getString("dealId");
		boolean close = request.getBoolean("close", false);

		IIgIndexSession session = getDependency(IIgIndexSession.class);
		dataMap.put("user", session.getCredentials().getUsername());
		dataMap.put("environment", session.getEnvironment());

		IIgIndexRestService rest = getDependency(IIgIndexRestService.class);
		MarketPosition position = rest.getPositionByDealId(dealId);
		if (close) {
			rest.closePosition(position);
			dataMap.put("closed", true);
		} else {
			dataMap.put("closed", false);
		}

		dataMap.put("dealId", dealId);
		dataMap.put("position", position);
		dataMap.put("json", Strings.json(position, true));

	}
}
