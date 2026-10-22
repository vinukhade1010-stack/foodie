<% String pageTitle = "My orders"; String activeNav = "orders"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<section class="wrap narrow">
  <h1 class="page-title">My orders</h1>

  <c:if test="${empty orders}">
    <div class="empty-state">
      <div class="empty-emoji" aria-hidden="true">&#129380;</div>
      <h2>No orders yet</h2>
      <p class="muted">When you place an order, you can track it here.</p>
      <a class="btn" href="${ctx}/menu">Browse the menu</a>
    </div>
  </c:if>

  <c:forEach var="o" items="${orders}">
    <article class="panel order-card" data-order-id="${o.id}">
      <header class="order-head">
        <div>
          <h2>Order #${o.id}</h2>
          <span class="muted"><c:out value="${o.createdAt}"/></span>
        </div>
        <span class="pill status-${o.status}" data-status-pill><c:out value="${o.statusLabel}"/></span>
      </header>

      <ol class="tracker" data-status="${o.status}" aria-label="Order progress">
        <li data-step="PLACED">Placed</li>
        <li data-step="PREPARING">Preparing</li>
        <li data-step="DELIVERED">Delivered</li>
      </ol>

      <p class="order-items">
        <c:forEach var="i" items="${o.items}" varStatus="st"><c:out value="${i.itemName}"/> &times; ${i.quantity}<c:if test="${not st.last}">, </c:if></c:forEach>
      </p>
      <footer class="order-foot">
        <strong>&#8377;<fmt:formatNumber value="${o.total}" minFractionDigits="2" maxFractionDigits="2"/></strong>
        <a class="btn btn-sm btn-ghost" href="${ctx}/receipt?id=${o.id}">Download receipt</a>
      </footer>
    </article>
  </c:forEach>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
