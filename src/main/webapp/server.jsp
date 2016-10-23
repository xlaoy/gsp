<%@page import="com.gewara.Config"%>
<div>Server:<%=Config.SYSTEMID%>,<%=Config.getHostname()%></div>
<div>GEWACONFIG: <%=System.getProperty("GEWACONFIG")%></div>
<%
	String taskFile = application.getRealPath("/WEB-INF/appContext-service-task.xml");
	boolean existsTask = false;
	try{existsTask = new java.io.File(taskFile).exists();}catch(Exception e){}
%>
<div>TASK: <%=existsTask%></div>
<div>
sversion:
</div>
