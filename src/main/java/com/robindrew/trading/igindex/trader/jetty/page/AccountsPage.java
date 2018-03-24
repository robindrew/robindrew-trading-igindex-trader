package com.robindrew.trading.igindex.trader.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;

import java.util.List;
import java.util.Map;

import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.common.text.Strings;
import com.robindrew.trading.igindex.trader.igindex.connection.IConnectionManager;
import com.robindrew.trading.provider.igindex.platform.IIgSession;
import com.robindrew.trading.provider.igindex.platform.rest.IIgRestService;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getaccounts.Account;

public class AccountsPage extends AbstractServicePage {

	public AccountsPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

		IIgSession session = getDependency(IIgSession.class);
		dataMap.put("user", session.getCredentials().getUsername());
		dataMap.put("environment", session.getEnvironment());

		IConnectionManager connection = getDependency(IConnectionManager.class);
		if (connection.isLoggedIn()) {
			IIgRestService rest = getDependency(IIgRestService.class);
			List<Account> accounts = rest.getAccountList();
			dataMap.put("accounts", accounts);
			dataMap.put("json", Strings.json(accounts, true));
			dataMap.put("loggedIn", true);
		} else {
			dataMap.put("loggedIn", false);
		}
	}
}
