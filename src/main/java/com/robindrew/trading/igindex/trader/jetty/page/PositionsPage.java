package com.robindrew.trading.igindex.trader.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.trading.igindex.platform.IIgSession;
import com.robindrew.trading.igindex.platform.rest.IIgRestService;
import com.robindrew.trading.igindex.platform.rest.executor.getactivity.response.ActivityList;
import com.robindrew.trading.igindex.platform.rest.executor.getpositions.MarketPosition;

public class PositionsPage extends AbstractServicePage {

	public PositionsPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

		boolean refresh = request.getBoolean("refresh", false);

		IIgSession session = getDependency(IIgSession.class);
		dataMap.put("user", session.getCredentials().getUsername());
		dataMap.put("environment", session.getEnvironment());

		IIgRestService rest = getDependency(IIgRestService.class);
		List<MarketPosition> positions = rest.getPositionList();
		dataMap.put("positions", new TreeSet<>(positions));

		ActivityList activities = rest.getActivityList(refresh);
		dataMap.put("activities", activities);

	}
}
