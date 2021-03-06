package com.robindrew.trading.igindex.trader.igindex.session;

import com.robindrew.trading.igindex.platform.IgIndexSession;

public class SessionManager implements SessionManagerMBean {

	private final IgIndexSession session;

	public SessionManager(IgIndexSession session) {
		this.session = session;
	}

	@Override
	public String getEnvironment() {
		return session.getEnvironment().name();
	}

	@Override
	public String getUsername() {
		return session.getCredentials().getUsername();
	}

	@Override
	public String getApiKey() {
		return session.getCredentials().getApiKey();
	}

}
