//var serverAddress = "https://n3phele-dev.appspot.com/resources/";//http://127.0.0.1:8888/
var serverAddress = "http://127.0.0.1:8888/resources/";

enyo.kind({ 
	name:"loginPage",
	kind: "FittableRows",
	fit: true,
	classes: "onyx onyx-sample",
	style: "padding: 0px",
	components:[
			{name: "topToolbar",kind: "onyx.Toolbar", components: [ {content: "N3PHELE"}, {fit: true}]},
				{classes: "panels-sample-sliding-content", allowHtml: true, style: "background: url('assets/bg_login.png') repeat; height: 100%; width:100%; display: table;", components:[
						{name: "login-container", style: "vertical-align: middle; display:table-cell; text-align:center;", components:[
								{name: "loginMsg"},
								{kind: "onyx.InputDecorator", style: "background-color: white;margin:2px;", components: [ {kind: "onyx.Input", name: "loginUser", placeholder: "Username"} ]},		
								{tag: "br"},
								{kind: "onyx.InputDecorator", style: "background-color: white;margin:2px;", components: [ {kind: "onyx.Input", name: "loginPw", type:"password", placeholder: "Password"} ]},
								{tag: "br"},
								{kind:"onyx.Button", content: "Login", style: "margin:2px;", classes: "onyx-blue", ontap:"validateUser"},
						]}//end div vertical cell
				]},//end panel table
	],//end components
	validateUser: function( sender, event){
		//obtain form data ********************************************
		var  user = sender.parent.owner.$.loginUser.getValue();
		var  pass = sender.parent.owner.$.loginPw.getValue();
		
		//validate form ********************************************
		if( user.length == 0 || pass.length == 0 ){
			sender.parent.owner.$.loginMsg.setContent("Please, fill the form!");
			return;
		}
		// Processing Popup *****************************************
		var popup = new spinnerPopup();
		popup.show();
	
		//request user validation ********************************************
		var hdr = user.replace("@", ".at-.") + ":" + pass;
		var encodeHdr = Base64.encode( hdr );
		var ajaxParams = {
			url: serverAddress+"user/login",
			headers:{ 'authorization' : "Basic "+ encodeHdr},
			method: "POST",
			contentType: "application/x-www-form-urlencoded",
			sync: false, 
		};
		
		sender.parent.owner.$.loginMsg.setClasses("loginMsgEnable");
		var ajaxComponent = new enyo.Ajax(ajaxParams); //connection parameters
		ajaxComponent
		.go()
		.response( this, function(inSender, inResponse){
			sender.parent.owner.$.loginMsg.setContent("User authenticated");
			

			var mainPage = new com.N3phele({ 'uid' : encodeHdr });
			
			popup.delete();
			mainPage.renderInto(document.body);
		}).error( this, function(inSender, inResponse){
			sender.parent.owner.$.loginMsg.setContent("Access denied!");
			popup.delete();
		});
		
		//*******************************************************************
	}
});//end kind
					
