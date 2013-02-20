enyo.kind({ 
	name:"ConcatLineElem", 
	published: { filename: "", msg: "" },
	components:[
		{ classes: "concatInternLine", components:[
			{ name: "filename",	content: "ND"},
		]},
		{ classes: "concatInternLine", style: "text-align:right", components:[
			{name:"msg", content: "ND"},
			{tag: "br"},
			{kind:"onyx.Button", content: "File", ontap:"buttonTapped"}
		]}	
	],
	create: function() {
		this.inherited(arguments);
		this.$.filename.setContent(this.filename);
		this.$.msg.setContent(this.msg);
	},
	buttonTapped: function(){ console.log("Foi botão");}
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
		{ name: "zone", classes: "concatExecLine", style:"width:29%"},
		{ name: "settings", classes: "concatExecLine", style:"width:25%"},
		{ name: "send", classes: "concatExecLine", style:"width:8%", components:[
			{kind:"onyx.ToggleButton", onChange:"toggleChanged"}
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
	name: "concatPage",
	classes: "onyx onyx-sample",
	components: [
		{classes: "onyx-sample-divider", content: "Command: Concatenate"},
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
				{content: "Zone", classes: "subsubheader", style:"width:29%" } , 
				{content: "Machine settings", classes: "subsubheader",  style:"width:25%" } ,
				{content: "Send Email?", classes: "subsubheader",  style:"width:8%" } ,
			]}
		]},		
		{tag: "br"},
		

	]
});
//nome, zona, configuração, send
					
