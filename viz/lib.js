var lib = (function(){
    var startMain = function() {
	data = [];
	for (var x = 0.0; x < 2 * Math.PI; x += 0.01)
	    data.push([x, Math.sin(x)]);
	$.plot($("#plot"), [data], {});
    };

    return {
	startMain: startMain
    };
})();
