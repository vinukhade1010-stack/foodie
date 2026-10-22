<% String pageTitle = "Checkout"; String activeNav = "checkout"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>
<section class="wrap">
  <h1 class="page-title">Checkout</h1>

  <div class="cart-layout">
    <form method="post" action="${ctx}/checkout" class="panel" id="checkout-form" novalidate>
      <h2>Delivery details</h2>
      <c:if test="${not empty error}"><p class="alert" role="alert"><c:out value="${error}"/></p></c:if>

      <p class="muted">Delivering to <strong><c:out value="${sessionScope.user.name}"/></strong>, <c:out value="${sessionScope.user.phone}"/></p>

      <div class="field">
        <label for="address">Delivery address</label>
        <textarea id="address" name="address" rows="3" maxlength="250" required placeholder="Flat / house no, building, street, area, city, PIN"><c:out value="${empty address ? sessionScope.user.address : address}"/></textarea>
        <small class="field-msg" aria-live="polite"></small>
      </div>

      <fieldset class="field pay">
        <legend>Payment</legend>
        <label class="radio"><input type="radio" name="payment" value="COD" checked> <span><strong>Cash on delivery</strong><br><small class="muted">Pay when the food arrives</small></span></label>
        <label class="radio"><input type="radio" name="payment" value="UPI_DEMO"> <span><strong>UPI (demo)</strong><br><small class="muted">No real payment is taken in this project</small></span></label>
      </fieldset>

      <button class="btn btn-lg" type="submit" id="place-order">Place order</button>
    </form>

    <div id="summary-root" data-mode="checkout"><div class="skeleton" style="height:300px"></div></div>
  </div>
</section>
<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
