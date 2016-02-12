var application = require("application");
var context = application.android.context;
var called = false;
var connection;
module.exports = {
	connector: function(){
                if(!connection){
		    connection = new edu.colorado.cs.nativescriptandroidrepl.SocketConnector();
                    connection.processRequest(38308);
		}
	}
};
