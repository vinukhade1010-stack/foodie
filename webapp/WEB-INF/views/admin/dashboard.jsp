<% String pageTitle = "Admin dashboard"; String activeNav = "admin-dashboard"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<%@ include file="/WEB-INF/views/includes/admin-nav.jspf" %>

<section class="wrap">
  <div class="section-head">
    <h1 class="page-title">Dashboard</h1>
    <a class="btn btn-sm" href="${ctx}/admin/report?date=${today}">Download today's sales report</a>
  </div>

  <div class="stats">
    <div class="stat"><span class="muted">Total sales</span><strong>&#8377;<fmt:formatNumber value="${totalSales}" maxFractionDigits="0"/></strong></div>
    <div class="stat"><span class="muted">All orders</span><strong>${orderCount}</strong></div>
    <div class="stat"><span class="muted">Sales today</span><strong>&#8377;<fmt:formatNumber value="${todaySales}" maxFractionDigits="0"/></strong></div>
    <div class="stat"><span class="muted">Orders today</span><strong>${todayCount}</strong></div>
    <div class="stat"><span class="muted">Being prepared or waiting</span><strong>${activeCount}</strong></div>
  </div>

  <div class="two-col">
    <div class="panel">
      <h2>Today's orders</h2>
      <c:choose>
        <c:when test="${empty todayOrders}"><p class="muted">No orders yet today.</p></c:when>
        <c:otherwise>
          <div class="table-wrap">
            <table class="table">
              <thead><tr><th>Order</th><th>Customer</th><th>Status</th><th class="num">Total</th></tr></thead>
              <tbody>
                <c:forEach var="o" items="${todayOrders}">
                  <tr>
                    <td>#${o.id}</td>
                    <td><c:out value="${o.customerName}"/></td>
                    <td><span class="pill status-${o.status}"><c:out value="${o.statusLabel}"/></span></td>
                    <td class="num">&#8377;<fmt:formatNumber value="${o.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="panel">
      <h2>Best sellers</h2>
      <c:choose>
        <c:when test="${empty topItems}"><p class="muted">Sales will show here after the first orders.</p></c:when>
        <c:otherwise>
          <ul class="bars">
            <c:forEach var="t" items="${topItems}">
              <li>
                <span><c:out value="${t[0]}"/></span>
                <span class="bar"><i style="width:${t[1] * 100 / topBest}%"></i></span>
                <b>${t[1]}</b>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
