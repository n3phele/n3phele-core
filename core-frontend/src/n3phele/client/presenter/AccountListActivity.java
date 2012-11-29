/**
 * @author Nigel Cook
 *
 * (C) Copyright 2010-2012. Nigel Cook. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
 * except in compliance with the License. 
 * 
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 */
package n3phele.client.presenter;

import java.util.List;

import n3phele.client.AppPlaceHistoryMapper;
import n3phele.client.CacheManager;
import n3phele.client.ClientFactory;
import n3phele.client.model.Account;
import n3phele.client.model.Collection;
import n3phele.client.presenter.helpers.AuthenticatedRequestFactory;
import n3phele.client.view.AccountListView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class AccountListActivity extends AbstractActivity {
	private EventBus eventBus;
	private final AppPlaceHistoryMapper historyMapper;
	private final AccountListView display;
	private List<Account> accountList = null;
	private final CacheManager cacheManager;
	private String accountCollection;
	private HandlerRegistration handlerRegistration;
	public AccountListActivity(String accountUri, ClientFactory factory) {
		
		this.historyMapper = factory.getHistoryMapper();
		this.display = factory.getAccountListView();
		this.cacheManager = factory.getCacheManager();
		this.accountCollection = URL.encode(factory.getCacheManager().ServiceAddress + "account");
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		this.eventBus = eventBus;
		handlerRegistration(eventBus);
		display.setPresenter(this);
		panel.setWidget(display);
		display.setDisplayList(this.accountList);
//		getClouds();
		getAccountList();
	}
	
	@Override
	public String mayStop() {
	    return null;
	}
	@Override
	public void onCancel() {
		unregister();
	}
	@Override
	public void onStop() {
		this.display.setDisplayList(null);
		unregister();
	}
	

//	protected void updateClouds(List<Cloud> list) {
//		display.setClouds(list); 
//	}

	protected void updateAccountList(List<Account> list) {
		this.accountList = list;
		display.refresh(this.accountList);
	}
	
	
	public void goToPrevious() {
		History.back();
	}
	public void handlerRegistration(EventBus eventBus) {
		this.handlerRegistration = this.eventBus.addHandler(AccountListUpdate.TYPE, new AccountListUpdateEventHandler() {
			@Override
			public void onMessageReceived(AccountListUpdate event) {
				getAccountList();
			}
		});
		CacheManager.EventConstructor change = new CacheManager.EventConstructor() {
			@Override
			public AccountListUpdate newInstance(String key) {
				return new AccountListUpdate();
			}
		};
		cacheManager.register(cacheManager.ServiceAddress + "account", "accountList", change);

		
	}

	protected void unregister() {
		cacheManager.unregister(cacheManager.ServiceAddress + "account", "accountList");
	}

	
	/*
	 * Data Handling
	 * -------------
	 */
	

	public void getAccountList() {
		// Send request to server and catch any errors.
		RequestBuilder builder = AuthenticatedRequestFactory.newRequest(RequestBuilder.GET, accountCollection);
		try {
			Request request = builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// displayError("Couldn't retrieve JSON "+exception.getMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					GWT.log("Got reply");
					if (200 == response.getStatusCode()) {
						Collection<Account> account = Account.asCollection(response.getText());
						updateAccountList(account.getElements());
					} else {

					}
				}

			});
		} catch (RequestException e) {
			//displayError("Couldn't retrieve JSON "+e.getMessage());
		}
	}
	
//	protected void getClouds() {
//		this.eventBus.addHandler(CacheManager.CloudListUpdate.TYPE, new CacheManager.CloudListUpdateEventHandler() {
//			@Override
//			public void onMessageReceived(CacheManager.CloudListUpdate event) {
//				updateClouds(cacheManager.getClouds());
//			}
//		});
//		updateClouds(cacheManager.getClouds());
//	}

	public void onSelect(Account selected) {
		History.newItem(historyMapper.getToken(new AccountPlace(selected.getUri())));

		
	}
	
	
	/*
	 * Event Definition
	 * ----------------
	 */
	public interface AccountListUpdateEventHandler extends EventHandler {
		void onMessageReceived(AccountListUpdate commandListUpdate);
	}
	public static class AccountListUpdate extends GwtEvent<AccountListUpdateEventHandler> {
		public static Type<AccountListUpdateEventHandler> TYPE = new Type<AccountListUpdateEventHandler>();
		public AccountListUpdate() {}
		@Override
		public com.google.gwt.event.shared.GwtEvent.Type<AccountListUpdateEventHandler> getAssociatedType() {
			return TYPE;
		}
		@Override
		protected void dispatch(AccountListUpdateEventHandler handler) {
			handler.onMessageReceived(this);
		}
	}


}
