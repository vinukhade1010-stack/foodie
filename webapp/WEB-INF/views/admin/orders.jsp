<% String pageTitle = "Orders"; String activeNav = "admin-orders"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<%@ include file="/WEB-INF/views/includes/admin-nav.jspf" %>

<section class="wrap">
  <div class="section-head">
    <h1 class="page-title">Orders</h1>
    <div class="seg" role="group" aria-label="Filter by status">
      <a class="${empty filter ? 'active' : ''}" href="${ctx}/admin/orders">All</a>
      <a class="${filter == 'PLACED' ? 'active' : ''}" href="${ctx}/admin/orders?status=PLACED">Placed</a>
      <a class="${filter == 'PREPARING' ? 'active' : ''}" href="${ctx}/admin/orders?status=PREPARING">Preparing</a>
      <a class="${filter == 'DELIVERED' ? 'active' : ''}" href="${ctx}/admin/orders?status=DELIVERED">Delivered</a>
      <a class="${filter == 'CANCELLED' ? 'active' : ''}" href="${ctx}/admin/orders?status=CANCELLED">Cancelled</a>
    </div>
  </div>

  <c:if test="${empty orders}"><div class="panel empty-state"><h2>No orders here</h2><p class="muted">Nothing matches this filter yet.</p></div></c:if>

  <c:if test="${not empty orders}">
  <div class="panel table-wrap">
    <table class="table">
      <thead><tr><th>Order</th><th>Customer</th><th>Items</th><th class="num">Total</th><th>Placed</th><th>Status</th></tr></thead>
      <tbody>
        <c:forEach var="o" items="${orders}">
          <tr>
            <td>#${o.id}</td>
            <td><c:out value="${o.customerName}"/><br><small class="muted"><c:out value="${o.address}"/></small></td>
            <td><c:forEach var="i" items="${o.items}" varStatus="st"><c:out value="${i.itemName}"/> &times; ${i.quantity}<c:if test="${not st.last}">, </c:if></c:forEach></td>
            <td class="num">&#8377;<fmt:formatNumber value="${o.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
            <td><small><c:out value="${o.createdAt}"/></small></td>
            <td>
              <form method="post" action="${ctx}/admin/orders" data-autosubmit>
                <input type="hidden" name="id" value="${o.id}">
                <input type="hidden" name="filter" value="<c:out value='${filter}'/>">
                <label class="sr-only" for="st-${o.id}">Status of order ${o.id}</label>
                <select id="st-${o.id}" name="status" class="select status-${o.status}">
                  <c:forEach var="s" items="PLACED,PREPARING,DELIVERED,CANCELLED">
                    <option value="${s}" ${o.status == s ? 'selected' : ''}>${s.substring(0,1)}${s.substring(1).toLowerCase()}</option>
                  </c:forEach>
                </select>
              </form>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
  </c:if>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
