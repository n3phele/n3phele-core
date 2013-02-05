var createCommandItems = function(arrayOfCommands, arrayOfImages) {
	list = [];
	console.log(arrayOfCommands);
	console.log(arrayOfImages);
	for (var i in arrayOfCommands)
	{
		var widget = { name: arrayOfCommands[i], displayName: arrayOfCommands[i], image: arrayOfImages[i] };
		list.push(widget);
	}
	return list;
}

enyo.kind({
	name: "enyo.sample.PanelsSlidingSample",
	kind: "FittableRows",
	classes: "onyx enyo-fit",
	c1:{ 
		name:"contContent",
		kind: "FittableRows",
		fit: true,
		components:[
			{name: "topToolbar",kind: "onyx.Toolbar", components: [
						{content: "Concatenate"},
						{fit: true}]},
			{kind: "enyo.Scroller", fit: true, components: [
				{name: "panel_three",
				classes: "panels-sample-sliding-content", allowHtml: true, components:[
				{name: "description", content: "Concatenate up to 8 files", style: "text-align:center; padding: 10px;"}]}
			]},
			{kind: "onyx.Toolbar", components: [
				{kind: "onyx.Button", content: "Close", ontap: "destroyPanel"}
			]}
		],			
	},	
	components: [
		{kind: "Panels", fit: true, touch: true, classes: "panels-sample-sliding-panels", arrangerKind: "CollapsingArranger", wrap: false, components: [
			{name: "left", components: [
				{kind: "Scroller", classes: "enyo-fit", touch: true, components: [
					{kind: "onyx.Toolbar", components: [
						{content: "N3phele"},
						{fit: true}]},
					{kind:"Image", src:"assets/cloud-theme.gif", fit: true},					
					{kind: "List", fit: true, touch:true, count:4, onSetupItem: "setupItemMenu", components: [
						{name: "menu_item",	style: "padding: 10px;", classes: "panels-sample-flickr-item", ontap: "itemTapMenu", components: [
							{name: "menu_option",kind:"Image"}]},
					]},
					
				]}
			]},
			{name: "imageIcon", kind: "Scroller" },			
        ],
			destroyPanel: function(inSender, inEvent) {
				this.setIndex(2);				
				this.getActive().destroy();					
				this.panelCreated = false;
				this.setIndex(0);
				this.reflow();
				
		},
			
		}
	],
	
	setupButton: function(inSender, inEvent) {
		this.$.item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.t.setContent({kind: "onyx.Button", ontap:"itemTapMenu", components: [
					{kind: "onyx.Icon", src: "https://github.com/enyojs/enyo/wiki/assets/fish_bowl.png"}
					]});
	},
	menu:["Files","Commands","Acvity History","Accounts"],	
	nepheleImages:["./assets/concatenate.gif"
	, "./assets/search-input-search.png", 
	"./assets/search-input-search.png",
	"./assets/Untar.gif"],
	commandPanels:["concPanel","copyPanel","impPanel","expPanel"],
	commands:["Concatenate","Copy","Import","Export"],
	closePanel: function(){
	alert("Closing Panel");
		this.$.panels.setIndex(0);
		this.destroyPanel();
	},
	
	setupItem: function(inSender, inEvent) {
		// given some available data.
		this.$.item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.t.setContent(this.menu[inEvent.index]);
	},
	
	setupItemMenu: function(inSender, inEvent) {
		// given some available data.
		this.$.menu_item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.menu_option.setContent(this.menu[inEvent.index]);
	},
	concPanel: function(inSender, inEvent) {
		if(!this.$.panels.panelCreated){
			this.$.panels.panelCreated =true;
			b = this.$.panels;
			p = b.createComponent( 
				this.c1
			);
			p.render();
			b.reflow();
			this.contContent = p
			this.$.panels.setIndex(1);
			
			this.$.panels.activePanel = "contContent";
		}
	},
	impPanel: function(inSender, inEvent) {
		alert("Import Panel");
	},
	copyPanel: function(inSender, inEvent) {
		alert("Copy Panel");
	},
	expPanel: function(inSender, inEvent) {
		console.log(this);
		alert("Export Panel");
	},	
	itemTapMenu: function(inSender, inEvent) {
		if (enyo.Panels.isScreenNarrow()) {
			this.$.panels.setIndex(1);
		}		

		if(this.$.panels.panelCreated)this.$.panels.destroyPanel();
		
		if(inEvent.index == 0){
			this.$.imageIcon.destroyClientControls();
			this.createComponent({kind: "onyx.Toolbar", container: this.$.imageIcon,components: [
						{content: "Files"},
						{fit: true}]}
			);
			this.$.imageIcon.render();
		
		}else if(inEvent.index == 1){
			this.build();
		}else if(inEvent.index == 2){
			this.$.imageIcon.destroyClientControls();
			this.createComponent({kind: "onyx.Toolbar", container: this.$.imageIcon,components: [
						{content: "Activity History"},
						{fit: true}]}
			);
			this.$.imageIcon.render();		
		}else if(inEvent.index == 3){
			this.$.imageIcon.destroyClientControls();
			this.createComponent({kind: "onyx.Toolbar", container: this.$.imageIcon,components: [
						{content: "Accounts"},
						{fit: true}]}
			);
			this.$.imageIcon.render();		
		}
	},	

	build: function() {
        this.$.imageIcon.destroyClientControls();
		this.createComponent({kind: "onyx.Toolbar", container: this.$.imageIcon,components: [
						{content: "Commands"},
						{fit: true}]}
		);
        //};
		this.createComponent( 
			{ name: "IconGallery", kind: "IconList", container: this.$.imageIcon ,onSelectedItem: "selectedItem" , nepheleImages: this.nepheleImages, commands: this.commands, retrieveContentData: function() { this.data = createCommandItems(this.commands, this.nepheleImages); } } 
		);
		
        this.$.imageIcon.render();
    },
	selectedItem: function(inSender, inEvent) {
		console.log("One item was selected on APP : " + inEvent.name );
			
		for( var i in this.commands )
		{
			if (this.commands[i] == inEvent.name)
			{
				eval('this.' + this.commandPanels[i] + '()');
			}
		}		
	},
	createPanel: function(inEvent) {	
		if(!this.panelCreated){
			b = this.$.panels;
				p = b.createComponent( 
					this.c1
				);
				p.render();
				b.reflow();
				this.conContent = p;
				this.createBackButton();
				this.$.panels.setIndex(1);
			}
	}
});