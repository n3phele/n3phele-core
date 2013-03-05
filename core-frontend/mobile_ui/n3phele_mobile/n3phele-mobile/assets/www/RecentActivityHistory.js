enyo.kind({
	name: "RecentActivityHistory",
	kind: "FittableRows",
	style: "padding : 0px;width:100%;height:100%;",
	components:[
	            {kind: "onyx.Toolbar", components: [	{content: "Activity History"}, {fit: true} ]},
	            {fit: true, touch: true, kind: "Scroller",components:[
	                  {name: "commandList", kind: "List", fit: true, touch:true, classes:  "enyo-fit list-sample-list",style: "width: 50%;margin: auto;background-color: #eee; max-height:90%", onSetupItem: "fillItems", components: [
	                        {name: "item", classes: "list-sample-item enyo-border-box", style:"border: 1px solid silver;padding: 18px;",ontap: "itemTap", components: [
                                 {name: "name"}
	                         ]}                                                                                              			                                                         			
	                    ]}
	             ]}
	],
	create: function(){
		this.inherited(arguments);
		
		this.lines = {
				"name":"Progress","uri":"https://n3phele-dev.appspot.com/resources/progress",
				"mime":"application/vnd.com.n3phele.Collection+json",
				"public":"true","total":"10","elements":[
					{"name":"t2","uri":"https://n3phele-dev.appspot.com/resources/progress/106011",
					 "mime":"factory/vnd.com.n3phele.Progress+json",
					 "owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					 "public":"false","command":"new_user_data",
					 "description":"Qiime Test command",
					 "started":"2013-01-30T16:02:32.719Z",
					 "completed":"2013-01-30T16:03:01.309Z",
					 "lastUpdate":"2013-01-30T16:03:01.425Z",
					 "duration":"128",
					 "percentx10Complete":"900",
					 "status":"COMPLETE",
					 "activity":"https://n3phele-dev.appspot.com/resources/activity/108004",
					 "narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 21 seconds.",
						"state":"info","id":"vm","stamp":"2013-01-30T16:03:00.643Z"
						}
					 },
					 {"name":"t",
					  "uri":"https://n3phele-dev.appspot.com/resources/progress/117017",
					  "mime":"factory/vnd.com.n3phele.Progress+json",
					  "owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					  "public":"false","command":"new_user_data",
					  "description":"Qiime Test command",
					  "started":"2013-01-30T15:57:34.990Z",
					  "completed":"2013-01-30T15:59:24.292Z",
					  "lastUpdate":"2013-01-30T15:59:24.380Z",
					  "duration":"135","percentx10Complete":"900",
					  "status":"COMPLETE",
					  "activity":"https://n3phele-dev.appspot.com/resources/activity/104010",
					  "narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 93 seconds.",
						"state":"info","id":"vm","stamp":"2013-01-30T15:59:23.600Z"
						}
					 },
					 {"name":"vm2",
					  "uri":"https://n3phele-dev.appspot.com/resources/progress/114008",
					  "mime":"factory/vnd.com.n3phele.Progress+json",
					  "owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					  "public":"false",
					  "command":"new_user_data",
					  "description":"Qiime Test command",
					  "started":"2013-01-24T11:31:49.149Z",
					  "completed":"2013-01-24T11:32:20.833Z",
					  "lastUpdate":"2013-01-24T11:32:20.946Z",
					  "duration":"130",
					  "percentx10Complete":"900",
					  "status":"COMPLETE",
					  "activity":"https://n3phele-dev.appspot.com/resources/activity/115004",
					  "narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 20 seconds.",
						"state":"info","id":"vm","stamp":"2013-01-24T11:32:20.365Z"
						}
					 },
					 {"name":"vm",
					  "uri":"https://n3phele-dev.appspot.com/resources/progress/113007",
					  "mime":"factory/vnd.com.n3phele.Progress+json",
					  "owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					  "public":"false","command":"new_user_data",
					  "description":"Qiime Test command",
					  "started":"2013-01-24T11:19:22.809Z",
					  "completed":"2013-01-24T11:21:13.496Z",
					  "lastUpdate":"2013-01-24T11:21:13.562Z",
					  "duration":"138",
					  "percentx10Complete":"900",
					  "status":"COMPLETE",
					  "activity":"https://n3phele-dev.appspot.com/resources/activity/108002",
					  "narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 98 seconds.",
						"state":"info",
						"id":"vm",
						"stamp":"2013-01-24T11:21:12.967Z"
						}
					},
					{"name":"6",
					 "uri":"https://n3phele-dev.appspot.com/resources/progress/112006",
					 "mime":"factory/vnd.com.n3phele.Progress+json",
					 "owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					 "public":"false","command":"new_user_data",
					 "description":"Qiime Test command",
					 "started":"2013-01-23T17:56:53.480Z",
					 "completed":"2013-01-23T17:57:26.239Z",
					 "lastUpdate":"2013-01-23T17:57:26.323Z",
					 "duration":"130",
					 "percentx10Complete":"900",
					 "status":"COMPLETE",
					 "activity":"https://n3phele-dev.appspot.com/resources/activity/109009",
					 "narratives":{"text":"1 vm(s) created completed successfully. Elapsed time 21 seconds.",
					 "state":"info","id":"vm","stamp":"2013-01-23T17:57:25.941Z"
					 }
					},
					{"name":"5","uri":"https://n3phele-dev.appspot.com/resources/progress/117016",
					"mime":"factory/vnd.com.n3phele.Progress+json",
					"owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					"public":"false","command":"new_user_data",
					"description":"Qiime Test command",
					"started":"2013-01-23T17:34:02.366Z",
					"completed":"2013-01-23T17:34:27.824Z",
					"lastUpdate":"2013-01-23T17:34:27.877Z",
					"duration":"130",
					"percentx10Complete":"900",
					"status":"COMPLETE",
					"activity":"https://n3phele-dev.appspot.com/resources/activity/117015",
					"narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 18 seconds.",
						"state":"info","id":"vm","stamp":"2013-01-23T17:34:27.292Z"
						}
					},
					{"name":"4",
					"uri":"https://n3phele-dev.appspot.com/resources/progress/106010",
					"mime":"factory/vnd.com.n3phele.Progress+json",
					"owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					"public":"false",
					"command":"new_user_data","description":"Qiime Test command",
					"started":"2013-01-23T17:21:49.756Z",
					"completed":"2013-01-23T17:24:15.942Z",
					"lastUpdate":"2013-01-23T17:24:16.495Z",
					"duration":"141",
					"percentx10Complete":"900",
					"status":"COMPLETE",
					"activity":"https://n3phele-dev.appspot.com/resources/activity/105008",
					"narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 132 seconds.",
						"state":"info","id":"vm","stamp":"2013-01-23T17:24:15.224Z"
					 }
					},
					{"name":"vm2",
					"uri":"https://n3phele-dev.appspot.com/resources/progress/120001",
					"mime":"factory/vnd.com.n3phele.Progress+json",
					"owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					"public":"false","command":"new_user_data",
					"description":"Qiime Test command","started":"2013-01-23T12:04:49.176Z",
					"completed":"2013-01-23T12:05:23.827Z",
					"lastUpdate":"2013-01-23T12:05:23.881Z",
					"duration":"131",
					"percentx10Complete":"900",
					"status":"COMPLETE",
					"activity":"https://n3phele-dev.appspot.com/resources/activity/119001",
					"narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 24 seconds.",
						"state":"info","id":"vm","stamp":"2013-01-23T12:05:23.477Z"
					 }
					},
					{"name":"vm",
					"uri":"https://n3phele-dev.appspot.com/resources/progress/118010",
					"mime":"factory/vnd.com.n3phele.Progress+json","owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					"public":"false","command":"new_user_data","description":"Qiime Test command",
					"started":"2013-01-23T11:59:29.432Z","completed":"2013-01-23T12:01:13.833Z",
					"lastUpdate":"2013-01-23T12:01:14.010Z","duration":"137",
					"percentx10Complete":"900",
					"status":"COMPLETE",
					"activity":"https://n3phele-dev.appspot.com/resources/activity/117013",
					"narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 91 seconds.",
						"state":"info","id":"vm","stamp":"2013-01-23T12:01:13.235Z"
						}
					},
					{"name":"123",
					"uri":"https://n3phele-dev.appspot.com/resources/progress/109008",
					"mime":"factory/vnd.com.n3phele.Progress+json",
					"owner":"https://n3phele-dev.appspot.com/resources/user/31014",
					"public":"false","command":"new_user_data",
					"description":"Qiime Test command",
					"started":"2013-01-22T16:28:28.135Z",
					"completed":"2013-01-22T16:30:17.332Z","lastUpdate":"2013-01-22T16:30:17.491Z",
					"duration":"138","percentx10Complete":"900",
					"status":"COMPLETE",
					"activity":"https://n3phele-dev.appspot.com/resources/activity/118009",
					"narratives":{
						"text":"1 vm(s) created completed successfully. Elapsed time 97 seconds.",
						"state":"info","id":"vm","stamp":"2013-01-22T16:30:16.588Z"
					 }
					}]
				};
		
				if (enyo.Panels.isScreenNarrow())
					this.createComponent({kind: "onyx.Toolbar", components: [ {kind: "onyx.Button", content: "Close", ontap: "backMenu"} ]});
				else
					this.createComponent({kind: "onyx.Toolbar"});
				
				this.$.commandList.setCount(this.lines.total);
		
		console.log(this.lines);
	},
	backMenu: function( sender , event){
		sender.parent.parent.parent.parent.setIndex(0);
	},
	fillItems: function( inSender, inEvent ){
		var index = inEvent.index;
		this.$.item.addRemoveClass("onyx-selected", inSender.isSelected(index));
		this.$.name.setContent( this.lines.elements[index].name );
	},
	itemTap:function( inSender, inEvent ){
		var bounds = inSender.parent.owner.getScrollBounds();
		var index = inEvent.index;
		var main = inSender.owner.parent.owner;
		var panels = main.$.panels;

		main.closeSecondaryPanels(2);
		main.createComponent({kind: "RecentActivityPanel", 'data': inSender.owner.lines.elements[index], container: panels}).render();
		
		panels.reflow();
		panels.setIndex(2);
		inSender.parent.owner.scrollTo( bounds["left"] ,  bounds["top"] );
	}
});