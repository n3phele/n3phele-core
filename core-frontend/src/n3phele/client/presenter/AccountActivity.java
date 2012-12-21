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
import n3phele.client.CacheManager;
import n3phele.client.ClientFactory;
import n3phele.client.model.Account;
import n3phele.client.model.Cloud;
import n3phele.client.presenter.helpers.AuthenticatedRequestFactory;
import n3phele.client.view.AccountView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class AccountActivity extends AbstractActivity {
	private final String accountUri;
	private final AccountView display;
	private Account account = null;
	private final CacheManager cacheManager;
	private final EventBus eventBus;

	public AccountActivity(String accountUri, ClientFactory factory) {
		this.accountUri = accountUri;
		this.display = factory.getAccountView();
		this.cacheManager = factory.getCacheManager();
		this.eventBus = factory.getEventBus();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		display.setPresenter(this);
		panel.setWidget(display);
		getClouds();

		if(accountUri == null || accountUri.length()==0 || accountUri.equals("null")) {
			this.account = JavaScriptObject.createObject().<Account> cast();
			display.setData(this.account);
		} else {
			this.account = null;
			getAccount();
			display.setData(this.account);
		}


	}

	@Override
	public String mayStop() {
		return null;
	}
	@Override
	public void onCancel() {

	}
	@Override
	public void onStop() {
		this.display.setData(null);
	}
	public void onSave(Account account, String name, String description,
			String cloud, String cloudId, String secret) {
			updateAccountDetails(account.getUri(), name, description, cloud, cloudId, secret);	
			
	}

	protected void updateClouds(List<Cloud> list) {
		display.setClouds(list);
	}

	protected void updateAccount(Account account) {
		this.account = account;
		display.setData(this.account);
	}


	public void goToPrevious() {
		History.back();
	}



	/*
	 * Data Handling
	 * -------------
	 */


	public void getAccount() {
		// Send request to server and catch any errors.
		RequestBuilder builder = AuthenticatedRequestFactory.newRequest(RequestBuilder.GET, accountUri);
		try {
			Request request = builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// displayError("Couldn't retrieve JSON "+exception.getMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					GWT.log("Got reply");
					if (200 == response.getStatusCode()) {
						Account account = Account.asAccount(response.getText());
						updateAccount(account);
					} else {

					}
				}

			});
		} catch (RequestException e) {
			//displayError("Couldn't retrieve JSON "+e.getMessage());
		}
	}

	protected void getClouds() {
		this.eventBus.addHandler(CacheManager.CloudListUpdate.TYPE, new CacheManager.CloudListUpdateEventHandler() {
			@Override
			public void onMessageReceived(CacheManager.CloudListUpdate event) {
				updateClouds(cacheManager.getClouds());
			}
		});
		updateClouds(cacheManager.getClouds());
	}
	private void updateAccountDetails(String url, String name, String description, String cloud, String cloudId, final String password) {

		// Send request to server and catch any errors.
		if(url==null || url.trim().length()==0 || url.equals("null")) {
			url = cacheManager.ServiceAddress+"account";
		}
		RequestBuilder builder = AuthenticatedRequestFactory.newRequest(RequestBuilder.POST, url);
		builder.setHeader("Content-type", "application/x-www-form-urlencoded");
		StringBuilder args = new StringBuilder();
		args.append("name=");
		args.append(URL.encodeQueryString(name));
		if(description != null && description.length() !=0) {
			args.append("&description=");
			args.append(URL.encodeQueryString(description));
		}
		args.append("&cloud=");
		args.append(URL.encodeQueryString(cloud));
		if(password != null && password.length() > 0) {
			args.append("&accountId=");
			args.append(URL.encodeQueryString(cloudId));
			args.append("&secret=");
			args.append(URL.encodeQueryString(password));
		}
		try {
			@SuppressWarnings("unused")
			Request request = builder.sendRequest(args.toString(), new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// displayError("Couldn't retrieve JSON "+exception.getMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					GWT.log("Got reply");
					if (200 == response.getStatusCode()) {
						goToPrevious();
					} else if (201 == response.getStatusCode()) {
						goToPrevious();
					} else {
						Window.alert("Account update error "+response.getStatusCode()+response.getStatusText());
					}					
				}

			});
		} catch (RequestException e) {
			//displayError("Couldn't retrieve JSON "+e.getMessage());
		}
	}



	public void onSelect(Account selected) {
		// TODO Auto-generated method stub

	}

	/*
	 * Event Definition
	 * ----------------
	 */



}
