var lib = (function(){
    var startMain = function() {
	data = [];
	for (var x = 0.0; x < 2 * Math.PI; x += 0.1)
	    data.push([x, Math.sin(x), 0.1 + 0.03 * Math.cos(x)]);
	$.plot($("#plot"), [data], {series: {bubbles: {active: true, show: true}}});
    };

    return {
	startMain: startMain
    };
})();
