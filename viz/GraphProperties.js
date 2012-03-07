var GraphProperties = (function(){
    var logt = function (x) { return Math.log(x); };
    var expt = function (x) { return Math.exp(x); };
    var logTicks = function(axis) {
        var t;
        var res = [];
        var logMin = Math.log(axis.min);
        var logMax = Math.log(axis.max);
        for (t = 0.0; t <= 1.0; t += 0.25) {
            res.push(Math.exp(logMin * (1.0 - t) + logMax * t));
        }
        return res;
    }

    var main = function() {
        var toPlot = $(".plot_me");
        toPlot.css("width", "512px");
        toPlot.css("height", "256px");
        toPlot.each(function (index, div) {
            var pointStrs = $(div).text().split(" ");
            var points = [];
            var mins = [Infinity, Infinity];
            var maxs = [-Infinity, -Infinity];
            var logX = false;
            var logY = false;
            var i;
            for (i = 0; i < pointStrs.length; ++i) {
                s = pointStrs[i];
                if ("s:logX" == s)
                    logX = true;
                else if ("s:logY" == s)
                    logY = true;
                else {
                    p = s.split(",").map(function (x, i) {
                        mins[i] = Math.min(mins[i], x);
                        maxs[i] = Math.max(maxs[i], x);
                        return parseFloat(x);
                    });
                    points.push(p);
                }
            }
            $.plot($(div), [points], {
                xaxis: logX ? {transform: logt, inverseTransform: expt, min: mins[0], max: maxs[0], ticks: logTicks} : {},
                yaxis: logY ? {transform: logt, inverseTransform: expt, min: mins[1], max: maxs[1], ticks: logTicks} : {}
            });
        });
    }

    return {
	main: main
    };
})();

$(document).ready(GraphProperties.main);
