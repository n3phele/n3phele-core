enyo.kind({
	name: "enyo.sample.PanelsSlidingSample",
	kind: "FittableRows",
	classes: "onyx enyo-fit",
	
	
	buttonTapped: function(inSender, inEvent) {
		// respond to the tap event
		this.$.panels.setIndex(0);
		this.destroyPanel();
	},
	c1:{ 
			name: "panel_three",
			classes: "panels-sample-sliding-content", components: [
			{name: "left", components: [
				{kind: "Scroller", classes: "enyo-fit", touch: true, components: [
					{kind: "onyx.Toolbar", components: [
						{content: "Concatenate"},
						{fit: true}]
					},
					{name: "description", content: "Concatenate up to 8 files", style: "text-align:center; padding: 10px;"},					
					]
				}]
			}
			]
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
			{name: "imageIcon"},			
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
	nepheleImages:["C:/Users/LIS/Desktop/bootplate/lib/layout/panels/samples/assets/teste.png"
	, "C:/Users/LIS/Desktop/bootplate/lib/layout/panels/samples/assets/search-input-search.png", 
	"C:/Users/LIS/Desktop/bootplate/lib/layout/panels/samples/assets/search-input-search.png",
	"C:/Users/LIS/Desktop/bootplate/lib/layout/panels/samples/assets/search-input-search.png"],
	commandPanels:["concPanel","copyPanel","impPanel","expPanel"],
	closePanel: function(){
	alert("Closing Panel");
		this.$.panels.setIndex(0);
		this.destroyPanel();
	},
	destroyPanel: function() {
		//if(this.panelCreated)
		//{
			this.panel_three.destroy();			
			this.panelCreated = false;
			this.$.panels.setIndex(0);
			this.$.panels.reflow();
		//}
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
		if(!this.panelCreated){
			this.panelCreated =true;
			b = this.$.panels;
			p = b.createComponent( 
				this.c1
			);
			p.render();
			b.reflow();
			this.panel_three = p
			this.$.panels.setIndex(1);
		}
		else{
			this.destroyPanel();
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
		this.createComponent({kind: "onyx.Toolbar", container: this.$.imageIcon,components: [
						{content: "Commands"},
						{fit: true}]}
		);
        for (var i=0; i<4; i++) {
            this.createComponent({kind: "onyx.Button", container: this.$.imageIcon, ontap: this.commandPanels[i], index: i, pack: "center", align: "center",components: [
									{kind: "onyx.Icon", src: this.nepheleImages[i]}]
								});  			
        };
		
        this.$.imageIcon.render();
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