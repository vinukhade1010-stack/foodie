<% String pageTitle = "Create account"; String activeNav = "register"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<section class="wrap auth">
  <div class="auth-art" aria-hidden="true"><span>&#127829;</span><p>Create an account to save favorites and track orders.</p></div>
  <form class="panel auth-form" method="post" action="${ctx}/register" data-validate="register" novalidate>
    <h1>Create your account</h1>
    <c:if test="${not empty error}"><p class="alert" role="alert"><c:out value="${error}"/></p></c:if>

    <div class="field">
      <label for="name">Full name</label>
      <input id="name" name="name" type="text" autocomplete="name" required minlength="2" value="<c:out value='${name}'/>">
      <small class="field-msg" aria-live="polite"></small>
    </div>
    <div class="field">
      <label for="email">Email</label>
      <input id="email" name="email" type="email" autocomplete="email" required value="<c:out value='${email}'/>">
      <small class="field-msg" aria-live="polite"></small>
    </div>
    <div class="field">
      <label for="phone">Mobile number</label>
      <input id="phone" name="phone" type="tel" inputmode="numeric" maxlength="10" autocomplete="tel-national" required value="<c:out value='${phone}'/>">
      <small class="field-msg" aria-live="polite"></small>
    </div>
    <div class="field">
      <label for="password">Password</label>
      <div class="pw-wrap">
        <input id="password" name="password" type="password" autocomplete="new-password" required>
        <button type="button" class="link pw-toggle" data-toggle-pw="password" aria-label="Show password">Show</button>
      </div>
      <div class="strength" id="strength" aria-hidden="true"><span></span><span></span><span></span><span></span></div>
      <small class="field-msg" id="strength-text" aria-live="polite">Use 8 or more characters with letters and numbers.</small>
    </div>
    <div class="field">
      <label for="confirm">Confirm password</label>
      <input id="confirm" name="confirm" type="password" autocomplete="new-password" required>
      <small class="field-msg" aria-live="polite"></small>
    </div>
    <button class="btn btn-lg" type="submit">Create account</button>
    <p class="muted center">Already have an account? <a class="link" href="${ctx}/login">Log in</a></p>
  </form>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
