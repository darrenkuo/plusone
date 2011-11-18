var lib = (function(){
    var exampleData = {
	"demoA-4": {
            gitHash: "Hashy",
            expName: "demoA-4",
            foo: "4",
            predictionScore: [0.9, 0.9, 0.6, 0.1, 1.0, 0.0],
            barValue: [0.81, 0.25, 0.36, 0.01, 1.0, 0.0]
	},
	"demoA-10": {
            gitHash: "Hashy",
            expName: "demoA-10",
            foo: "10",
            predictionScore: [0.09, 0.09, 0.06, 0.01, 0.1, 0.0],
            barValue: [0.81, 0.25, 0.36, 0.01, 1.0, 0.0]
	},
	"demoB-5": {
            gitHash: "Hashy",
            expName: "demoB-5",
            baz: "5",
            predictionScore: [0.95, 0.75, 0.8, 0.55, 1.0, 0.5],
	}
    };

    function makeArray(length, gen) {
        var ret = [];
        for (var i = 0; i < length; ++ i)
            ret.push(gen(i));
        return ret;
    }

    function flattenArrays(d) {
        var length = undefined;
        for (k in d) {
            if (!d.hasOwnProperty(k)) continue;
            var v = d[k];
            if (v instanceof Array) {
                length = v.length;
                break;
            }
        }
        if (undefined == length) return [];
        return makeArray(length, function (index) {
            var ret = {index: index};
            for (k in d) {
                if (!d.hasOwnProperty(k)) continue;
                if ("index" == k) throw 'The reserved property name "index" was used somewhere!';
                var v = d[k];
                if (v instanceof Array) {
                    if (length != v.length) throw "Unequal array lengths!";
                    ret[k] = v[index];
                } else
                    ret[k] = v;
            }
            return ret;
        });
    }

    function genAxisValues(data, filter, axisKey, joinKey) {
        var ret = {};
        for (var i = 0; i < data.length; ++i) {
            var d = data[i];
            if (!filter(d)) continue;
            var j = d[joinKey];
            if (undefined != ret[j])
                /* Allowing multiple values might be okay; this is why ret[j]
                 * is an array.  For simplicity we don't allow multiple values
                 * right now. */
                throw "Multiple values for join index " + j + " on axis " + axisKey;
            ret[j] = [d[axisKey]];
        }
        return ret;
    }

    function joinAxes(axes) {
        if (0 == axes.length) throw "joinAxes: no axes";
        var ret = [];
        for (var k in axes[0]) {
            ret.push(makeArray(axes.length, function (index) {
                var a = axes[index][k];
                if (1 != a.length) throw "joinAxes: support for multiple values not yet added";
                return a[0];
            }));
        }
        return ret;
    }

    function globSimilar(data) {
	if (0 == data.length) return [];
	function fuzzyCompare(baseIndex) {
	    return function(a, b) {
		if (baseIndex == a.length) return 0;
		if (Math.abs(a[baseIndex] - b[baseIndex]) < 1e-9) return fuzzyCompare(baseIndex + 1)(a, b);
		return a[baseIndex] - b[baseIndex];
	    }
	}
	var sorting = data.slice(0);
	sorting.sort(fuzzyCompare(0));
	var curPoint = sorting[0];
	var curVal = 1;
	var ret = [];
	for (var i = 1; i <= sorting.length; ++ i) {
	    if (sorting.length != i && fuzzyCompare(0)(curPoint, sorting[i]) == 0)
		++ curVal;
	    else {
		var p = curPoint.slice(0);
		p.push(curVal);
		ret.push(p);
		curPoint = sorting[i];
		curVal = 1;
	    }
	}
	return ret;
    }

    function fixDiameters(data, maxD) {
	var maxVal = Math.max.apply(Math, data.map(function(value){return value[value.length-1];}));
	return data.map(function (value){
	    var v=value.slice(0);
	    v[v.length - 1] = Math.sqrt(v[v.length - 1] / maxVal) * maxD;
	    return v;
	});
    }

    function fillParamDiv(div) {
	function makeAxisParam(friendlyName, idBase, defaultValue) {
	    var ret = $("<div/>").text(friendlyName + ": ").append($("<input/>", {id: idBase + "_x", type: "text", value: defaultValue}));
	    var yInput = $("<input/>", {id: idBase + "_y", type: "text", value: defaultValue}).css("display", "none");
	    ret.append($("<input/>", {id: idBase+ "_c", type: "checkbox"}).change(function (event) {
	        if (event.target.checked)
		    yInput.css("display", "inline");
		else
		    yInput.css("display", "none");
            }));
	    ret.append(yInput);
	    return ret;
	}
	div.append($("<input/>", {id: "data_url", type: "text"}));
	div.append(makeAxisParam("Axis key", "axis_key", "predictionScore"));
	div.append(makeAxisParam("Experiment name (regex)", "expName_regex", "demoA-4"));
	div.append($("<input/>", {type: "button", value: "Plot"}).click(function(){updatePlot();}));
    }

    function startMain() {
	fillParamDiv($("#param_div"));
	$("#param_div_wait_p").remove();
	updatePlot();
    }

    function getAxisParam(axis, paramName) {
        var inputObj;
	if ($("#" + paramName + "_c").is(":checked"))
	    inputObj = $("#" + paramName + "_" + axis);
        else
	    inputObj = $("#" + paramName + "_x");
	return inputObj.attr("value");
    }

    function makeAxisFilter(axis) {
        expNameRegex = new RegExp("^" + getAxisParam(axis, "expName_regex") + "$");
	return function (d) {
	    return expNameRegex.test(d.expName);
	}
    }

    function updatePlot() {
	var data_url = $("#data_url").attr("value");

	v = undefined;  // Global variable that the script at data_path puts the data into.
	var script = $("<script/>", {type: "text/javascript", src: encodeURI(data_url)});
	$("#script_holder").append(script);
	var rawData = v;
	if (undefined == rawData)
	    rawData = exampleData;

        var fData = [];
	for (e in rawData) {
	    if (!rawData.hasOwnProperty(e)) continue;
            fData = fData.concat(flattenArrays(rawData[e]));
        };
	var xData = genAxisValues(fData, makeAxisFilter("x"), getAxisParam("x", "axis_key"), "index");
	var yData = genAxisValues(fData, makeAxisFilter("y"), getAxisParam("y", "axis_key"), "index");
        var dataWithRepeats = joinAxes([xData, yData]);
        var globbedData = globSimilar(dataWithRepeats);
	plot(globbedData);
    }

    function plot(data) {
	if (0 == data.length) {
	    $.plot($("plot"), []);
	}
	if (3 != data[0].length)
	    throw "lib.plot: Sorry, we assume two data dimensions for now.";
	var axisMin = [], axisMax = [];
	var axisMinRelSpace = [];
	for (var i = 0; i < 2; ++ i) {
	    var coords = data.map(function(value){return value[i];});
	    coords.sort();
	    var aMin = coords[0];
	    var aMax = coords[coords.length - 1]
	    if (aMax - aMin < 1e-9) {
		aMin -= 0.5;
		aMax += 0.5;
	    }
	    axisMin.push(aMin);
	    axisMax.push(aMax);
	    var ms = Infinity;
	    for (var j = 0; j + 1 < coords.length; ++j) {
		var diff = coords[j + 1] - coords[j];
		if (diff < 1e-9) continue;
		ms = Math.min(ms, diff);
	    }
	    axisMinRelSpace.push(ms / (aMax - aMin));
	}
	var minRelSpace = Math.min.apply(Math, axisMinRelSpace);
	var maxD = minRelSpace * (axisMax[1] - axisMin[1]);
	var dataWithDiameters = fixDiameters(data, maxD);

	/* Add some space so the bubbles don't leave the plot area. */
	var finalAxisMin = [];
	var finalAxisMax = [];
	for (var i = 0; i < 2; ++ i) {
	    finalAxisMin[i] = (1 + minRelSpace) * axisMin[i] - minRelSpace * axisMax[i];
	    finalAxisMax[i] = (1 + minRelSpace) * axisMax[i] - minRelSpace * axisMin[i];
	}

	$.plot($("#plot"), [dataWithDiameters], {
	    series: {bubbles: {active: true, show: true}},
	    crosshair: {mode: "xy"},
	    xaxis: {min: finalAxisMin[0], max: finalAxisMax[0]},
	    yaxis: {min: finalAxisMin[1], max: finalAxisMax[1]}
	});
    };

    return {
        makeArray:makeArray,
	startMain: startMain
    };
})();
