var TiPush = require("ti.push");
var isAvailable = TiPush.isGooglePlayServicesAvailable();
switch (isAvailable) {
case TiPush.SERVICE_DISABLED:
	Ti.API.info("SERVICE_DISABLED");
	break;

case TiPush.SERVICE_INVALID:
	Ti.API.info("SERVICE_INVALID");
	break;

case TiPush.SERVICE_MISSING:
	Ti.API.info("SERVICE_MISSING");
	break;

case TiPush.SERVICE_UPDATING:
	Ti.API.info("SERVICE_UPDATING");
	break;

case TiPush.SERVICE_VERSION_UPDATE_REQUIRED:
	Ti.API.info("SERVICE_VERSION_UPDATE_REQUIRED");
	break;

case TiPush.SUCCESS:
	Ti.API.info("SUCCESS");
	break;

default:
	Ti.API.info("Something went wrong, no matches : " + isAvailable);
}

function update() {
	TiPush.updateGooglePlayServices();
}

function getToken() {
	TiPush.retrieveDeviceToken({
		senderId : "15063256342",
		success : function(evt) {
			console.log(evt);
		},
		error : function(evt) {
			console.log(evt);
		}
	});
}

function deleteToken() {
	TiPush.destroyDeviceToken({
		senderId : "15063256342",
		success : function(evt) {
			console.log(evt);
		},
		error : function(evt) {
			console.log(evt);
		}
	});
}

var window = Ti.UI.createWindow({
	backgroundColor : "#FFFFFF",
	layout : "vertical"
});

var registerBtn = Ti.UI.createButton({
	title : "Register",
	top : "120",
	id : "__alloyId0"
});
window.add(registerBtn);

var unregisterBtn = Ti.UI.createButton({
	title : "Un-Register",
	top : "20",
	id : "__alloyId1"
});
window.add(unregisterBtn);

var updateBtn = Ti.UI.createButton({
	title : "Update",
	top : "20",
	id : "__alloyId2"
});
window.add(updateBtn);

registerBtn.addEventListener("click", getToken);
unregisterBtn.addEventListener("click", deleteToken);
updateBtn.addEventListener("click", update);

window.open();
