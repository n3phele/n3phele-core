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
					]}
				]}
			]},
			{name: "imageIcon", kind: "Image", classes: "enyo-fit panels-sample-flickr-center panels-sample-flickr-image", style: 'height: 5%;width:3%;'}			
        ]
		}
	],
	menu:["Files","Commands","Acvity History","Accounts"],	
	setupItem: function(inSender, inEvent) {
		// given some available data.
		this.$.item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.t.setContent(this.menu[inEvent.index]);
	},
	setupCell:function(sender, event) {
        this.$.btn.setContent("Button #" + event.index);
    },
	setupItemMenu: function(inSender, inEvent) {
		// given some available data.
		this.$.menu_item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.menu_option.setContent(this.menu[inEvent.index]);
	},
	itemTapMenu: function(inSender, inEvent) {
		//alert("You tapped on row: " + inEvent.index);
		
		if (enyo.Panels.isScreenNarrow()) {
			this.$.panels.setIndex(1);
		}	
		
		//
		this.$.
		.setSrc("assets/iconNephele.png");
		if(!this.panelCreated)
		{
			this.createPanel(inEvent);
		}
		else
		{
			this.destroyPanel();
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
	createPanel: function(inEvent) {
		if(!this.panelCreated)
		{
			this.panelCreated = true;
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
	}
});