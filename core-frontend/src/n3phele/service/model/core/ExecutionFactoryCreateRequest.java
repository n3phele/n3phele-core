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
package n3phele.service.model.core;

import java.net.URI;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "ExecutionFactoryCreateRequest")
@XmlType(name = "ExecutionFactoryCreateRequest", propOrder = { "name", "description", "location", "parameters", "notification",
		"accessKey", "encryptedSecret", "owner", "idempotencyKey", "instanceId", "spotId", "created", "price", "account",
		"activity", "id" })
public class ExecutionFactoryCreateRequest {
	public String name;
	public String description;
	public URI location;
	public ArrayList<NameValue> parameters;
	public URI notification;
	public String accessKey;
	public String encryptedSecret;
	public URI owner;
	public String idempotencyKey;

	// Created for n3phele instances
	public String instanceId;
	public String spotId;
	public String created;
	public String price;
	public URI account;
	public URI activity;
	public Long id;

	public ExecutionFactoryCreateRequest() {
	}

}