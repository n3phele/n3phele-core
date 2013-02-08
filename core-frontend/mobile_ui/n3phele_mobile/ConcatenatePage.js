enyo.kind({ 
	name:"ConcatLineElem", 
	published: { filename: "", msg: "" },
	components:[
		{ classes: "concatInternLine", components:[
			{ name: "filename",	content: "ND"},
		]},
		{ classes: "concatInternLine", style: "text-align:right", components:[
			{name:"msg", content: "ND" , style: "margin: 2px 0px;"},
			{tag: "br"},
			{kind:"onyx.Button", content: "File", ontap:"selectFile", style: "margin: 2px 0px;"}
		]}	
	],
	create: function() {
		this.inherited(arguments);
		this.$.filename.setContent(this.filename);
		this.$.msg.setContent(this.msg);
	},
	selectFile: function() {
	// Retrieve image file location from specified source
		navigator.camera.getPicture( 
				this.onSuccessFileSelection, 
				function(message) { alert('get file failed'); },
				{ 
					quality: 50, 
					destinationType: navigator.camera.DestinationType.FILE_URI,
					sourceType: navigator.camera.PictureSourceType.PHOTOLIBRARY,
					mediaType: navigator.camera.MediaType.ALLMEDIA 
				}
		);		
	},
	onSuccessFileSelection: function(imageData) {
	    console.log(imageData);
	},
});

enyo.kind({ 
	name:"ConcatExecLine", 
	published: { mcName: "", zone: "", setting:"", send:"" },
	classes : "concatExec",
	components:[
		{ classes: "concatExecLine onyx-sample-tools", style:"width:34%", ontap: "clickExecLine", components: [
				{kind:"onyx.Checkbox", name: "concatCheck"},
				{name: "name",  style: "display:inline-block"}
		]},
		{ name: "zone", classes: "concatExecLine", style:"width:26%"},
		{ name: "settings", classes: "concatExecLine", style:"width:25%"},
		{ name: "send", classes: "concatExecLine", style:"width:11%", components:[
			{kind:"onyx.Checkbox", onChange:"toggleChanged"}
		]}
		
	],
	create: function() {
		this.inherited(arguments);
		this.$.name.setContent(this.name);
		this.$.zone.setContent(this.zone);
		this.$.settings.setContent(this.settings);
		//this.$.send.setContent(this.send);
	},
	clickExecLine: function(inSender, inEvent){
		var oldValue = this.$.concatCheck.getValue();
		this.$.concatCheck.setValue(!oldValue);
	}
});

enyo.kind({ 
	name: "ConcatExecFinal",
	id: "ConcatExecFinal",
	content: "Algo",
	classes: "concatExecFinal",
	components:[
		{kind: "onyx.InputDecorator", style: "background-color:white; width:50%; display: inline-block; margin-right:10px; ", components: [
				{kind: "onyx.Input", placeholder: "Enter Job Name", onchange:"inputChanged"}
		]},
		{kind:"onyx.Button", content: "Run", style: "margin-right:10px;"},
		{kind:"onyx.Button", content: "Cancel"}
	]
});

enyo.kind({ 
	name:"concatPage",
	kind: "FittableRows",
	fit: true,
	classes: "onyx onyx-sample",
	style: "padding: 0px",
	components:[
			{name: "topToolbar",kind: "onyx.Toolbar", components: [ {content: "Concatenate"}, {fit: true}]},
			{kind: "enyo.Scroller", fit: true, components: [
				{name: "panel_three", classes: "panels-sample-sliding-content", allowHtml: true, components:[
						{tag: "br"},
						{name:"concatInFiles", id:"concatInFiles", classes: "concatTable", kind: "onyx.Groupbox", components: [
							{kind: "onyx.GroupboxHeader", classes: "header", content: "Input Files"},
							{classes: "subheader", components:[ 
								{content: "Filename", classes: "subsubheader" } , 
								{content: "Status", classes: "subsubheader"} 
							]}

						]},		
						{tag: "br"},
						
						{name:"concatOutFiles", id:"concatOutFiles", classes: "concatTable", kind: "onyx.Groupbox", components: [
							{kind: "onyx.GroupboxHeader", classes: "header", content: "Output Files"},
							{classes: "subheader", components:[ 
								{content: "Filename", classes: "subsubheader" } , 
								{content: "Status", classes: "subsubheader"} 
							]}
						]},		
						{tag: "br"},
						
						{name:"concatRun", id: "concatRun", classes: "concatTable", kind: "onyx.Groupbox", components: [
							{kind: "onyx.GroupboxHeader", classes: "header", content: "Execute On"},
							{classes: "subheader", style: "padding:0px", components:[ 
								{content: "Machine name", classes: "subsubheader", style:"width:34%"} , 
								{content: "Zone", classes: "subsubheader", style:"width:26%" } , 
								{content: "Machine settings", classes: "subsubheader",  style:"width:25%" } ,
								{content: "Send Email?", classes: "subsubheader",  style:"width:11%" } ,
							]}
						]},		
						{tag: "br"}
				]},
		]},
		{kind: "onyx.Toolbar", components: [ {kind: "onyx.Button", content: "Close", ontap: "destroyPanel"} ]},
		//{kind: "onyx.Toolbar", components: [ {kind: "onyx.Button", content: "Close", ontap: "closeCPanel"} ]}

	],	
	destroyPanel:function(inSender){
			//alert("Closing Panel");
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
});
//nome, zona, configuração, send
					
