(function () {
    var setTimeout;
    setTimeout = function (fn,delay) {
	var executor = new java.util.concurrent.Executors.newScheduledThreadPool(1);
        var runnable = new JavaAdapter(java.lang.Runnable, {run: fn});
        executor.schedule(runnable, delay, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    return setTimeout;
})()
