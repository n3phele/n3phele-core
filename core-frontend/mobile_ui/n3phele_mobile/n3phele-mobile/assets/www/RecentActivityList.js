var listSize = 3;
enyo.kind({ 
		name:"RecentActivityList",
		result: null,
		components:[
			{classes: "onyx-sample-divider", content: "Recent Activities", style: "color: #375d8c"},
			{name: "list", kind: "List", fit: true, touch: true, onSetupItem: "setupItem", count: 1, style: "height:"+(55*listSize)+"px", components:[
				{name: "item", style: "padding: 10px; box-shadow: -4px 0px 4px rgba(0,0,0,0.3);",  classes: "panels-sample-flickr-item enyo-border-box",  ontap: "itemTap", components:[
					{ style:"margin: 2px; display:inline-block", components: [ {tag:"img", style:"width: 70%;", src: "assets/activities.png" }, ]},
					{ name: "activity", style: "display:inline-block"},
				]}//end item
			]}
		], //end components	
		getRecentActivities: function( uid ){
			var ajaxParams = {
				url: serverAddress+"progress",
				headers:{ 'authorization' : "Basic "+ uid},
				method: "GET",
				contentType: "application/x-www-form-urlencoded",
				sync: false, 
			};
			
			var ajaxComponent = new enyo.Ajax(ajaxParams); //connection parameters
			
			ajaxComponent
			.go({'summary' : true, 'start' : 0, 'end' : listSize-1})
			.response( this, "processRecentActivities" )
			.error( this, function(){ console.log("Error to load recent activities!!"); });
		},
		processRecentActivities: function( request, response){
			if(response.total == 0){alert("There is no recent activities!");return;}
			this.results = response.elements;
			this.$.list.setCount(this.results.length);
			this.$.list.reset();
		},
		setupItem: function(inSender, inEvent){
			if(this.results == null ) return;
			this.$.item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
			var i = inEvent.index;
			var item = this.results[i];
			this.$.activity.setContent(item.name);
		},
/**		rendered: function() {
			this.inherited(arguments);
			this.getRecentActivities(this.uid);
		},**/
		itemTap: function( sender, event){
			if(this.results == null ) return;
			var main = sender.owner.parent.owner;
			var panels = main.$.panels;

			main.closeSecondaryPanels(2);

			if (enyo.Panels.isScreenNarrow()){
				panels.setIndex(1);
			}
			
			panels.owner.$.imageIconPanel.destroyClientControls();
			main.createComponent({kind: "RecentActivityPanel", 'data': this.results[event.index], container: main.$.imageIconPanel});
		
			panels.owner.$.imageIconPanel.render();
		},
});

enyo.kind({ 
		name:"RecentActivityPanel",
		kind: "FittableRows",
		fit: true,
		components:[
			{name: "topToolbar",kind: "onyx.Toolbar", components: [	{content: "Activity"}, {fit: true} ]},
			{kind: "enyo.Scroller", fit: true, components: [
				{name: "panel_three", classes: "panels-sample-sliding-content", allowHtml: true, components:[
					{tag: "span", content: "Name:", style:"font-variant:small-caps;"}, {name: "acName", style:"font-weight: bold; display: inline-block"},
					{tag: "br"},
					{tag: "span", content: "Status:", style:"font-variant:small-caps;"}, {name: "acStatus", style:"display: inline-block"},
					{tag: "br"},
					{tag: "span", content: "Command:", style:"font-variant:small-caps;"}, 
					{name: "acCommand", style:"display: inline-block"},
					{name: "acComDesc", style:"display: inline-block"},
					{tag: "br"},
					{tag: "span", content: "Executed From", style:"font-variant:small-caps;"}, 
					{name: "acStart", style:"display: inline-block"},
					{tag: "span", content: " to ", style:"font-variant:small-caps;"}, 
					{name: "acComplete", style:"display: inline-block"},
					{tag: "br"},
					{tag: "span", content: "Estimated duration[in seconds]: ", style:"font-variant:small-caps;"}, //seconds
					{name: "acDuration", style:"display: inline-block"},
					{tag: "br"},
					{tag: "span", content: "Log: ", style:"font-variant:small-caps;"},
					{tag: "br"},
					{name: "acNarStamp", style:"display: inline-block;"},//key
					{name: "acNarStatus", style:"display: inline-block;font-weight: bold;"},//parentesis
					{name: "acNarId", style:"display: inline-block;"}, //" :"
					{name: "acNarText", style:"display: inline-block;font-weight: bold"}
				]}
			]}
		],
		create: function() {
			this.inherited(arguments);
			this.$.acName.setContent(" "+this.data.name);
			this.$.acStatus.setContent(" "+this.data.status);
			this.$.acCommand.setContent(" "+this.data.command);
			this.$.acComDesc.setContent(" ( "+this.data.description+" )");
			this.$.acStart.setContent(" "+this.data.started);
			this.$.acComplete.setContent(" "+this.data.completed);
			this.$.acDuration.setContent(" "+this.data.duration);
			this.$.acNarStamp.setContent("["+this.data.narratives.stamp+"]");
			this.$.acNarStatus.setContent(" ("+this.data.narratives.state+") ");
			this.$.acNarId.setContent(" "+this.data.narratives.id+" : ");
			this.$.acNarText.setContent(" "+this.data.narratives.text);
			
			if (enyo.Panels.isScreenNarrow()){
				this.createComponent({kind: "onyx.Toolbar", components: [ {kind: "onyx.Button", content: "Close", ontap: "backMenu"} ] });
			}else{
				this.createComponent({kind: "onyx.Toolbar"});
			}
		},
		backMenu: function( sender , event){
			sender.parent.parent.parent.parent.setIndex(0);
		}
});