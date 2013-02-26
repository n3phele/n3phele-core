enyo.kind({
	name: "enyo.sample.PanelsFlickrSample",
	kind: "Panels",
	classes: "panels-sample-flickr-panels enyo-unselectable enyo-fit",
	arrangerKind: "CollapsingArranger",
	components: [
	
		{layoutKind: "FittableRowsLayout", components: [
		
			{kind:"Image", src:"assets/cloud-theme.gif",scale:"auto", classes:"enyo-fit"},
			{name: "mylabel", content: "N3phele", style: "color: black; font-size: 25 px; text-align:center;font-weight:bold;"},
			{kind: "List", fit: true, touch: true, count: 4, onSetupItem: "setupItem", components: [
				{name: "item", style: "padding: 10px;", classes: "panels-sample-flickr-item enyo-border-box", ontap: "itemTap", components: [
					{name: "name"},
				]},				
			]}
			
		]},
		
		
		{name: "pictureView", fit: true, kind: "FittableRows", classes: "enyo-fit panels-sample-flickr-main", components: [
			{name: "backToolbar", kind: "onyx.Toolbar", showing: true, components: [
				{kind: "onyx.Button", content: "Back", ontap: "setupItem"}
			]},			
		]},
	],
	
	menuItem: [
		{name: "Files"},
		{name: "Commands"},
		{name: "Activity History"},
		{name: "Accounts"},
	],
	setupItem: function(inSender, inEvent) {
		var i = inEvent.index;
		var item = this.menuItem[i];
		this.$.item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.name.setContent(item.name);
	},
	
	itemTap: function(inSender, inEvent) {
		if (enyo.Panels.isScreenNarrow()) {
			this.setIndex(1);
		}		
	},	
	

});
