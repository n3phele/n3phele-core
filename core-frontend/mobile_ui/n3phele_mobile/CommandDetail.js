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
var onSuccessFileUp = function(imageData){ console.log(imageData); }
/****************/

enyo.kind({
	name: "filesList",
	components:[
		{tag: "br"},
		{name: "groupbox", classes: "commandTable", kind: "onyx.Groupbox", components: [
			{name: "header", kind: "onyx.GroupboxHeader", classes: "groupboxBlueHeader", content: "Input Files"},//header
			{classes: "subheader", components:[ //subheader
				{content: "Filename", classes: "subsubheader" } , 
				{content: "Status", classes: "subsubheader"} 
			]}
		]}//end groupbox
	],//end components inFilesList
	create: function(){
		this.inherited(arguments);
		this.$.header.setContent( this.title );
		this.initializeLines( this.lines );
	},
	initializeLines: function( linesInfo ){
		for( var i in linesInfo ){
			console.log("oioioi", linesInfo[i]);
			var description = "" + linesInfo[i].description;
			 
			this.$.groupbox.createComponent({ classes: "commandFilesLine", style:"padding: 1px;", components:[
					{tag:"div", components:[ {name: linesInfo[i].name, content: description } ]},
					{tag:"div", style: "text-align:right", components:[
						{name:"msg", content: "Specify a file." , style: "margin: 2px 0px;display:block"},
						{kind:"onyx.Button", content: "File", ontap:"uploadFile", style: "margin: 2px 0px;display:inline-block"}
					]}
			]});//end this.createComponent
		}
	},
});

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
			this.$.panel_three.createComponent({kind:"filesList", "lines": this.data.inputFiles, "title" : "Input Files"});
			this.$.panel_three.reflow();
		}// end if(typeof this.data.inputFiles != 'undefined')
		
		if(typeof this.data.outputFiles != 'undefined'){
			var info = new Array();
				info.push(this.data.outputFiles);
			this.$.panel_three.createComponent({kind:"filesList", "lines": info, "title" : "Output Files" });
			this.$.panel_three.reflow();
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