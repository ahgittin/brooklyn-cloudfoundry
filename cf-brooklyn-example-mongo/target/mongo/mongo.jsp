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
	//DBObject result = (DBObject)request.getAttribute("result");
	//out.print("<pre>" + pretty(result.toString()) + "</pre>");
	String vcap = System.getenv("VCAP_SERVICES");
	if (vcap != null) vcap = pretty(vcap);
	out.print("<pre>" + vcap + "</pre>");
	%>

</body>
</html>
