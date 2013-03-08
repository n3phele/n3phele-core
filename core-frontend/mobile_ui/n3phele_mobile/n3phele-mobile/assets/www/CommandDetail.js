var execCloudName = "34" ;
var execCloudZone = "26" ;
var execCloudSett = "25" ;
var execCloudEmail = "11" ;

/** Upload Function **/
var uploadFile = function() {
// Retrieve image file location from specified source
	navigator.camera.getPicture( 
			onSuccessFileUp, 
			function(message) { alert('get file failed'); },
			{ 
				quality: 50, 
				destinationType: navigator.camera.DestinationType.FILE_URI,
				sourceType: navigator.camera.PictureSourceType.PHOTOLIBRARY,
				mediaType: navigator.camera.MediaType.ALLMEDIA 
			}
	);		
};
var onSuccessFileUp = function(imageData){ alert(imageData); }

/*** The Groubbox of parameters ****/
enyo.kind({
	name: "commandParamGroup",
	components:[
		{tag: "br"},
		{name: "groupbox", classes: "commandTable", kind: "onyx.Groupbox", components: [
			{name: "header", kind: "onyx.GroupboxHeader", classes: "groupboxBlueHeader", content: "Parameters"},//header
			{classes: "subheader", components:[ //subheader
				{content: "Type of data", style: "width:70%", classes: "subsubheader" } , 
				{content: "Value", style: "width:30%", classes: "subsubheader"} 
			]}
		]}//end groupbox
	],//end components inFilesList
	create: function(){
		this.inherited(arguments);
		
		if(typeof this.params == 'undefined') return;//checking if the lines informations are set
		this.params = fixArrayInformation(this.params);
		this.initializeLines(this.params);
	},
	initializeLines: function( linesInfo ){
		for( var i in linesInfo ){
			var fieldType = "";
			
		
			var line = this.$.groupbox.createComponent({
					classes: "commandFilesLine", style: "padding: 4px 4px",
					components: [
					             {tag:"div", style: "width:70%", content: linesInfo[i].description.replace(","," , ") },
					             {name: "field", tag:"div", style: "width:30%"}
					]
				});// end this.$.groupbox.createComponent
			
			//Set the field that will gat the parameter needed	
			switch( linesInfo[i].type.toLowerCase() ){
				case "long":
					line.owner.$.field.createComponent({name:"item"+i, kind: "onyx.InputDecorator",  style:"background-color:white", components: [ {kind: "onyx.Input", value: linesInfo[i].defaultValue } ] });
				break;
				case "boolean":
					line.owner.$.field.createComponent({name:"item"+i,kind: "onyx.Checkbox", value: linesInfo[i].defaultValue});
				break;
				case "string":
					line.owner.$.field.createComponent({name:"item"+i, kind: "onyx.InputDecorator",  style:"background-color:white", components: [ {kind: "onyx.Input", value: linesInfo[i].defaultValue } ] });
				break;
			}//end switch
		}//end for
	}//end initializeLines
});

/*** The Groubbox of Input or output files ****/
enyo.kind({
	name: "commandFilesGroup",
	components:[
		{tag: "br"},
		{name: "groupbox", classes: "commandTable", kind: "onyx.Groupbox", components: [
			{name: "header", kind: "onyx.GroupboxHeader", classes: "groupboxBlueHeader", content: "Title"},//header
			{classes: "subheader", components:[ //subheader
				{content: "Filename", classes: "subsubheader" } , 
				{content: "Status", classes: "subsubheader"} 
			]}
		]}//end groupbox
	],//end components inFilesList
	create: function(){
		this.inherited(arguments);
		
		//checking the type of groupbox
		if(this.type == 'output')
			this.$.header.setContent( "Output Files" );
		else
			this.$.header.setContent( "Input Files" );
		
		if(typeof this.lines == 'undefined') return;//checking if the lines informations are set
		
		//correcting the type of information
		if( !(this.lines instanceof Array) ){
			
			var aux = this.lines;
			this.lines = new Array();
			this.lines.push(aux);
		}

		this.initializeLines( this.lines );
	},
	initializeLines: function( linesInfo ){
		for( var i in linesInfo ){
			this.$.groupbox.createComponent({ kind: "commandFilesLine", data: linesInfo[i], type: this.type });
		}
	}
});

