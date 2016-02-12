var androidrepl = require("nativescript-androidrepl");
var vmModule = require("./main-view-model");
function pageLoaded(args) {
    var page = args.object;
    androidrepl.connector(); 
    page.bindingContext = vmModule.mainViewModel;
}
exports.pageLoaded = pageLoaded;
