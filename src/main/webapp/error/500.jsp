<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="true" import="org.slf4j.*,com.gewara.util.*,com.gewara.Config"%>
<%
	Logger logger = LoggerFactory.getLogger("HTTP500ERROR");
	if(exception!=null) {
		logger.warn("HTTP500ERROR:" + WebUtils.getParamStr(request, true) + ", \n", exception);
		exception.printStackTrace();
	}else{
		logger.warn("HTTP500ERROR:NO-EXCEPTION," + WebUtils.getParamStr(request, true) + ", \n");
	}
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<body>
<div class="ui_layout">
	<div class="errorContent">
		<h2>你浏览的网页暂时不能显示222</h2>
		<p>对不起，你浏览的网页可能<em>已被删除或不存在！</em></p>
		<span>可以尝试以下方法：</span>
		<ul>
			<li>检查网址是否正确</li>
			<li>返回<a class="ml5" href="/">格瓦拉首页</a></li>
			<li>返回<a class="ml5" href="javascript:history.go(-1)">上一页</a></li>
		</ul>
	</div>
</div>
<script type="text/javascript">
</script>
</body>
</html>