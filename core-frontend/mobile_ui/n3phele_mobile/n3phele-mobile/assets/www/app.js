var createCommandItems = function(arrayOfCommands, arrayOfImages) {
	list = [];
	for (var i in arrayOfCommands)
	{
		var widget = { name: arrayOfCommands[i], displayName: arrayOfCommands[i], image: arrayOfImages[i] };
		list.push(widget);
	}
	return list;
}

/*Main painels*/
enyo.kind({
	name: "com.N3phele",
	kind: "FittableRows",
	classes: "onyx enyo-fit",
	
	menu:["Files","Commands","Acvity History","Accounts"],	
	menuImages:["./assets/files.png","./assets/commands.png","./assets/activityHistory.png","./assets/accounts.png"],
	
	commands: null,
	commandsImages : null,
		
	components: [
		{name:"N3pheleCommands", style: "display:none"},
		{kind: "Panels", panelCreated : false, fit: true, touch: true, classes: "panels-sample-sliding-panels", arrangerKind: "CollapsingArranger", wrap: false, components: [
			{name: "left", components: [
				{kind: "Scroller", classes: "enyo-fit", touch: true, components: [
					
					{kind: "onyx.Toolbar", components: [ {content: "N3phele"}, {fit: true} ]}, //Panel Title
					
					{name: "mainMenuPanel", style:"width:90%;margin:auto", components:[//div to align content
					    {kind:"Image", src:"assets/cloud-theme.gif", fit: true, style:  "padding-left:30px; padding-top: 30px;"},
						{classes: "onyx-sample-divider", content: "Main Menu", style: "color: #375d8c"},					
						{kind: "List", fit: true, touch:true, count:4, style: "height:"+(4*65)+"px", onSetupItem: "setupItemMenu", components: [
							{name: "menu_item",	classes: "panels-sample-flickr-item", ontap: "mainMenuTap", style: "box-shadow: -4px 0px 4px rgba(0,0,0,0.3);", components: [
								{name:"menu_image", kind:"Image"},
								{name: "menu_option",kind:"Image"}]},
						]}
					]}// end mainMenuPanel
				]}//end scroller
			]},
			{name: "imageIconPanel", kind:"FittableRows", fit:true, components:[
				{name: "imageIcon",kind: "enyo.Scroller"}
			]}			
		]}
	],	
	setupButton: function(inSender, inEvent) {
		this.$.item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.t.setContent({kind: "onyx.Button", ontap:"itemTapMenu", components: [
					{kind: "onyx.Icon", src: "https://github.com/enyojs/enyo/wiki/assets/fish_bowl.png"}
					]});
	},
	destroyPanel: function(inSender, inEvent) {
		this.setIndex(2);				
		this.getActive().destroy();					
		this.panelCreated = false;
		
		if (enyo.Panels.isScreenNarrow()) {
			this.setIndex(1);
		}
		else {
			this.setIndex(0);
		}		
		
		this.reflow();
		this.owner.$.IconGallery.deselectLastItem();
	},			
	closePanel: function(){
		this.$.panels.setIndex(0);
		this.destroyPanel();
	},	
	setupItemMenu: function(inSender, inEvent) {// given some available data.
		this.$.menu_item.addRemoveClass("onyx-selected", inSender.isSelected(inEvent.index));
		this.$.menu_image.setSrc(this.menuImages[inEvent.index]);
		this.$.menu_option.setContent(this.menu[inEvent.index]);
	},
	mainMenuTap: function(inSender, inEvent) {
		//Checking if the device has a small screen and adjust if necessa
		if (enyo.Panels.isScreenNarrow()) {
			this.$.panels.setIndex(1);
		}		

		if(this.$.panels.panelCreated)this.$.panels.destroyPanel(); //??
		
		this.$.imageIconPanel.destroyClientControls(); // clear second painel
		
		//Checking the menu selected
		switch(inEvent.index){
			case 0://File menu
				this.closeSecondaryPanels(2);
				this.createComponent({
					kind: "onyx.Toolbar",
					container: this.$.imageIconPanel,
					components: [
						{content: "Files"}, {fit: true}
					]
				});
				this.$.imageIconPanel.render();	
			break;
			case 1://Command Menu
				this.closeSecondaryPanels(2);
				this.createCommandList();
			break;
			case 2://Activity History
				this.closeSecondaryPanels(2);
				this.$.imageIconPanel.createComponent({
					kind: "RecentActivityHistory", 'uid' : this.uid
				});
				this.$.imageIconPanel.render();	
			break;
			case 3://Accounts
				this.closeSecondaryPanels(2);
				this.$.imageIconPanel.createComponent({
					kind: "AccountList", 'uid' : this.uid
				});
				this.$.imageIconPanel.render();	
			break;
		}//end switch
	},	
	backMenu: function(){
		this.$.panels.setIndex(0);
	},
	createCommandList: function() {
	
		this.createComponent({name:"toolComm", kind: "onyx.Toolbar", container: this.$.imageIconPanel,components: [
							{content: "Commands"},
							{fit: true}]}
			);		
		
		this.createComponent({
			name: "IconGallery",
			kind: "IconList",
			container: this.$.imageIconPanel,
			onDeselectedItems: "commandDeselect",
			onSelectedItem: "commandTap", 
			commands: this.commands,
			commandsImages: this.commandsImages,
			retrieveContentData: function(){
				this.data = createCommandItems(this.commands, this.commandsImages); } 
			} 
		);		
		
		if (enyo.Panels.isScreenNarrow()) {
			this.createComponent({kind: "onyx.Toolbar",container: this.$.imageIconPanel, components: [
				{kind: "onyx.Button", content: "Close", ontap: "backMenu"}
			]});
		}
		else{
			this.createComponent({kind: "onyx.Toolbar",container: this.$.imageIconPanel});
		}
		
        this.$.imageIconPanel.render();
    },
    commandDeselect: function(inSender, inEvent) {
   // 	this.closeSecondaryPanels( 2 );
    
	/*8    if (enyo.Panels.isScreenNarrow()) {
			this.$.panels.setIndex(2);
		}
		else {
			this.$.panels.setIndex(1);
		}**/
	},
	/** When an command icon is actioned It will be runned**/
	commandTap: function(inSender, inEvent) {
		//check if command information is set
		if( !( inEvent.index in this.commandsData ) ){
			alert("There is not commands in the database!");
			return;
		}
		this.closeSecondaryPanels(2);//close old panels
		
		//create panel
		this.$.panels.createComponent({ kind: "CommandDetail", "uid": this.uid, 'icon': this.commandsData[inEvent.index].icon, 'uri': this.commandsData[inEvent.index].uri }).render();
		this.$.panels.reflow();
		this.$.panels.setIndex(2);
		
		inSender.scrollIntoView(inSender.$["commandItem"+inEvent.index], false);

	},
	/** It's called when the king is instanciated **/
	create: function() {
		this.inherited(arguments);
		var popup = new spinnerPopup();
		popup.show();
		if (!enyo.Panels.isScreenNarrow())
			this.$.mainMenuPanel.createComponent({ kind: "RecentActivityList", 'uid' : this.uid});
	
		//setting connection parameters
		var ajaxComponent = new enyo.Ajax({
			url: serverAddress+"command",
			headers:{ 'authorization' : "Basic "+ this.uid},
			method: "GET",
			contentType: "application/x-www-form-urlencoded",
			sync: false, 
		});
		
		//Requesting service reply
		ajaxComponent
		.go({'summary' : true, 'start' : 0, 'end' : 16, 'preferred' :true})
		.response( this, function( sender, response ){
			if(response.total == 0){alert("There is no recent activities!");return;}
			
			this.commandsData = response.elements;//get the response
			this.commandsData = fixArrayInformation(this.commandsData);
			this.commands = new Array();
			this.commandsImages = new Array();
			
			var waiting = 0;
			var errorIndex = new Array();// array containing the index of the icons that did exists 
			for( var i in this.commandsData ){//set comand list information
				this.commands.push( this.commandsData[i].name ); //set name
				
				var iconUrl = this.commandsData[i].icon; //get icon url
					iconUrl = this.fixCommandIconUrl( iconUrl );//fix icon url 
				
				this.commandsData[i].icon = iconUrl;
				this.commandsImages.push( iconUrl ); //set icon url fixed

				//checking if icon exists
				waiting++;
				var test = new enyo.Ajax({ url : iconUrl, handleAs: "text", index: i });
		
				test.go().response(this,function(sender, response){
					waiting--;
					if(waiting == 0) this.replaceWrongIcons(errorIndex);
					popup.delete();
				}).error(this,function(sender, response){
					waiting--;
					errorIndex.push( sender.index );//adding icon with error
					if(waiting == 0) this.replaceWrongIcons(errorIndex);	
					popup.delete();
				});
			}// end for( var i in response.elements )
		})
		.error( this, function(){ console.log("Error to load the list of commands!!"); popup.delete();});
	
	},
	/** Used to set the default command icon when the icon address doesn't exist**/
	replaceWrongIcons: function( wrongIcons ){
		for(var i in wrongIcons){//it will change the icons that doesn't have icon
			var imageIndex = wrongIcons[i];
			this.commandsImages[imageIndex] = "./assets/Script.png";
		}
	},
	/** Used to fix wrong information in the command icon url **/
	fixCommandIconUrl: function( iconUrl ){
		var url = iconUrl;
			url = url.substring( url.search("/")+1 );//removing https
			url = "http://"+ url;
		
		if( url.search("/icons/") < 0 ){ // correcting wrong url
			var aux = url.split("/");
			var filename = aux[ aux.length - 1 ];
			var filenameIndex =  url.search( filename );
			url = url.substring(0,filenameIndex-1) +"/icons/"+ filename;
		}
		return url;		
	},
	/** It will close painels that are not needed anymore **/
	closeSecondaryPanels: function( level ){
		var panels = this.$.panels.getPanels();

		if( panels.length > level ){// Is there panels opened? close it
			
			
			for(var i=level; i < panels.length ;){
				panels[i].destroy();
			}
			
			this.$.panels.reflow();
		}
		
	}
});