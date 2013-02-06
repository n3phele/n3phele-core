var createRandomItems = function(number) {
	list = [];
	for (var i=0; i<number; i++)
	{
		var ownerName = Math.random().toString(36).substr(2, 5);
		var owner = { name: ownerName, website: "http://github.com/LucioC", twitter: "" };
		var widgetName = Math.random().toString(36).substr(4, 10);
		var widget = { name: widgetName, displayName: "Nome da lista Cool", owner: "Lucio", dependencies: [ ] , image: "gallery_images/CoolList.jpg" };
		widget.owner = owner;
		list.push(widget);
	}
	return list;
}

//Exposes a list of items in two configurations, one is a Card type used for large screens
//, and a ListItem, used for narrow screens. Cards are shown in a grid like gallery, and 
// ListItem are shown in a basic list mode.
enyo.kind({
	name: "IconList",
	kind: "Scroller",
	data: [],
	events: {
		onSelectedItem: "",
		onDeselectedItems: ""
	},
	fit: true, touch: true, classes: "main",
	components: [
		// using media query (see css) to determine which one should be displayed
		{name: "cards", classes: "cards"},
	],
	constructor: function() {
		this.inherited(arguments);
	},
	create: function() {
		this.inherited(arguments);
	},
	rendered: function() {
		this.inherited(arguments);
		this.retrieveContentData();
		this.fillContent();
	},
	resizeHandler: function() {
		this.inherited(arguments);
	},
	//Get data content from this.data and pass to this.widgets. Call render function.
	fillContent: function() {
		this.widgets = {};
		
		for(var n in this.data)
		{
			this.widgets[this.data[n].name] = this.data[n];
		}
		
		this.renderItems();
	},
	retrieveContentData: function() {
		this.data = createRandomItems(2);
	},
	//Create Card and ListItems based on the content of this.widgets variable
	renderItems: function() {
		this.$.cards.destroyClientControls();
		
		var items = this.widgets;
		
		items = this.toArray(items);
		
		for (var i=0, w; (w=items[i]); i++) {
			var more = {data: w, ontap: "itemTap"};
			this.createComponent({kind: "Card", container: this.$.cards}, more);
		}
		
		this.$.cards.render();
	},
	toArray: function(inItems) {
		var ls = [];		
		for (var n in inItems) {
			ls.push(inItems[n]);
		}
		return ls;
	},
	lastSelected: false,
	selectItem: function(item) {	
		if( item != this.lastSelected ) 
		{	
			this.setAsSelected(item);
			this.lastSelected = item;
			this.doSelectedItem(item.data);
		}
		else
		{	
			this.deselectLastItem();
			this.lastSelected = null;
			this.doDeselectedItems();
		}
	},
	setAsSelected: function(item) { 
		if (this.lastSelected){
				this.lastSelected.addRemoveClass("onyx-selected", false );
		}
		item.addRemoveClass("onyx-selected", true );
	},
	deselectLastItem: function() {
		if(this.lastSelected) this.lastSelected.addRemoveClass("onyx-selected", false );
		lastSelected = null;
	}
	,
	itemTap: function(inSender, inEvent) {
		var selectedObject = inSender;
		
		console.log( "selected object" + selectedObject);
		console.log( "last selected object" + this.lastSelected);
		
		this.selectItem(inSender);
	},
	preventTap: function(inSender, inEvent) {
		inEvent.preventTap();
	}
});

//Hold the 'widget' object from the json file, showing displayName and owner.name
//Represents the data that is shown in the narrow screen mode (list only)
/*
enyo.kind({
	name: "ListItem",
	classes:"listitem",
	published: {
		data: ""
	},
	components: [
		{name: "name", classes: "name"}
	],
	create: function() {
		this.inherited(arguments);
		this.dataChanged();
	},
	dataChanged: function() {
		var i = this.data;
		if (!i) {
			return;
		}
		this.$.name.setContent(i.displayName);
	}
});
*/

//Presents a icon for the object
enyo.kind({
	name: "Card",
	kindClasses: "card",
	published: {
		data: ""
	},
	components: [
		{name: "icon", kind: "Image", classes: "icon"}
		,
		{name: "name", classes: "name"}
	],
	dataChanged: function() {
		this.inherited(arguments);
		
		var i = this.data;
		if (!i) {
			return;
		}
		this.$.name.setContent(i.displayName);
		
		if (this.data) {
			this.$.icon.setSrc(this.data.image);
		}
	},
	create: function() {
		this.inherited(arguments);
		this.dataChanged();
	}
});