/*** The Groubbox that defines the cloud to execute the command ****/
enyo.kind({ 
	name: "commandExecGroup",
	components:[
		{tag: "br"},
		{name: "groupbox", classes: "commandTable", kind: "onyx.Groupbox", components: [
			{name: "header", kind: "onyx.GroupboxHeader", classes: "groupboxBlueHeader", content: "Execute on"},//header
			{classes: "subheader", components:[ //subheader
                {content: "Machine name", classes: "subsubheader", style:"width:"+execCloudName+"%"} , 
				{content: "Zone", classes: "subsubheader", style:"width:"+execCloudZone+"%" } , 
				{content: "Machine settings", classes: "subsubheader",  style:"width:"+execCloudSett+"%" } ,
				{content: "Send Email?", classes: "subsubheader",  style:"width:"+execCloudEmail+"%" } ,
			]}
		]}//end groupbox
	],//end components inFilesList
	create: function(){
		this.inherited(arguments);

		if(!this.lines)
			return;
		
		if( this.lines.length == 0 ){
			this.addEmptyLine();
		}else{
			this.addLines(this.lines);
			this.insertLastLine();
		}
	},
	addLines: function( linesInfo ){//addlines from an array
			
		for( var i in linesInfo ){
			this.$.groupbox.createComponent({ kind: "commandExecLine", data: linesInfo[i] });
		}
	},
	addEmptyLine:function(){//there is not clouds available
		this.$.groupbox.createComponent({content:"There is not cloud available for this operation!", style:"text-align:center; padding:4px; font-weight:bold"});
	},
	insertLastLine: function(){
		this.$.groupbox.createComponent({components:[
 		    {kind: "onyx.InputDecorator", style: "background-color:white; width:50%; display: inline-block; margin-right:10px; ", components: [
       				{kind: "onyx.Input", placeholder: "Enter Job Name", onchange:"inputChanged"}
       		]},
       		{kind:"onyx.Button", content: "Run", style: "margin-right:10px;", ontap: "runCommand" },
       		/**{kind:"onyx.Button", content: "Cancel"}**/
       	]}, {owner: this});
	},
	runCommand: function(){
		alert("Run");
	}
});

/*** Line that contains informations - Groubbox Files  ****/
enyo.kind({
	name: "commandFilesLine",
	classes: "commandFilesLine",
	style:"padding: 1px;", 
	components:[
		{tag:"div", components:[{name: "filename"}]},
		{tag:"div", style: "text-align:right", components:[
			{name:"msg", content: "Specify a file." , style: "margin: 2px 0px;display:block"},
			{name:"btnUp", kind:"onyx.Button", content: "Upload", ontap:"doFileUpload"},
			{name:"btnDown", kind:"onyx.Button", content: "Download", ontap:"doFileDownload"}
		]}
	],
	doFileUpload: function(inSender, inEvent){
		uploadFile();
	},
	create: function(){
		this.inherited(arguments);
		this.$.filename.setContent(this.data.description);

		if(this.type == "output"){
			this.$.btnUp.addClass("btnInactive");
			this.$.btnDown.addClass("btnActive");			
		}else{//input
			this.$.btnUp.addClass("btnActive");
			this.$.btnDown.addClass("btnInactive");
		}
	},
	doFileDownload: function(){
		alert("Download function!!");
	}
	
});

/*** Line that contains informations - Groubbox Exec  ****/
enyo.kind({
	name: "commandExecLine",
	classes: "commandFilesLine",
	style:"padding: 1px;", 
	components:[
		{classes: "onyx-sample-tools", style:"width:"+execCloudName+"%", components: [
				{kind:"onyx.Checkbox", name: "execCheck"}, {name: "execCloud",  style: "display:inline-block"}
		]},
		{ name: "execZone", style:"width:"+execCloudZone+"%" },
		{ name: "execSettings", style:"width:"+execCloudSett+"%" },
		{style:"width:"+execCloudEmail+"%", components:[ {kind:"onyx.Checkbox", name: "execSend", onChange:"toggleChanged"} ]}
	],
	create: function(){
		this.inherited(arguments);
		this.$.execCheck.setValue(false);
		this.$.execCloud.setContent(this.data.accountName);
		this.$.execZone.setContent(this.data.cloudName);
		this.$.execSettings.setContent(this.data.description);
		this.$.execSend.setValue(false);
	}
	
});

