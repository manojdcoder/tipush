var TiPush = require("ti.push");
TiPush.addEventListener("callback", function(e) {
	console.log("callback fired");
	//test parsing
	var data = JSON.parse(e.payload);
	label.text = JSON.stringify(data);
});

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

var window = Ti.UI.createWindow({
	backgroundColor : "#FFFFFF"
});
window.open();

var scrollView = Ti.UI.createScrollView({
	layout : "vertical"
});
window.add(scrollView);

var deviceIdBtn = Ti.UI.createButton({
	title : "Get device id",
	top : "20"
});
deviceIdBtn.addEventListener("click", getDeviceId);
scrollView.add(deviceIdBtn);

var registerBtn = Ti.UI.createButton({
	title : "Register",
	top : "20"
});
registerBtn.addEventListener("click", getToken);
scrollView.add(registerBtn);

var unregisterBtn = Ti.UI.createButton({
	title : "Un-Register",
	top : "20"
});
unregisterBtn.addEventListener("click", deleteToken);
scrollView.add(unregisterBtn);

var updateBtn = Ti.UI.createButton({
	title : "Update",
	top : "20"
});
updateBtn.addEventListener("click", update);
scrollView.add(updateBtn);

var newWinBtn = Ti.UI.createButton({
	title : "Open new window",
	top : "20"
});
newWinBtn.addEventListener("click", openNewWin);
scrollView.add(newWinBtn);

var label = Ti.UI.createLabel({
	top : 20,
	left : 12,
	right : 12,
	textAlign : "center",
	color : "#000"
});
scrollView.add(label);

function getDeviceId() {
	alert(TiPush.deviceId);
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

function openNewWin() {
	Ti.UI.createWindow({
		backgroundColor : "#FFFFFF"
	}).open();
}
