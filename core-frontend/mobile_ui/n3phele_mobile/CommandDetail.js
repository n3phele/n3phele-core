enyo.kind({ 
	name:"CommandDetail",
	kind: "FittableRows",
	fit: true,
	classes: "onyx onyx-sample",
	style: "padding: 0px",
	components:[
		{kind: "onyx.Toolbar", components: [ { name: "title" }, {fit: true}]},
		{kind: "enyo.Scroller", fit: true, components: [
				{name: "panel_three", classes: "panels-sample-sliding-content", allowHtml: true, components:[
					{tag:"img", src:"./assets/info.png", style: "display: inline-block;margin: 2px;"},
					{name:"description",content: "Main Menu", style: "color: #63B8FF;display: inline-block; font-size: 16px;"},
					{tag: "br"},
					
				]}
		]},
		{kind: "onyx.Toolbar", components: [ {kind: "onyx.Button", content: "Close", ontap: "closePanel"} ]},
		
	
	
	],
	create: function(){
		this.inherited(arguments);
		this.$.title.setContent(this.data.name);
		this.$.description.setContent(this.data.description);
		
		if(typeof this.data.inputFiles != 'undefined'){
			this.$.panel_three.createComponent({
				name:"concatInFiles",
				classes: "concatTable",
				style: "margin:4px",
				kind: "onyx.Groupbox",
				components: [
					{kind: "onyx.GroupboxHeader", classes: "groupboxBlueHeader", content: "Input Files"},
					{classes: "subheader", components:[ 
						{content: "Filename", classes: "subsubheader" } , 
						{content: "Status", classes: "subsubheader"} 
					]},
					{components:[
						{ classes: "concatInternLine", components:[
							{ name: "filename",	content: "Filename"},
						]},
						{ classes: "concatInternLine", style: "text-align:right", components:[
							{name:"msg", content: "some message" , style: "margin: 2px 0px;"},
							{tag: "br"},
							{kind:"onyx.Button", content: "File", ontap:"selectFile", style: "margin: 2px 0px;"}
						]}	
					]}
				]
			});
			this.$.panel_three.reflow();
		}// end if(typeof this.data.inputFiles != 'undefined')
		
		if(typeof this.data.outputFiles != 'undefined'){
			this.$.panel_three.createComponent({
				name:"concatOutFiles",
				classes: "concatTable",
				style: "margin: 4px",
				kind: "onyx.Groupbox",
				components: [
					{kind: "onyx.GroupboxHeader", classes: "groupboxBlueHeader", content: "Output Files"},
					{classes: "subheader", components:[ 
						{content: "Filename", classes: "subsubheader" } , 
						{content: "Status", classes: "subsubheader"} 
					]},
					{components:[
						{ classes: "concatInternLine", components:[
							{ name: "filename",	content: "Filename"},
						]},
						{ classes: "concatInternLine", style: "text-align:right", components:[
							{name:"msg", content: "some message" , style: "margin: 2px 0px;"},
							{tag: "br"},
							{kind:"onyx.Button", content: "File", ontap:"selectFile", style: "margin: 2px 0px;"}
						]}	
					]}
				]
			});
			this.$.panel_three.reflow();
		}
		
		console.log("painel", this.data, (typeof this.data.inputFiles != 'undefined'), (typeof this.data.outputFiles != 'undefined'));
	},
	closePanel: function(inSender, inEvent){
			var panel = inSender.parent.parent.parent;
			
			panel.setIndex(2);				
			panel.getActive().destroy();					
			panel.panelCreated = false;
			
			if (enyo.Panels.isScreenNarrow()) {
				panel.setIndex(1);
			}
			else {
				panel.setIndex(0);
			}		
			
			panel.reflow();		
			panel.owner.$.IconGallery.deselectLastItem();			
	}
	
})