/*** The main classes that mount the command detail page  ****/
enyo.kind({ 
	name:"CommandDetail",
	kind: "FittableRows",
	fit: true,
	classes: "onyx onyx-sample",
	style: "padding: 0px",
	components:[
		{kind: "onyx.Toolbar", components: [ { name: "title" }, {fit: true}]},
		 {fit: true, kind: "Scroller", components:[
/**    {tag:"ul", name:"ul", id: "ul", components:[
	  			{tag:"li", name:"li1", ontap: "tabTap", index: "1", content: "Information"},
	  			{tag:"li", name:"li2", ontap: "tabTap", index: "2", content: "Command"}
	  		]},
	 		{fit: true, kind: "Scroller",classes: "panels", name:"panels", components:[
			 
			]}**/
	  		{kind: "FittableRows", classes: "commandTabs enyo-fit", components: [
	  		    {components: [
	  		                {tag:"ul", name:"ul", id: "ul", components:[
              		  			{tag:"li", name:"li1", ontap: "tabTap", index: "1", content: "Information"},
              		  			{tag:"li", name:"li2", ontap: "tabTap", index: "2", content: "Command"}
              		  		]},
	  		     ]},
          		{kind: "FittableColumns",fit:true, classes: "panels fittable-sample-box fittable-sample-mtb enyo-center",name:"panels", components: [
          			   {name: "div1", components:[
							{tag:"img", src:"./assets/info.png", style: "display: inline-block;margin: 2px;"},
							{name:"description",content: "Main Menu", style: "color: #63B8FF;display: inline-block; font-size: 16px;"}
          			    ]},
          			   {name: "div2", components:[
          			                              
          			    ]},
          		]}
          	]}//kind: "FittableRows"
		]},
/**		{kind: "enyo.Scroller", fit: true, components: [
				{name: "panel_three", classes: "panels-sample-sliding-content", allowHtml: true, components:[
					,
					{tag: "br"},
					
				]}
		]},**/
		{kind: "onyx.Toolbar", components: [ {kind: "onyx.Button", content: "Close", ontap: "closePanel"} ]},
	],
	create: function(){
		this.inherited(arguments);
		this.tabTap({index:2},{});
		this.$.title.setContent(this.data.name);
		this.$.description.setContent(this.data.description);

		if(typeof this.data.executionParameters != 'undefined'){
			this.$.div2.createComponent({kind:"commandParamGroup", "params": this.data.executionParameters});
			this.$.div2.reflow();
		}
		
		if(typeof this.data.inputFiles != 'undefined'){
			this.$.div2.createComponent({kind:"commandFilesGroup", "lines": this.data.inputFiles, "type" : "input"});
			this.$.div2.reflow();
		}
		
		if(typeof this.data.outputFiles != 'undefined'){
			this.$.div2.createComponent({kind:"commandFilesGroup", "lines": this.data.outputFiles, "type" : "output" });
			this.$.div2.reflow();
		}
		
		if( typeof this.data.cloudProfiles != 'undefined' ){// it is a object
			var cloudList = new Array();
			if( !(this.data.cloudProfiles instanceof Array) )//if it is not an array
				cloudList.push(this.data.cloudProfiles);
			else
				cloudList = this.data.cloudProfiles;//it is already an array
			
			this.$.div2.createComponent({kind:"commandExecGroup", "lines": cloudList });	
			
		}else{//clouds não definidas
			this.$.div2.createComponent({kind:"commandExecGroup", "lines": new Array() });	
		}
		
	},
	tabTap: function( sender, event ){
		
		var tabs = this.$.ul.components;
		for( var i in tabs ){
			var tabname = tabs[i].name;
			this.$[tabname].addRemoveClass("active", this.$[tabname].name == "li"+sender.index );
		}
		
		var divs = this.$.panels.components;
		for( var i in divs ){
			var divname = divs[i].name;
			this.$[divname].addRemoveClass("active", this.$[divname].name == "div"+sender.index );
		}
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