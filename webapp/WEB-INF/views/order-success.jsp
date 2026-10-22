<% String pageTitle = "Order placed"; String activeNav = "orders"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<section class="wrap narrow">
  <div class="panel success">
    <div class="success-mark" aria-hidden="true">&#10003;</div>
    <h1>Order #${order.id} is placed</h1>
    <p class="muted">We have sent it to the kitchen. You can follow its progress below or from My orders.</p>

    <ol class="tracker" data-status="${order.status}" data-order-id="${order.id}" aria-label="Order progress">
      <li data-step="PLACED">Placed</li>
      <li data-step="PREPARING">Preparing</li>
      <li data-step="DELIVERED">Delivered</li>
    </ol>

    <table class="table compact">
      <tbody>
        <c:forEach var="i" items="${order.items}">
          <tr><td><c:out value="${i.itemName}"/> &times; ${i.quantity}</td><td class="num">&#8377;<fmt:formatNumber value="${i.lineTotal}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
        </c:forEach>
        <tr><td class="muted">Subtotal</td><td class="num">&#8377;<fmt:formatNumber value="${order.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
        <c:if test="${order.discount > 0}"><tr><td class="muted">Discount<c:if test="${not empty order.couponCode}"> (<c:out value="${order.couponCode}"/>)</c:if></td><td class="num">&minus;&#8377;<fmt:formatNumber value="${order.discount}" minFractionDigits="2" maxFractionDigits="2"/></td></tr></c:if>
        <tr><td class="muted">GST (5%)</td><td class="num">&#8377;<fmt:formatNumber value="${order.gst}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
        <tr class="total"><td>Total</td><td class="num">&#8377;<fmt:formatNumber value="${order.total}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
      </tbody>
    </table>
    <p class="muted">Delivering to: <c:out value="${order.address}"/><br>Payment: <c:out value="${order.paymentMethod}"/></p>

    <div class="row-actions">
      <a class="btn" href="${ctx}/receipt?id=${order.id}">Download receipt</a>
      <a class="btn btn-ghost" href="${ctx}/orders">My orders</a>
      <a class="btn btn-ghost" href="${ctx}/menu">Order more</a>
    </div>
  </div>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
