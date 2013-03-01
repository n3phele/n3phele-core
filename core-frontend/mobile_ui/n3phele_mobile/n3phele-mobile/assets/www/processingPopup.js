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