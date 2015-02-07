<%@ page import="com.mongodb.DBObject" %>
<%@ page import="com.google.gson.*" %>
<html>
<body>

	<h1>MongoDB Example</h1>
	
	<%!
	public String pretty(String json){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(json);
		return gson.toJson(je);
	}
	%>

	<%
	DBObject result = (DBObject)request.getAttribute("result");
	if(result != null) out.print("<pre>" + pretty(result.toString()) + "</pre>");
	out.print("<pre>" + System.getenv() + "</pre>");
	%>

</body>
</html>
