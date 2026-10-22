<% String pageTitle = "Menu"; String activeNav = "menu"; %>
<%@ include file="/WEB-INF/views/includes/header.jspf" %>

<section class="wrap menu-layout" id="menu-root" data-max="${maxPrice}">
  <aside class="filters" aria-label="Filters">
    <div class="filters-head">
      <h2>Filters</h2>
      <button type="button" class="link" id="clear-filters">Clear all</button>
    </div>

    <div class="filter-group">
      <label for="f-search">Search</label>
      <input id="f-search" type="search" placeholder="Pizza, paneer, mango..." autocomplete="off">
    </div>

    <div class="filter-group">
      <h3>Category</h3>
      <div class="chips" id="f-cats">
        <button type="button" class="chip active" data-cat="">All</button>
        <c:forEach var="cat" items="${categories}">
          <button type="button" class="chip" data-cat="${cat.id}"><span aria-hidden="true"><c:out value="${cat.emoji}"/></span> <c:out value="${cat.name}"/></button>
        </c:forEach>
      </div>
    </div>

    <div class="filter-group">
      <h3>Type</h3>
      <div class="seg" id="f-type" role="group" aria-label="Veg or non-veg">
        <button type="button" class="active" data-type="">All</button>
        <button type="button" data-type="veg"><span class="vdot veg"></span> Veg</button>
        <button type="button" data-type="nonveg"><span class="vdot nonveg"></span> Non-veg</button>
      </div>
    </div>

    <div class="filter-group">
      <label for="f-price">Price up to <output id="f-price-label"></output></label>
      <input type="range" id="f-price" min="50" max="${maxPrice}" step="10" value="${maxPrice}">
    </div>

    <label class="check"><input type="checkbox" id="f-fav"> Show my favorites only</label>
  </aside>

  <div class="menu-main">
    <div class="menu-head">
      <h1>Menu</h1>
      <span class="muted" id="menu-count" aria-live="polite"></span>
    </div>
    <div class="grid" id="menu-grid" data-food-grid="menu"></div>
  </div>
</section>

<%@ include file="/WEB-INF/views/includes/footer.jspf" %>
