package plusone.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class MetadataLogger {

    public class TestMetadata {
	public String name;
	protected Map<String, Object> metadata = new HashMap<String, Object>();

	public TestMetadata(String name) {	  
	    this.name = name;
	    this.metadata.put("gitHash", gitHash);
	}

	public void createSingleValueEntry(String key, Object value) {
	    metadata.put(key, value);
	}

	public void createListValueEntry(String key, Object[] vals) {
	    metadata.put(key, vals);
	}

	public Map<Object, Object> toJson() {
	    Map<Object, Object> m = new HashMap<Object, Object>();
	    for (Map.Entry<String, Object> entry : metadata.entrySet()) 
		m.put(entry.getKey(), entry.getValue());
	    return m;
	}
    }

    protected Map<String, TestMetadata> experiments = 
	new HashMap<String, TestMetadata>();

    protected String gitHash = getGitHash();

    public TestMetadata getTestMetadata(String experiment) {
	if (experiments.containsKey(experiment)) {
	    return experiments.get(experiment);
	}

	TestMetadata newMeta = new TestMetadata(experiment);
	experiments.put(experiment, newMeta);
	return newMeta;
    }

    protected Map<Object, Object> toJsonFormat() {
	Map<Object, Object> jsonMap = new HashMap<Object, Object>();
	for (Map.Entry<String, TestMetadata> entry :
		 experiments.entrySet()) {
	    jsonMap.put(entry.getKey(), entry.getValue().toJson());
	}
	return jsonMap;
    }

    public String getJson() {
	return new Gson().toJson(toJsonFormat());
    }

    private String getGitHash() {
	String hash = "";
	try {
            Process p = Runtime.getRuntime().exec("git rev-parse HEAD");
            BufferedReader stdInput = 
		new BufferedReader(new InputStreamReader(p.getInputStream()));
	    
	    String s;
            while ((s = stdInput.readLine()) != null) {
		hash += s;
            }
	}
        catch (IOException e) {
            e.printStackTrace();
	    hash = "no hash";
        }
	return hash.trim();
    }
}