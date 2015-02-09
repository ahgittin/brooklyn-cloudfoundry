package io.brooklyn.cf.example.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

public class MongoServlet extends HttpServlet {

	private static final long serialVersionUID = 8697126501348471234L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String vcap = System.getenv("VCAP_SERVICES");
		DBObject dbObject = vcap == null ? null : createMongoDB(req, vcap);

		req.setAttribute("result", dbObject);
		req.getRequestDispatcher("mongo.jsp").forward(req, resp);
	}

	private DBObject createMongoDB(HttpServletRequest req, String vcap)
			throws UnknownHostException {

		Entry<String, JsonElement> entry = getCredentialsEntry(new JsonParser().parse(vcap));
		if (entry == null) return null;
		
		String hostname = getHostName(entry);
		int port = getPort(entry);
		
		MongoClient mongoClient = new MongoClient(hostname, port);
		DB database = mongoClient.getDB("myDatabase");
		DBCollection collection = database.getCollection("users");
		BasicDBObject document = new BasicDBObject("name", "Fred").append("age", "28"); 
		collection.insert(document); 
		return collection.findOne();
	}

	private Entry<String, JsonElement> getCredentialsEntry(JsonElement parse) {
		Iterator<Entry<String, JsonElement>> entrySet = getAllCredentials(parse).iterator();
		// we only need the first entry
		if(entrySet.hasNext()){
			return entrySet.next();
		}
		return null;
	}

	private Set<Entry<String, JsonElement>> getAllCredentials(JsonElement parse) {
		return getCredentialsElement(parse).getAsJsonObject().entrySet();
	}

	private JsonElement getCredentialsElement(JsonElement parse) {
		return getField(getSingletonElement(parse), "credentials");
	}

	private JsonElement getSingletonElement(JsonElement parse) {
		return getMongoDb(parse).getAsJsonArray().get(0);
	}

	private JsonElement getMongoDb(JsonElement parse) {
		return getField(parse, "MongoDB");
	}
	
	private JsonElement getJsonElementFromValue(Entry<String, JsonElement> entry, String field) {
		return getField(entry.getValue(), field);
	}

	private String getHostName(Entry<String, JsonElement> entry) {
		return getJsonElementFromValue(entry, "host.name").getAsString();
	}
	
	private int getPort(Entry<String, JsonElement> entry) {
		return getJsonElementFromValue(entry, "mongodb.server.port").getAsInt();
	}

	private JsonElement getField(JsonElement parse, String field){
		return parse.getAsJsonObject().get(field);
	}
	
	
	
}
