<% String pageTitle = "Menu items"; String activeNav = "admin-foods"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<%@ include file="/WEB-INF/views/includes/admin-nav.jspf" %>

<section class="wrap">
  <h1 class="page-title">Menu items</h1>

  <div class="two-col wide-left">
    <div class="panel table-wrap">
      <table class="table">
        <thead><tr><th>Dish</th><th>Category</th><th class="num">Price</th><th>Status</th><th><span class="sr-only">Actions</span></th></tr></thead>
        <tbody>
          <c:forEach var="f" items="${foods}">
            <tr>
              <td><span class="vdot ${f.veg ? 'veg' : 'nonveg'}"></span> <c:out value="${f.emoji}"/> <c:out value="${f.name}"/><c:if test="${f.featured}"> <span class="tag inline">Featured</span></c:if></td>
              <td><c:out value="${f.categoryName}"/></td>
              <td class="num">&#8377;<fmt:formatNumber value="${f.price}" maxFractionDigits="2"/></td>
              <td><span class="pill ${f.available ? 'status-DELIVERED' : 'status-CANCELLED'}">${f.available ? 'Available' : 'Sold out'}</span></td>
              <td class="actions">
                <a class="btn btn-sm btn-ghost" href="${ctx}/admin/foods?edit=${f.id}#form">Edit</a>
                <form method="post" action="${ctx}/admin/foods" class="inline-form">
                  <input type="hidden" name="action" value="toggle"><input type="hidden" name="id" value="${f.id}">
                  <button class="btn btn-sm btn-ghost" type="submit">${f.available ? 'Mark sold out' : 'Mark available'}</button>
                </form>
                <form method="post" action="${ctx}/admin/foods" class="inline-form" data-confirm="Delete ${fn:escapeXml(f.name)}? This cannot be undone.">
                  <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${f.id}">
                  <button class="btn btn-sm btn-danger" type="submit">Delete</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>

    <form class="panel" id="form" method="post" action="${ctx}/admin/foods">
      <h2>${empty editItem ? 'Add a dish' : 'Edit dish'}</h2>
      <input type="hidden" name="action" value="save">
      <input type="hidden" name="id" value="${empty editItem ? 0 : editItem.id}">

      <div class="field"><label for="name">Name</label>
        <input id="name" name="name" required maxlength="100" value="<c:out value='${editItem.name}'/>"></div>

      <div class="field"><label for="categoryId">Category</label>
        <select id="categoryId" name="categoryId" class="select" required>
          <c:forEach var="cat" items="${categories}">
            <option value="${cat.id}" ${editItem.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
          </c:forEach>
        </select></div>

      <div class="field"><label for="description">Description</label>
        <textarea id="description" name="description" rows="2" maxlength="255"><c:out value="${editItem.description}"/></textarea></div>

      <div class="field-row">
        <div class="field"><label for="price">Price (&#8377;)</label>
          <input id="price" name="price" type="number" step="0.01" min="1" required value="${editItem.price}"></div>
        <div class="field"><label for="emoji">Emoji</label>
          <input id="emoji" name="emoji" maxlength="8" placeholder="&#127829;" value="<c:out value='${editItem.emoji}'/>"></div>
      </div>

      <div class="field"><label for="image">Image link (optional)</label>
        <input id="image" name="image" type="url" maxlength="500" placeholder="https://..." value="<c:out value='${editItem.image}'/>">
        <small class="muted">Paste a direct link to a picture. Without one, the emoji is shown.</small></div>

      <div class="checks">
        <label class="check"><input type="checkbox" name="veg" ${empty editItem || editItem.veg ? 'checked' : ''}> Vegetarian</label>
        <label class="check"><input type="checkbox" name="featured" ${editItem.featured ? 'checked' : ''}> Featured today</label>
        <label class="check"><input type="checkbox" name="available" ${empty editItem || editItem.available ? 'checked' : ''}> Available</label>
      </div>

      <div class="row-actions">
        <button class="btn" type="submit">${empty editItem ? 'Add dish' : 'Save changes'}</button>
        <c:if test="${not empty editItem}"><a class="btn btn-ghost" href="${ctx}/admin/foods">Cancel</a></c:if>
      </div>
    </form>
  </div>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
