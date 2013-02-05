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
			
			{name: "imageIcon"},			
        ],
			destroyPanel: function(inSender, inEvent) {
				alert("Destroying panel");
				this.$.contContent.destroy();		
				//this.activePanel.destroy();					
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
		//else{
			//this.destroyPanel();
			//}	
		
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