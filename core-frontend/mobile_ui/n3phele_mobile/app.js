var createCommandItems = function(arrayOfCommands, arrayOfImages) {
	list = [];
	for (var i in arrayOfCommands)
	{
		var widget = { name: arrayOfCommands[i], displayName: arrayOfCommands[i], image: arrayOfImages[i] };
		list.push(widget);
	}
	return list;
}

/*Main painels*/
enyo.kind({
	name: "com.N3phele",
	kind: "FittableRows",
	classes: "onyx enyo-fit",
	components: [
		{name:"N3pheleUid", style: "display:none"},
		{kind: "Panels", panelCreated : false, fit: true, touch: true, classes: "panels-sample-sliding-panels", arrangerKind: "CollapsingArranger", wrap: false, components: [
			{name: "left", components: [
				{kind: "Scroller", classes: "enyo-fit", touch: true, components: [
					
					{kind: "onyx.Toolbar", components: [ {content: "N3phele"}, {fit: true} ]}, //Panel Title
					
					{name: "mainMenuPanel", style:"width:90%;margin:auto", components:[//div to align content
						
						{kind:"Image", src:"assets/cloud-theme.gif", fit: true, style:  "padding-left:30px; padding-top: 30px;"},
						{classes: "onyx-sample-divider", content: "Main Menu", style: "color: #375d8c"},					
						{kind: "List", fit: true, touch:true, count:4, style: "height:"+(4*65)+"px", onSetupItem: "setupItemMenu", components: [
							{name: "menu_item",	classes: "panels-sample-flickr-item", ontap: "itemTapMenu", style: "box-shadow: -4px 0px 4px rgba(0,0,0,0.3);", components: [
								{name:"menu_image", kind:"Image"},
								{name: "menu_option",kind:"Image"}]},
						]}
					]}// end mainMenuPanel
				]}//end scroller
			]},
			{name: "imageIconPanel", kind:"FittableRows", fit:true, components:[
				{name: "imageIcon",kind: "enyo.Scroller"}
			]}			
		],
		destroyPanel: function(inSender, inEvent) {
			this.setIndex(2);				
			this.getActive().destroy();					
			this.panelCreated = false;
			
			if (enyo.Panels.isScreenNarrow()) {
				this.setIndex(1);
			}
			else {
				this.setIndex(0);
			}		
			
			this.reflow();
			
			this.owner.$.IconGallery.deselectLastItem();
		}			
	}
	],	
	setupButton: function(inSender, inEvent) {
		this.$.item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.t.setContent({kind: "onyx.Button", ontap:"itemTapMenu", components: [
					{kind: "onyx.Icon", src: "https://github.com/enyojs/enyo/wiki/assets/fish_bowl.png"}
					]});
	},
	menu:["Files","Commands","Acvity History","Accounts"],	
	menuImages:["./assets/files.png","./assets/commands.png","./assets/activityHistory.png","./assets/accounts.png"],
	nepheleImages:["./assets/nogloss.gif"
	, "./assets/fileCopy.gif", 
	"./assets/Import.gif",
	"./assets/export.gif"],
	commandPanels:["concPanel","copyPanel","impPanel","expPanel"],
	commands:["Concatenate","Copy","Import","Export"],
	closePanel: function(){
	alert("Closing Panel");
		this.$.panels.setIndex(0);
		this.destroyPanel();
	},	
	setupItemMenu: function(inSender, inEvent) {
		// given some available data.
		this.$.menu_item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.menu_image.setSrc(this.menuImages[inEvent.index]);
		this.$.menu_option.setContent(this.menu[inEvent.index]);
		
	},
	concPanel: function(inSender, inEvent) {
		if(!this.$.panels.panelCreated){
			this.$.panels.panelCreated =true;
			var panel = this.$.panels;
			var concPage = panel.createComponent(
				new concatPage({classes: "enyo-unselectable"})
				//this.c1
			);
			
			/* -Begin- Generating Table Lines ****************************************************************************************/
			var names = "";
			for (var i=0; i<2; i++){
				concPage.$.concatInFiles.createComponent({
					kind: ConcatLineElem,
					filename: "file"+i+".jpg",
					msg: "You have not specified the file!"
				});
				
				names += "file"+i+".jpg, ";
			}
			
			concPage.$.concatOutFiles.createComponent({
					kind: ConcatLineElem,
					filename: "concatenation of " + names,
					msg: "You have not specified the file!"
			});
			
			for (var i=0; i<2; i++){
				concPage.$.concatRun.createComponent({
						kind: ConcatExecLine,
						name: "Machine"+i,
						zone: "zone"+i,
						settings: "standard.small",
						send: "ok"
				});
			}
			
			concPage.$.concatRun.createComponent({ kind: ConcatExecFinal }); 
			/* -End- Generating Table Lines ****************************************************************************************/
			concPage.render();
			panel.reflow();
			this.contContent = concPage;
			
			if (enyo.Panels.isScreenNarrow()) {
				this.$.panels.setIndex(2);
			}
			else {
				this.$.panels.setIndex(1);
			}
			
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
		alert("Export Panel");
	},	
	itemTapMenu: function(inSender, inEvent) {
		if (enyo.Panels.isScreenNarrow()) {
			this.$.panels.setIndex(1);
		}		

		if(this.$.panels.panelCreated)this.$.panels.destroyPanel();
		
		if(inEvent.index == 0){
			this.$.imageIconPanel.destroyClientControls();
			this.createComponent({kind: "onyx.Toolbar", container: this.$.imageIconPanel,components: [
						{content: "Files"},
						{fit: true}]}
			);
			this.$.imageIconPanel.render();
		
		}else if(inEvent.index == 1){
			this.build();
		}else if(inEvent.index == 2){
			this.$.imageIconPanel.destroyClientControls();
			this.createComponent({kind: "onyx.Toolbar", container: this.$.imageIconPanel,components: [
						{content: "Activity History"},
						{fit: true}]}
			);
			this.$.imageIconPanel.render();		
		}else if(inEvent.index == 3){
			this.$.imageIconPanel.destroyClientControls();
			this.createComponent({kind: "onyx.Toolbar", container: this.$.imageIconPanel,components: [
						{content: "Accounts"},
						{fit: true}]}
			);
			this.$.imageIconPanel.render();		
		}
	},	
	
	backMenu: function(){
		this.$.panels.setIndex(0);
	},

	build: function() {
        this.$.imageIconPanel.destroyClientControls();
		
		this.createComponent({name:"toolComm", kind: "onyx.Toolbar", container: this.$.imageIconPanel,components: [
							{content: "Commands"},
							{fit: true}]}
			);		
		
		this.createComponent( 
			{ name: "IconGallery", kind: "IconList",container: this.$.imageIconPanel, onDeselectedItems: "closeThirdPanel" , onSelectedItem: "selectedItem" , nepheleImages: this.nepheleImages, commands: this.commands, retrieveContentData: function() { this.data = createCommandItems(this.commands, this.nepheleImages); } } 
		);		
		
		
		if (enyo.Panels.isScreenNarrow()) {
		this.createComponent({kind: "onyx.Toolbar",container: this.$.imageIconPanel, components: [
				{kind: "onyx.Button", content: "Close", ontap: "backMenu"}
			]});
		}
		else{
		this.createComponent({kind: "onyx.Toolbar",container: this.$.imageIconPanel});
		}
		
        this.$.imageIconPanel.render();
    },
	closeThirdPanel: function() {
		if ( this.$.panels.panelCreated )
		{
			this.$.panels.setIndex(2);
			this.$.panels.getActive().destroy();
			this.$.panels.panelCreated = false;
			this.$.panels.setIndex(0);
			this.$.panels.reflow();
			this.$.IconGallery.deselectLastItem();
		}
	}
	,
	selectedItem: function(inSender, inEvent) {
		console.log("One command was selected on APP : " + inEvent.name );
		
		for( var i in this.commands )
		{
			if (this.commands[i] == inEvent.name)
			{
				eval('this.' + this.commandPanels[i] + '()');
			}
		}		
	},
	create: function() {
		this.inherited(arguments);
		this.$.N3pheleUid.setContent( this.uid );
		this.$.mainMenuPanel.createComponent({ kind: "RecentActivityList", 'uid' : this.uid});
	}
});