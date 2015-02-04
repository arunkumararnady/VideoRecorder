var exec = require('cordova/exec');
function VideoRecorder() { 
	console.log("VideoRecorder.js: is created");
}

VideoRecorder.prototype.recordVideo = function(aString,successCallback,errorCallback){ 
	console.log("CoolPlugin.js: showToast"); 
	exec(successCallback, 
			errorCallback,"VideoRecorder",aString,[]);
} 

var videoRecorder = new VideoRecorder(); 
module.exports = videoRecorder;
