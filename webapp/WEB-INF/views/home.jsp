<% String pageTitle = "Order food online"; String activeNav = "home"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>

<h1 class="sr-only">Order your favourite food with Foodie</h1>

<%-- ===== Offers carousel (ads from the database) ===== --%>
<section class="wrap hero">
  <c:choose>
    <c:when test="${not empty ads}">
      <div class="carousel" data-carousel role="region" aria-roledescription="carousel" aria-label="Offers">
        <div class="slides">
          <c:forEach var="ad" items="${ads}" varStatus="st">
            <article class="slide theme-${ad.theme}${st.first ? ' is-active' : ''}">
              <div class="slide-copy">
                <h2><c:out value="${ad.title}"/></h2>
                <p><c:out value="${ad.subtitle}"/></p>
                <div class="slide-actions">
                  <c:if test="${not empty ad.couponCode}">
                    <button type="button" class="btn btn-light" data-copy="${fn:escapeXml(ad.couponCode)}">Copy code <strong><c:out value="${ad.couponCode}"/></strong></button>
                  </c:if>
                  <a class="btn btn-outline-light" href="${ctx}/menu<c:if test='${not empty ad.categoryId}'>?cat=${ad.categoryId}</c:if>">Order now</a>
                </div>
              </div>
              <div class="slide-art" aria-hidden="true">
                <c:choose>
                  <c:when test="${not empty ad.image}"><img src="<c:out value='${ad.image}'/>" alt=""></c:when>
                  <c:otherwise><span><c:out value="${ad.emoji}"/></span></c:otherwise>
                </c:choose>
              </div>
            </article>
          </c:forEach>
        </div>
        <button class="car-btn prev" type="button" aria-label="Previous offer">&#8249;</button>
        <button class="car-btn next" type="button" aria-label="Next offer">&#8250;</button>
        <div class="dots"></div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="slide theme-g1 is-active static-hero">
        <div class="slide-copy">
          <h2>Hot food, at your door</h2>
          <p>Pick a dish, add it to your cart and we will take care of the rest.</p>
          <div class="slide-actions"><a class="btn btn-light" href="${ctx}/menu">Browse the menu</a></div>
        </div>
        <div class="slide-art" aria-hidden="true"><span>&#127829;</span></div>
      </div>
    </c:otherwise>
  </c:choose>
</section>

<%-- ===== Categories ===== --%>
<section class="wrap section">
  <h2 class="section-title">What are you craving?</h2>
  <div class="chips big">
    <c:forEach var="cat" items="${categories}">
      <a class="chip" href="${ctx}/menu?cat=${cat.id}"><span aria-hidden="true"><c:out value="${cat.emoji}"/></span> <c:out value="${cat.name}"/></a>
    </c:forEach>
  </div>
</section>

<%-- ===== Coupons ===== --%>
<section class="wrap section" id="offers">
  <h2 class="section-title">Coupons you can use today</h2>
  <div class="offer-row">
    <c:forEach var="cp" items="${coupons}">
      <div class="offer-card">
        <div class="offer-pct">${cp.discountPercent}% off</div>
        <p class="muted"><c:out value="${cp.description}"/></p>
        <div class="offer-code">
          <code><c:out value="${cp.code}"/></code>
          <button type="button" class="btn btn-sm btn-ghost" data-copy="${fn:escapeXml(cp.code)}">Copy code</button>
        </div>
        <small class="muted">Minimum order &#8377;<fmt:formatNumber value="${cp.minOrder}" maxFractionDigits="0"/></small>
      </div>
    </c:forEach>
    <c:if test="${empty coupons}"><p class="muted">No coupons right now. Check back soon.</p></c:if>
  </div>
</section>

<%-- ===== Today's special (loaded by JS from /api/menu) ===== --%>
<section class="wrap section">
  <div class="section-head">
    <h2 class="section-title">Today's special</h2>
    <a class="link" href="${ctx}/menu">See full menu</a>
  </div>
  <div class="grid" data-food-grid data-query="featured=true&limit=8"></div>
</section>

<%-- ===== Combo deals ===== --%>
<c:forEach var="cat" items="${categories}">
  <c:if test="${cat.name == 'Combos'}">
    <section class="wrap section">
      <div class="section-head">
        <h2 class="section-title">Combo deals</h2>
        <a class="link" href="${ctx}/menu?cat=${cat.id}">All combos</a>
      </div>
      <div class="grid" data-food-grid data-query="cat=${cat.id}&limit=8"></div>
    </section>
  </c:if>
</c:forEach>

<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
