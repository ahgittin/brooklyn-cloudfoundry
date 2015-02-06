package io.brooklyn.cf.example.mongo;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoServlet extends HttpServlet {

	private static final long serialVersionUID = 8697126501348471234L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		// TODO: get details from Cloud Foundry instance
		MongoClient mongoClient = new MongoClient("localhost", 27019);
		DB database = mongoClient.getDB("myDatabase");
		DBCollection collection = database.getCollection("users");
		BasicDBObject document = new BasicDBObject("name", "Fred").append("age", "28"); 
		collection.insert(document); 
		req.setAttribute("result", collection.findOne());
		req.getRequestDispatcher("mongo.jsp").forward(req, resp);
	}
}
