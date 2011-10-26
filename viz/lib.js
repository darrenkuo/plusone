var lib = (function(){
    var exampleData = [{
        gitHash: "Hashy",
        expName: "demoA-4",
        foo: "4",
        predictionScore: [0.9, 0.5, 0.6, 0.1, 1.0, 0.0],
        barValue: [0.81, 0.25, 0.36, 0.01, 1.0, 0.0]
    }, {
        gitHash: "Hashy",
        expName: "demoA-10",
        foo: "10",
        predictionScore: [0.09, 0.05, 0.06, 0.01, 0.1, 0.0],
        barValue: [0.81, 0.25, 0.36, 0.01, 1.0, 0.0]
    }, {
        gitHash: "Hashy",
        expName: "demoB-5",
        baz: "5",
        predictionScore: [0.95, 0.75, 0.8, 0.55, 1.0, 0.5],
    }];

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
            for (var i = 0; i < ret.length; ++i) ret[i].push(0.01);  //TODO: get rid of this awful hack
        }
        return ret;
    }

    function startMain() {
        var fData = [];
        exampleData.forEach(function(value, index, array) {
            fData = fData.concat(flattenArrays(value));
        });
        xFilter = function (d) { return "demoA-4" == d.expName; };
        yFilter = function (d) { return "demoA-10" == d.expName; };
        var data = joinAxes([genAxisValues(fData, xFilter, "predictionScore", "index"),
                                       genAxisValues(fData, yFilter, "predictionScore", "index")]);
	$.plot($("#plot"), [data], {series: {bubbles: {active: true, show: true}}});
    };

    return {
        makeArray:makeArray,
	startMain: startMain
    };
})();
