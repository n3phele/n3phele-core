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
	destroyPanel: function() {
		if(this.panelCreated)
		{
			this.panel_three.destroy();			
			this.panelCreated = false;
			this.$.panels.reflow();
		}
	},
	buttonTapped: function(inSender, inEvent) {
		// respond to the tap event
		this.$.panels.setIndex(0);
		this.destroyPanel();
	},
	c1:{ 
			name: "panel_three",
			style: "width: 800px" ,
			classes: "panels-sample-sliding-content", 
			content: "Broke, down dumb hospitality firewood chitlins. Has mud tired uncle everlastin' cold, out. Hauled thar, up thar tar heffer quarrel farmer fish water is. Simple gritts dogs soap give kickin'. Ain't shiney water range, preacher java rent thar go. Skinned wirey tin farm, trespassin' it, rodeo. Said roped caught creosote go simple. Buffalo butt, jig fried commencin' cipherin' maw, wash. Round-up barefoot jest bible rottgut sittin' trailer shed jezebel. Crop old over poker drinkin' dirt where tools skinned, city-slickers tools liniment mush tarnation. Truck lyin' snakeoil creosote, old a inbred pudneer, slap dirty cain't. Hairy, smokin', nothin' highway hootch pigs drinkin', barefoot bootleg hoosegow mule. Tax-collectors uncle wuz, maw watchin' had jumpin' got redblooded gimmie truck shootin' askin' hootch. No fat ails fire soap cabin jail, reckon if trespassin' fixin' rustle jest liniment. Ya huntin' catfish shot good bankrupt. Fishin' sherrif has, fat cooked shed old. Broke, down dumb hospitality firewood chitlins. Has mud tired uncle everlastin' cold, out. Hauled thar, up thar tar heffer quarrel farmer fish water is. Simple gritts dogs soap give kickin'. Ain't shiney water range, preacher java rent thar go. Skinned wirey tin farm, trespassin' it, rodeo. Said roped caught creosote go simple. Buffalo butt, jig fried commencin' cipherin' maw, wash. Round-up barefoot jest bible rottgut sittin' trailer shed jezebel. Crop old over poker drinkin' dirt where tools skinned, city-slickers tools liniment mush tarnation. Truck lyin' snakeoil creosote, old a inbred pudneer, slap dirty cain't. Hairy, smokin', nothin' highway hootch pigs drinkin', barefoot bootleg hoosegow mule. Tax-collectors uncle wuz, maw watchin' had jumpin' got redblooded gimmie truck shootin' askin' hootch. No fat ails fire soap cabin jail, reckon if trespassin' fixin' rustle jest liniment. Ya huntin' catfish shot good bankrupt. Fishin' sherrif has, fat cooked shed old. Broke, down dumb hospitality firewood chitlins. Has mud tired uncle everlastin' cold, out. Hauled thar, up thar tar heffer quarrel farmer fish water is. Simple gritts dogs soap give kickin'. Ain't shiney water range, preacher java rent thar go. Skinned wirey tin farm, trespassin' it, rodeo. Said roped caught creosote go simple. Buffalo butt, jig fried commencin' cipherin' maw, wash. Round-up barefoot jest bible rottgut sittin' trailer shed jezebel. Crop old over poker drinkin' dirt where tools skinned, city-slickers tools liniment mush tarnation. Truck lyin' snakeoil creosote, old a inbred pudneer, slap dirty cain't. Hairy, smokin', nothin' highway hootch pigs drinkin', barefoot bootleg hoosegow mule. Tax-collectors uncle wuz, maw watchin' had jumpin' got redblooded gimmie truck shootin' askin' hootch. No fat ails fire soap cabin jail, reckon if trespassin' fixin' rustle jest liniment. Ya huntin' catfish shot good bankrupt. Fishin' sherrif has, fat cooked shed old. Broke, down dumb hospitality firewood chitlins. Has mud tired uncle everlastin' cold, out. Hauled thar, up thar tar heffer quarrel farmer fish water is. Simple gritts dogs soap give kickin'. Ain't shiney water range, preacher java rent thar go. Skinned wirey tin farm, trespassin' it, rodeo. Said roped caught creosote go simple. Buffalo butt, jig fried commencin' cipherin' maw, wash. Round-up barefoot jest bible rottgut sittin' trailer shed jezebel. Crop old over poker drinkin' dirt where tools skinned, city-slickers tools liniment mush tarnation. Truck lyin' snakeoil creosote, old a inbred pudneer, slap dirty cain't. Hairy, smokin', nothin' highway hootch pigs drinkin', barefoot bootleg hoosegow mule. Tax-collectors uncle wuz, maw watchin' had jumpin' got redblooded gimmie truck shootin' askin' hootch. No fat ails fire soap cabin jail, reckon if trespassin' fixin' rustle jest liniment. Ya huntin' catfish shot good bankrupt. Fishin' sherrif has, fat cooked shed old."
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
        ]
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
	
		panelCreated =true;
		
		b = this.$.panels;
		p = b.createComponent( 
			this.c1
		);
		p.render();
		b.reflow();
		this.panel_three = p
		this.$.panels.setIndex(1);
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
		alert("You tapped on row: " + inEvent.index);
		
		if (enyo.Panels.isScreenNarrow()) {
			this.$.panels.setIndex(1);
		}		

		if(this.panelCreated)this.destroyPanel();
		
		if(inEvent.index == 1){
			this.build();
			/*if(!this.panelCreated)
			{
				this.createPanel(inEvent);
			}
			else
			{
				this.destroyPanel();
			}*/
		}
	},	
		
	createBackButton: function() {
		panel = this.$.body;
		//b = panel.createComponent( 
		//	{kind: "onyx.Button", content: "tap me", ontap: "buttonTapped", style:"position:absolute; top:100px;"}
		//);
		b.render();
		panel.reflow();
	},	
	build: function() {
        this.$.imageIcon.destroyClientControls();
        //for (var i=0; i<4; i++) {
        //    this.createComponent({kind: "onyx.Button", container: this.$.imageIcon, ontap: this.commandPanels[i], index: i, pack: "center", align: "center",components: [
		//							{kind: "onyx.Icon", src: this.nepheleImages[i]}]
		//						});  			
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
	
		alert("Creating panel");
		if(!this.panelCreated){
			b = this.$.panels;
				p = b.createComponent( 
					this.c1
				);
				p.render();
				b.reflow();
				this.panel_three = p;
				this.createBackButton();
				this.$.panels.setIndex(1);
			}
		
		/*if(!this.panelCreated)
		{
			this.panelCreated = true;
			b = this.imageIcon;
			p = b.createComponent( 
				//this.c1
				this.p3
			);
			p.render();
			b.reflow();
			//this.panel_three = p;
			this.imageIcon = p;
			this.createBackButton();
			this.$.panels.setIndex(1);
		}*/
	}
});