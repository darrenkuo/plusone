var GraphProperties = (function(){
    main = function() {
        var toPlot = $(".plot_me");
        toPlot.css("width", "512px");
        toPlot.css("height", "256px");
        toPlot.each(function (index, div) {
            valueStrs = $(div).text().split(" ").filter(function (x) { return 0 != x.length; });
            values = valueStrs.map(function (x, index) { return [index, parseFloat(x)]; });
            $.plot($(div), [values]);
        });
    }

    return {
	main: main
    };
})();

$(document).ready(GraphProperties.main);
