<%@ page isErrorPage="true" %>
<% String pageTitle = "Something went wrong"; String activeNav = "error"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<section class="wrap narrow empty-state">
  <div class="empty-emoji" aria-hidden="true">&#127858;</div>
  <h1>That page is not on the menu</h1>
  <p class="muted">The page was not found, or something broke on our side. If you just set the project up, check the database settings in <code>db.properties</code>.</p>
  <a class="btn" href="${ctx}/home">Back to home</a>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
