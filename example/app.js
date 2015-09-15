var window = Ti.UI.createWindow({
	backgroundColor : "#FFFFFF",
	layout : "vertical"
});
window.open();

var registerBtn = Ti.UI.createButton({
	title : "Register",
	top : "120"
});
window.add(registerBtn);

var unregisterBtn = Ti.UI.createButton({
	title : "Un-Register",
	top : "20"
});
window.add(unregisterBtn);

var updateBtn = Ti.UI.createButton({
	title : "Update",
	top : "20"
});
window.add(updateBtn);

var newWinBtn = Ti.UI.createButton({
	title : "Open new window",
	top : "20"
});
window.add(newWinBtn);

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

function didCallback(e) {

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
	TiPush.clearStatus({
		senderId : "15063256342",
		success : function(evt) {
			console.log(evt);
		},
		error : function(evt) {
			console.log(evt);
		}
	});
}

/**
 * for testing purpose
 * open root activity
 * without clearing top
 */
function openNewWin() {
	Ti.UI.createWindow({
		backgroundColor : "#FFFFFF"
	}).open();
}

registerBtn.addEventListener("click", getToken);
unregisterBtn.addEventListener("click", deleteToken);
updateBtn.addEventListener("click", update);
newWinBtn.addEventListener("click", openNewWin);

TiPush.addEventListener("callback", didCallback);
