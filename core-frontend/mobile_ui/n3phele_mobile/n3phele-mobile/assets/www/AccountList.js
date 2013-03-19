/*** The main classes that mount the account list page  ****/
enyo.kind({ 
	name:"AccountList",
	kind: "FittableRows",
	fit: true,
	style: "padding: 0px",
	components:[
		{kind: "onyx.Toolbar", components: [ { name: "title", content:"Accounts" }, {fit: true}]},

		{kind: "FittableRows", name:"panel", fit: true, components: [
			{style: "width: 80%;margin:auto;color: #63B8FF;font-size: 16px;", content: "Details about the accounts registered"},
			{name: "groupbox", classes: "table", kind: "onyx.Groupbox", style: "width: 80%;margin:auto", components: [
				{name: "header", kind: "onyx.GroupboxHeader", classes: "groupboxBlueHeader", content: "List of accounts"},//header
				{classes: "subheader", style:"background-color: rgb(200,200,200);font-weight: bold;font-size:13px;", components:[ //subheader
					{content: "Name", style:"width: 25%; display: inline-block;"} , 
					{content: "Last 24 hours",  style:"width:25%; display: inline-block;" } , 
					{content: "Active", style:"width:25%; display: inline-block;" } ,
					{content: "Cloud",  style:"width:25%; display: inline-block;" } ,
				]}
			]},
		]},
		
		{kind: "onyx.Toolbar", components: [ {kind: "onyx.Button", content: "Close", ontap: "backMenu"} ]}
	],
	create: function(){
		this.inherited(arguments)
		var popup = new spinnerPopup();
		popup.show();
		
		var ajaxComponent = new enyo.Ajax({
			url: serverAddress+"account",
			headers:{ 'authorization' : "Basic "+ this.uid},
			method: "GET",
			contentType: "application/x-www-form-urlencoded",
			sync: false, 
		}); 
				
		ajaxComponent.go()
		.response(this, function(sender, response){
	
			response.elements = fixArrayInformation(response.elements);
			for( var i=0; i<response.total ; i++ ){
				this.$.groupbox.createComponent({style: "background-color:white", components:[
  					{content: response.elements[i].name, classes: "subsubheader", style:"width: 25%; display: inline-block;"} , 
					{content: "0",  style:"width:25%; display: inline-block;" } , 
					{content: "0",  style:"width:25%; display: inline-block;" } ,
					{content: response.elements[i].cloudName, classes: "subsubheader",  style:"width:25%; display: inline-block;" } ,
				],owner:this});
				
			}
			this.$.groupbox.render();
			this.reflow();
			popup.delete();
		})
		.error(this, function(){
			console.log("Error to load the detail of the command!");
			popup.delete();
		});		
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
	},
	backMenu: function( sender , event){
		sender.parent.parent.parent.parent.setIndex(0);
	}
	
})