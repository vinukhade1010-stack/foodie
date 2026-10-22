<% String pageTitle = "Profile"; String activeNav = "profile"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<section class="wrap narrow">
  <h1 class="page-title">Your profile</h1>
  <form class="panel" method="post" action="${ctx}/profile" data-validate="profile" novalidate>
    <c:if test="${not empty error}"><p class="alert" role="alert"><c:out value="${error}"/></p></c:if>
    <div class="field">
      <label for="email">Email</label>
      <input id="email" type="email" value="<c:out value='${sessionScope.user.email}'/>" disabled>
      <small class="muted">Your email is your login and cannot be changed here.</small>
    </div>
    <div class="field">
      <label for="name">Full name</label>
      <input id="name" name="name" type="text" required minlength="2" value="<c:out value='${sessionScope.user.name}'/>">
      <small class="field-msg" aria-live="polite"></small>
    </div>
    <div class="field">
      <label for="phone">Mobile number</label>
      <input id="phone" name="phone" type="tel" inputmode="numeric" maxlength="10" required value="<c:out value='${sessionScope.user.phone}'/>">
      <small class="field-msg" aria-live="polite"></small>
    </div>
    <div class="field">
      <label for="address">Delivery address</label>
      <textarea id="address" name="address" rows="3" maxlength="250"><c:out value="${sessionScope.user.address}"/></textarea>
    </div>
    <button class="btn" type="submit">Save changes</button>
  </form>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
