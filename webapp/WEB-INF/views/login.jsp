<% String pageTitle = "Log in"; String activeNav = "login"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<section class="wrap auth">
  <div class="auth-art" aria-hidden="true"><span>&#127836;</span><p>Hungry? Your cart is waiting.</p></div>
  <form class="panel auth-form" method="post" action="${ctx}/login" data-validate="login" novalidate>
    <h1>Log in</h1>
    <c:if test="${not empty error}"><p class="alert" role="alert"><c:out value="${error}"/></p></c:if>
    <input type="hidden" name="next" value="<c:out value='${next}'/>">

    <div class="field">
      <label for="email">Email</label>
      <input id="email" name="email" type="email" autocomplete="email" required value="<c:out value='${email}'/>">
      <small class="field-msg" aria-live="polite"></small>
    </div>
    <div class="field">
      <label for="password">Password</label>
      <div class="pw-wrap">
        <input id="password" name="password" type="password" autocomplete="current-password" required>
        <button type="button" class="link pw-toggle" data-toggle-pw="password" aria-label="Show password">Show</button>
      </div>
      <small class="field-msg" aria-live="polite"></small>
    </div>
    <button class="btn btn-lg" type="submit">Log in</button>
    <p class="muted center">New to Foodie? <a class="link" href="${ctx}/register">Create an account</a></p>
  </form>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
