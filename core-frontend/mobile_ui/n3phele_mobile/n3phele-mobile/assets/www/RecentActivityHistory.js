enyo.kind({
	name: "RecentActivityHistory",
	kind: "FittableRows",
	style: "padding : 0px;width:100%;height:100%;",
	end : 9,
	lines:  new Array(),
	components:[
	            {kind: "onyx.Toolbar", components: [	{content: "Activity History"}, {fit: true} ]},
	            {fit: true, touch: true, kind: "Scroller",components:[
	                  {classes: "onyx-sample-divider", content: "Tasks executed by user", style: "color: #375d8c;text-align:center", name:"divider"},
	                  {name: "commandList", kind: "List", fit: true, touch:true, count: 0 , classes:  "enyo-fit list-sample-list",style: "width: 50%;margin: auto;background-color: #eee; max-height:90%", ondown:"listUpdate", onSetupItem: "fillItems", components: [
	                        {name: "item", classes: "list-sample-item enyo-border-box", style:"border: 1px solid silver;padding: 18px;",ontap: "itemTap",components: [
                                 {name: "name"}
	                         ]}                                                                                              			                                                         			
	                  ]}
	             ]}
	],
	create: function(){
		this.inherited(arguments);
		
		//Footer toolbar
		if (enyo.Panels.isScreenNarrow())
			this.createComponent({kind: "onyx.Toolbar", components: [ {kind: "onyx.Button", content: "Close", ontap: "backMenu"} ]});
		else
			this.createComponent({kind: "onyx.Toolbar"});

		//get data
		this.mountingList( 0 , this.end );		
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
		main.createComponent({kind: "RecentActivityPanel", 'url': inSender.owner.lines.elements[index].uri, 'uid': this.uid, container: panels}).render();
		
		panels.reflow();
		panels.setIndex(2);
		inSender.parent.owner.scrollTo( bounds["left"] ,  bounds["top"] );
	},

	listUpdate: function( sender, event){
		var limitIndex = this.lines.elements.length - 5;
		if( event.index > limitIndex ){
			this.mountingList( this.end , this.end + 3  );
			this.end += 3;
		}		
	},
	mountingList: function( start , end ){
		if( this.lines.elements != undefined )
			if( this.lines.elements.length == this.lines.total )
				return;
			
		var ajaxComponent = new enyo.Ajax({
			url: serverAddress+"process",
			headers:{ 'authorization' : "Basic "+ this.uid},
			method: "GET",
			contentType: "application/x-www-form-urlencoded",
			sync: false, 
		}); //connection parameters
		
		ajaxComponent
		.go({'summary' : true, 'start' : start, 'end' : end})
		.response( this, function(sender, response){
			//increment data
			if( this.lines.length == 0){
				if( response.total == 0 ){
					this.$.divider.setContent("User doesn't have history activities!");
					this.$.commandList.applyStyle("display", "none !important");
					this.reflow();
					return;
				}
				response.elements = fixArrayInformation(response.elements);
				this.lines = response;
				
			}else{
				response.elements = fixArrayInformation(response.elements);
				this.lines.elements = this.lines.elements.concat( elements );
			}
			this.$.commandList.setCount(this.lines.elements.length);
			this.$.commandList.applyStyle("height", ""+(this.lines.elements.length * 62)+"px" );
			this.$.commandList.reset();
		} )
		.error( this, function(){ console.log("Error to load recent activities!!");});
		
		
	}
	
});