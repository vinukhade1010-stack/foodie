<% String pageTitle = "Banners"; String activeNav = "admin-ads"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<%@ include file="/WEB-INF/views/includes/admin-nav.jspf" %>

<section class="wrap">
  <h1 class="page-title">Home page banners</h1>
  <p class="muted lead">Active banners whose dates include today appear in the home page carousel. Expired banners disappear on their own.</p>

  <div class="two-col wide-left">
    <div class="panel table-wrap">
      <table class="table">
        <thead><tr><th>Banner</th><th>Shown</th><th>Status</th><th><span class="sr-only">Actions</span></th></tr></thead>
        <tbody>
          <c:forEach var="a" items="${ads}">
            <c:set var="expired" value="${a.endDate < today}" />
            <c:set var="upcoming" value="${a.startDate > today}" />
            <tr>
              <td><c:out value="${a.emoji}"/> <strong><c:out value="${a.title}"/></strong><br>
                <small class="muted"><c:out value="${a.subtitle}"/><c:if test="${not empty a.couponCode}"> | code <c:out value="${a.couponCode}"/></c:if></small></td>
              <td><small>${a.startDate} to ${a.endDate}</small></td>
              <td>
                <c:choose>
                  <c:when test="${expired}"><span class="pill status-CANCELLED">Expired</span></c:when>
                  <c:when test="${upcoming}"><span class="pill status-PREPARING">Scheduled</span></c:when>
                  <c:when test="${a.active}"><span class="pill status-DELIVERED">Live</span></c:when>
                  <c:otherwise><span class="pill status-CANCELLED">Off</span></c:otherwise>
                </c:choose>
              </td>
              <td class="actions">
                <a class="btn btn-sm btn-ghost" href="${ctx}/admin/ads?edit=${a.id}#form">Edit</a>
                <form method="post" action="${ctx}/admin/ads" class="inline-form">
                  <input type="hidden" name="action" value="toggle"><input type="hidden" name="id" value="${a.id}">
                  <button class="btn btn-sm btn-ghost" type="submit">${a.active ? 'Switch off' : 'Switch on'}</button>
                </form>
                <form method="post" action="${ctx}/admin/ads" class="inline-form" data-confirm="Delete this banner?">
                  <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${a.id}">
                  <button class="btn btn-sm btn-danger" type="submit">Delete</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty ads}"><tr><td colspan="4" class="muted">No banners yet. Add your first one on the right.</td></tr></c:if>
        </tbody>
      </table>
    </div>

    <form class="panel" id="form" method="post" action="${ctx}/admin/ads">
      <h2>${empty editAd ? 'Add a banner' : 'Edit banner'}</h2>
      <input type="hidden" name="action" value="save">
      <input type="hidden" name="id" value="${empty editAd ? 0 : editAd.id}">

      <div class="field"><label for="title">Headline</label>
        <input id="title" name="title" required maxlength="100" value="<c:out value='${editAd.title}'/>"></div>
      <div class="field"><label for="subtitle">Supporting line</label>
        <input id="subtitle" name="subtitle" maxlength="200" value="<c:out value='${editAd.subtitle}'/>"></div>

      <div class="field-row">
        <div class="field"><label for="theme">Colour</label>
          <select id="theme" name="theme" class="select">
            <option value="g1" ${editAd.theme == 'g1' ? 'selected' : ''}>Chilli red</option>
            <option value="g2" ${editAd.theme == 'g2' ? 'selected' : ''}>Basil green</option>
            <option value="g3" ${editAd.theme == 'g3' ? 'selected' : ''}>Saffron gold</option>
            <option value="g4" ${editAd.theme == 'g4' ? 'selected' : ''}>Plum</option>
          </select></div>
        <div class="field"><label for="emoji">Emoji</label>
          <input id="emoji" name="emoji" maxlength="8" placeholder="&#127829;" value="<c:out value='${editAd.emoji}'/>"></div>
      </div>

      <div class="field-row">
        <div class="field"><label for="couponCode">Coupon to show</label>
          <select id="couponCode" name="couponCode" class="select">
            <option value="">None</option>
            <c:forEach var="cp" items="${coupons}">
              <option value="${fn:escapeXml(cp.code)}" ${editAd.couponCode == cp.code ? 'selected' : ''}><c:out value="${cp.code}"/> (${cp.discountPercent}%)</option>
            </c:forEach>
          </select></div>
        <div class="field"><label for="categoryId">"Order now" opens</label>
          <select id="categoryId" name="categoryId" class="select">
            <option value="">Whole menu</option>
            <c:forEach var="cat" items="${categories}">
              <option value="${cat.id}" ${editAd.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
            </c:forEach>
          </select></div>
      </div>

      <div class="field-row">
        <div class="field"><label for="startDate">Starts</label>
          <input id="startDate" name="startDate" type="date" required value="${empty editAd ? today : editAd.startDate}"></div>
        <div class="field"><label for="endDate">Ends</label>
          <input id="endDate" name="endDate" type="date" required value="${empty editAd ? today : editAd.endDate}"></div>
      </div>

      <div class="field"><label for="image">Image link (optional)</label>
        <input id="image" name="image" type="url" maxlength="500" placeholder="https://..." value="<c:out value='${editAd.image}'/>"></div>

      <label class="check"><input type="checkbox" name="active" ${empty editAd || editAd.active ? 'checked' : ''}> Switched on</label>

      <div class="row-actions">
        <button class="btn" type="submit">${empty editAd ? 'Add banner' : 'Save changes'}</button>
        <c:if test="${not empty editAd}"><a class="btn btn-ghost" href="${ctx}/admin/ads">Cancel</a></c:if>
      </div>
    </form>
  </div>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
