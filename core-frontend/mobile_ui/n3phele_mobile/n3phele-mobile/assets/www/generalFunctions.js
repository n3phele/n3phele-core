enyo.kind({
		name: "spinnerPopup",
		classes: "onyx-sample-popup",
		kind: "onyx.Popup",
		centered: true, 
		floating: true,
		onHide: "popupHidden",
		scrim: true,
		autoDismiss: false,
		scrimWhenModal: false,
		components: [
			{kind: "onyx.Spinner"},
			{content: "Loading", style: "font-size:12px"},
		],
		delete: function (){
			this.hide();
			this.destroy();
		}
});

fixArrayInformation = function( arr ){
	if( arr == 'undefined' )
		return new Array();
		
	//correcting the type of information
	if( !( arr instanceof Array ) ){
		
		var aux = arr;
		arr = new Array();
		arr.push(aux);
	}
	
	return arr;
};