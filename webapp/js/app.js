/* =========================================================================
   Foodie front-end. Plain JavaScript, no framework.
   The server sends JSON (Gson); this file turns it into what you see.
   Sections: helpers | toast | theme | cart | food cards | quick view |
             search suggestions | menu filters | cart page | forms |
             carousel | orders | admin | start-up
   ========================================================================= */
(function () {
  'use strict';

  /* ------------------------------ helpers ------------------------------ */
  var body = document.body;
  var CTX = body.dataset.ctx || '';
  var LOGGED_IN = body.dataset.loggedIn === 'true';
  var RUPEE = '\u20B9';

  function $(sel, root) { return (root || document).querySelector(sel); }
  function $$(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }
  function esc(s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  }
  function money(n, fixed) {
    return RUPEE + Number(n).toLocaleString('en-IN', { minimumFractionDigits: fixed ? 2 : 0, maximumFractionDigits: 2 });
  }
  function debounce(fn, ms) {
    var t;
    return function () { var a = arguments, self = this; clearTimeout(t); t = setTimeout(function () { fn.apply(self, a); }, ms); };
  }
  function getJSON(url) {
    return fetch(url, { headers: { 'Accept': 'application/json' } }).then(function (r) { return r.json(); });
  }
  function postForm(url, params) {
    return fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
      body: new URLSearchParams(params)
    }).then(function (r) {
      return r.json().catch(function () { return { ok: false, error: 'Something went wrong. Please try again.' }; });
    }).catch(function () {
      return { ok: false, error: 'No connection. Check your internet and try again.' };
    });
  }

  /* ------------------------------ toast ------------------------------ */
  function toast(message, opts) {
    opts = opts || {};
    var box = $('#toasts');
    if (!box) return;
    while (box.children.length >= 3) box.firstChild.remove();
    var el = document.createElement('div');
    el.className = 'toast' + (opts.error ? ' error' : '');
    el.innerHTML = '<span>' + esc(message) + '</span>' +
      (opts.link ? '<a href="' + CTX + esc(opts.link.href) + '">' + esc(opts.link.text) + '</a>' : '');
    box.appendChild(el);
    setTimeout(function () {
      el.classList.add('out');
      setTimeout(function () { el.remove(); }, 300);
    }, opts.ms || 3200);
  }

  /* ------------------------------ theme ------------------------------ */
  function initTheme() {
    var btn = $('#theme-toggle');
    if (!btn) return;
    btn.addEventListener('click', function () {
      var next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      try { localStorage.setItem('foodie-theme', next); } catch (e) { /* private mode: ignore */ }
    });
  }

  /* ------------------------------ cart (shared) ------------------------------ */
  var badge = $('#cart-badge');
  function setBadge(n) {
    if (!badge) return;
    var changed = badge.textContent !== String(n);
    badge.textContent = n;
    if (changed) {            // restart the little bounce animation
      badge.classList.remove('bump');
      void badge.offsetWidth;
      badge.classList.add('bump');
    }
  }
  function cartAction(params) {
    return postForm(CTX + '/api/cart', params).then(function (d) {
      if (d.bill) setBadge(d.bill.count);
      return d;
    });
  }

  /* ------------------------------ food cards ------------------------------ */
  var ITEMS = {};   // id -> dish, remembered so quick view needs no extra request

  function tintOf(id) { return 'tint-' + (Number(id) % 6); }

  function artHTML(f) {
    return f.image
      ? '<img src="' + esc(f.image) + '" alt="" loading="lazy">'
      : '<span class="art-emoji" aria-hidden="true">' + esc(f.emoji) + '</span>';
  }

  function foodCard(f) {
    ITEMS[f.id] = f;
    return '<article class="card food">' +
      '<button type="button" class="art ' + tintOf(f.categoryId) + '" data-quick="' + f.id + '" aria-label="Quick view: ' + esc(f.name) + '">' + artHTML(f) + '</button>' +
      '<button type="button" class="fav' + (f.favorite ? ' on' : '') + '" data-fav="' + f.id + '" aria-pressed="' + !!f.favorite + '" aria-label="Favorite ' + esc(f.name) + '">&#9829;</button>' +
      (f.featured ? '<span class="tag">Featured</span>' : '') +
      '<div class="food-body">' +
        '<div class="food-top"><span class="vdot ' + (f.veg ? '' : 'nonveg') + '" title="' + (f.veg ? 'Veg' : 'Non-veg') + '"></span><h3>' + esc(f.name) + '</h3></div>' +
        '<p class="muted clamp">' + esc(f.description) + '</p>' +
        '<div class="food-foot"><strong class="price">' + money(f.price) + '</strong>' +
          (f.available ? '<button type="button" class="btn btn-sm" data-add="' + f.id + '">Add</button>' : '<span class="muted">Sold out</span>') +
        '</div>' +
      '</div></article>';
  }

  function skeletons(grid, n) {
    grid.innerHTML = new Array(n + 1).join('<div class="card skel" aria-hidden="true"></div>');
  }

  function loadGrid(grid, query) {
    skeletons(grid, 4);
    return getJSON(CTX + '/api/menu?' + query).then(function (d) {
      grid.innerHTML = d.items.map(foodCard).join('') ||
        '<p class="muted">Nothing here yet.</p>';
    }).catch(function () {
      grid.innerHTML = '<p class="muted">Could not load dishes. <button type="button" class="link" data-retry>Try again</button></p>';
      grid.querySelector('[data-retry]').addEventListener('click', function () { loadGrid(grid, query); });
    });
  }

  /* ------------------------------ quick view ------------------------------ */
  var lastFocus = null;
  function closeModal() {
    var root = $('#modal-root');
    if (!root || !root.firstChild) return;
    root.innerHTML = '';
    if (lastFocus && lastFocus.focus) lastFocus.focus();
  }
  function openQuick(id) {
    var f = ITEMS[id];
    if (!f) return;
    lastFocus = document.activeElement;
    var root = $('#modal-root');
    root.innerHTML =
      '<div class="modal-back" data-modal-back>' +
        '<div class="modal" role="dialog" aria-modal="true" aria-labelledby="qv-title">' +
          '<button type="button" class="modal-close" data-close-modal aria-label="Close">&#10005;</button>' +
          '<div class="art-big ' + tintOf(f.categoryId) + '">' + artHTML(f) + '</div>' +
          '<div class="modal-body">' +
            '<h2 id="qv-title"><span class="vdot ' + (f.veg ? '' : 'nonveg') + '"></span>' + esc(f.name) + '</h2>' +
            '<span class="muted">' + esc(f.categoryName) + '</span>' +
            '<p>' + esc(f.description) + '</p>' +
            '<div class="modal-foot"><strong class="price">' + money(f.price) + '</strong>' +
              '<span>' +
                '<button type="button" class="btn btn-ghost btn-sm' + '" data-fav="' + f.id + '" aria-pressed="' + !!f.favorite + '">' + (f.favorite ? '&#9829; Saved' : '&#9825; Save') + '</button> ' +
                (f.available ? '<button type="button" class="btn" data-add="' + f.id + '">Add to cart</button>' : '<span class="muted">Sold out</span>') +
              '</span>' +
            '</div>' +
          '</div>' +
        '</div>' +
      '</div>';
    var close = $('[data-close-modal]', root);
    if (close) close.focus();
  }
  document.addEventListener('keydown', function (e) {
    var root = $('#modal-root');
    if (!root || !root.firstChild) return;
    if (e.key === 'Escape') { closeModal(); return; }
    if (e.key === 'Tab') {   // keep keyboard focus inside the dialog
      var f = $$('button, a[href]', root);
      if (!f.length) return;
      var first = f[0], last = f[f.length - 1];
      if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last.focus(); }
      else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first.focus(); }
    }
  });

  /* ------------------------------ clicks (one listener for the whole page) ------------------------------ */
  function copyText(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) return navigator.clipboard.writeText(text);
    return new Promise(function (resolve) {
      var ta = document.createElement('textarea');
      ta.value = text; document.body.appendChild(ta); ta.select();
      try { document.execCommand('copy'); } catch (e) { /* ignore */ }
      ta.remove(); resolve();
    });
  }

  document.addEventListener('click', function (e) {
    var t = e.target;
    var el;

    if ((el = t.closest('[data-add]'))) {
      el.disabled = true;
      var label = el.textContent;
      cartAction({ action: 'add', id: el.dataset.add }).then(function (d) {
        el.disabled = false;
        if (d.ok) {
          toast(d.message, { link: { href: '/cart', text: 'View cart' } });
          el.textContent = 'Added \u2713';
          setTimeout(function () { el.textContent = label; }, 900);
        } else {
          toast(d.error, { error: true });
        }
      });
      return;
    }

    if ((el = t.closest('[data-fav]'))) {
      var id = el.dataset.fav;
      postForm(CTX + '/api/favorite', { id: id }).then(function (d) {
        if (d.loginRequired) {
          var next = location.pathname.slice(CTX.length) + location.search;
          toast(d.error, { link: { href: '/login?next=' + encodeURIComponent(next), text: 'Log in' } });
          return;
        }
        if (!d.ok) { toast(d.error, { error: true }); return; }
        if (ITEMS[id]) ITEMS[id].favorite = d.favorite;
        $$('[data-fav="' + id + '"]').forEach(function (b) {
          b.classList.toggle('on', d.favorite);
          b.setAttribute('aria-pressed', d.favorite);
          if (b.classList.contains('btn')) b.innerHTML = d.favorite ? '&#9829; Saved' : '&#9825; Save';
          b.classList.remove('pop'); void b.offsetWidth; b.classList.add('pop');
        });
        toast(d.favorite ? 'Saved to favorites' : 'Removed from favorites');
        if (!d.favorite && window.__menuRefresh) window.__menuRefresh();
      });
      return;
    }

    if ((el = t.closest('[data-quick]'))) {
      hideSuggest();
      openQuick(el.dataset.quick);
      return;
    }

    if (t.closest('[data-close-modal]') || t.matches('[data-modal-back]')) { closeModal(); return; }

    if ((el = t.closest('[data-copy]'))) {
      var code = el.dataset.copy;
      copyText(code).then(function () {
        toast('Code ' + code + ' copied. Paste it in your cart.', { link: { href: '/cart', text: 'Go to cart' } });
      });
      return;
    }

    if ((el = t.closest('[data-toggle-pw]'))) {
      var input = document.getElementById(el.dataset.togglePw);
      var show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      el.textContent = show ? 'Hide' : 'Show';
      el.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
      return;
    }

    if (!t.closest('.nav-search')) hideSuggest();
  });

  /* ------------------------------ search suggestions ------------------------------ */
  var searchBox = $('#nav-search');
  var suggestList = $('#suggest');
  var suggestSel = -1;
  function hideSuggest() { if (suggestList) { suggestList.hidden = true; suggestList.innerHTML = ''; suggestSel = -1; } }
  function initSuggest() {
    if (!searchBox) return;
    var run = debounce(function () {
      var q = searchBox.value.trim();
      if (q.length < 2) { hideSuggest(); return; }
      getJSON(CTX + '/api/menu?limit=6&q=' + encodeURIComponent(q)).then(function (d) {
        if (searchBox.value.trim() !== q) return;   // the person kept typing; ignore this late answer
        suggestSel = -1;
        suggestList.innerHTML = d.items.length
          ? d.items.map(function (f) {
              ITEMS[f.id] = f;
              return '<li><button type="button" data-quick="' + f.id + '"><span aria-hidden="true">' + esc(f.emoji) + '</span> ' + esc(f.name) +
                     '<span class="price">' + money(f.price) + '</span></button></li>';
            }).join('')
          : '<li class="none">No dishes match &ldquo;' + esc(q) + '&rdquo;</li>';
        suggestList.hidden = false;
      });
    }, 200);
    searchBox.addEventListener('input', run);
    searchBox.addEventListener('keydown', function (e) {
      var btns = $$('button', suggestList);
      if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
        if (!btns.length) return;
        e.preventDefault();
        suggestSel = (suggestSel + (e.key === 'ArrowDown' ? 1 : -1) + btns.length) % btns.length;
        btns.forEach(function (b, i) { b.setAttribute('aria-selected', i === suggestSel); });
      } else if (e.key === 'Enter') {
        e.preventDefault();
        if (suggestSel >= 0 && btns[suggestSel]) btns[suggestSel].click();
        else if (searchBox.value.trim()) location.href = CTX + '/menu?q=' + encodeURIComponent(searchBox.value.trim());
      } else if (e.key === 'Escape') {
        hideSuggest();
      }
    });
  }

  /* ------------------------------ menu page: filters without reload ------------------------------ */
  function initMenu() {
    var root = $('#menu-root');
    if (!root) return;
    var grid = $('#menu-grid');
    var count = $('#menu-count');
    var maxPrice = Number(root.dataset.max);
    var search = $('#f-search'), price = $('#f-price'), priceLabel = $('#f-price-label'), fav = $('#f-fav');
    var state = { q: '', cat: '', type: '', max: maxPrice, fav: false };
    var ticket = 0;

    var p = new URLSearchParams(location.search);
    state.q = p.get('q') || '';
    state.cat = p.get('cat') || '';
    state.type = p.get('type') || '';
    state.fav = p.get('fav') === '1';

    function reflect() {   // make the controls match the state
      search.value = state.q;
      fav.checked = state.fav;
      price.value = state.max;
      priceLabel.textContent = money(state.max);
      $$('#f-cats .chip').forEach(function (c) { c.classList.toggle('active', c.dataset.cat === state.cat); });
      $$('#f-type button').forEach(function (b) { b.classList.toggle('active', b.dataset.type === state.type); });
    }

    function syncUrl() {
      var q = new URLSearchParams();
      if (state.q) q.set('q', state.q);
      if (state.cat) q.set('cat', state.cat);
      if (state.type) q.set('type', state.type);
      if (state.fav) q.set('fav', '1');
      history.replaceState(null, '', location.pathname + (q.toString() ? '?' + q : ''));
    }

    function load() {
      syncUrl();
      if (state.fav && !LOGGED_IN) {
        count.textContent = '';
        grid.innerHTML = '<div class="empty-state" style="grid-column:1/-1"><div class="empty-emoji">&#9829;</div><h3>Log in to see your favorites</h3>' +
          '<p class="muted">Tap the heart on any dish to save it.</p><a class="btn" href="' + CTX + '/login?next=' + encodeURIComponent('/menu?fav=1') + '">Log in</a></div>';
        return;
      }
      var my = ++ticket;
      skeletons(grid, 6);
      var q = new URLSearchParams();
      if (state.q) q.set('q', state.q);
      if (state.cat) q.set('cat', state.cat);
      if (state.type) q.set('type', state.type);
      if (state.max < maxPrice) q.set('max', state.max);
      if (state.fav) q.set('fav', 'true');
      getJSON(CTX + '/api/menu?' + q).then(function (d) {
        if (my !== ticket) return;     // an older request finished late: ignore it
        count.textContent = d.items.length + (d.items.length === 1 ? ' dish' : ' dishes');
        grid.innerHTML = d.items.length ? d.items.map(foodCard).join('') :
          '<div class="empty-state" style="grid-column:1/-1"><div class="empty-emoji">&#127869;</div><h3>Nothing matches</h3>' +
          '<p class="muted">Try a different word, or clear the filters.</p><button type="button" class="btn btn-sm" data-clear>Clear filters</button></div>';
      }).catch(function () {
        grid.innerHTML = '<p class="muted">Could not load the menu. <button type="button" class="link" data-clear>Try again</button></p>';
      });
    }
    window.__menuRefresh = load;

    function clear() {
      state.q = ''; state.cat = ''; state.type = ''; state.max = maxPrice; state.fav = false;
      reflect(); load();
    }

    search.addEventListener('input', debounce(function () { state.q = search.value.trim(); load(); }, 250));
    price.addEventListener('input', function () { state.max = Number(price.value); priceLabel.textContent = money(state.max); });
    price.addEventListener('change', load);
    fav.addEventListener('change', function () { state.fav = fav.checked; load(); });
    $('#f-cats').addEventListener('click', function (e) {
      var c = e.target.closest('.chip'); if (!c) return;
      state.cat = c.dataset.cat; reflect(); load();
    });
    $('#f-type').addEventListener('click', function (e) {
      var b = e.target.closest('button'); if (!b) return;
      state.type = b.dataset.type; reflect(); load();
    });
    $('#clear-filters').addEventListener('click', clear);
    grid.addEventListener('click', function (e) { if (e.target.closest('[data-clear]')) clear(); });

    reflect();
    load();
  }

  /* ------------------------------ cart + checkout summary ------------------------------ */
  function mountCart(root, mode) {
    var msg = null;     // coupon message shown under the coupon box

    function totalsHTML(bill) {
      var coupon = bill.couponCode
        ? '<div class="applied"><span>&#127991;&#65039; ' + esc(bill.couponCode) + ' applied</span><button type="button" class="link" data-remove-coupon>Remove</button></div>'
        : '<form class="coupon-form" data-coupon-form><label class="sr-only" for="coupon-input">Coupon code</label>' +
          '<input id="coupon-input" type="text" placeholder="Coupon code" autocomplete="off" maxlength="30">' +
          '<button class="btn btn-sm btn-ghost" type="submit">Apply</button></form>';
      var hint = '';
      if (!bill.couponCode && bill.subtotal > 0 && bill.subtotal < 800) {
        hint = '<p class="muted">Add ' + money(800 - bill.subtotal) + ' more for a flat ' + RUPEE + '50 off.</p>';
      } else if (bill.note) {
        hint = '<p class="muted">' + esc(bill.note.replace('Rs.', RUPEE)) + '.</p>';
      }
      return coupon +
        '<p class="coupon-msg ' + (msg ? msg.type : '') + '" aria-live="polite">' + (msg ? esc(msg.text) : '') + '</p>' +
        '<dl>' +
          '<div class="row"><dt>Subtotal</dt><dd>' + money(bill.subtotal, true) + '</dd></div>' +
          (bill.discount > 0 ? '<div class="row discount"><dt>Discount' + (bill.couponCode ? ' (' + esc(bill.couponCode) + ')' : '') + '</dt><dd>&minus;' + money(bill.discount, true) + '</dd></div>' : '') +
          '<div class="row"><dt>GST (5%)</dt><dd>' + money(bill.gst, true) + '</dd></div>' +
          '<div class="row total"><dt>Total</dt><dd>' + money(bill.total, true) + '</dd></div>' +
        '</dl>' + hint;
    }

    function rowHTML(i) {
      var art = i.image ? '<img src="' + esc(i.image) + '" alt="">' : esc(i.emoji);
      return '<div class="cart-row" data-id="' + i.foodId + '">' +
        '<div class="cart-art ' + tintOf(i.foodId) + '" aria-hidden="true">' + art + '</div>' +
        '<div><div class="cart-name"><span class="vdot ' + (i.veg ? '' : 'nonveg') + '"></span>' + esc(i.name) + '</div>' +
          '<small class="muted">' + money(i.price) + ' each</small> &nbsp;<button type="button" class="remove-btn" data-remove="' + i.foodId + '">Remove</button></div>' +
        '<div class="stepper" role="group" aria-label="Quantity of ' + esc(i.name) + '">' +
          '<button type="button" data-qty="' + i.foodId + '" data-delta="-1" aria-label="One less ' + esc(i.name) + '">&minus;</button>' +
          '<output aria-live="polite">' + i.quantity + '</output>' +
          '<button type="button" data-qty="' + i.foodId + '" data-delta="1" aria-label="One more ' + esc(i.name) + '">+</button></div>' +
        '<div class="line-total">' + money(i.lineTotal, true) + '</div></div>';
    }

    function draw(bill, focusSel) {
      if (!bill.items.length) {
        if (mode === 'checkout') { location.href = CTX + '/menu'; return; }
        root.className = '';
        root.innerHTML = '<div class="panel empty-state"><div class="empty-emoji">&#128722;</div><h2>Your cart is empty</h2>' +
          '<p class="muted">Add a few dishes and they will show up here.</p><a class="btn" href="' + CTX + '/menu">Browse the menu</a></div>';
        return;
      }
      if (mode === 'cart') {
        root.className = 'cart-layout';
        root.innerHTML =
          '<div class="panel cart-list">' + bill.items.map(rowHTML).join('') +
            '<div class="row-actions"><button type="button" class="btn btn-sm btn-ghost" data-clear-cart>Clear cart</button></div></div>' +
          '<div class="panel summary"><h2>Bill summary</h2>' + totalsHTML(bill) +
            '<a class="btn btn-lg" href="' + CTX + '/checkout">Proceed to checkout</a></div>';
      } else {
        root.innerHTML = '<div class="panel summary"><h2>Your order</h2><ul class="mini-items">' +
          bill.items.map(function (i) { return '<li><span>' + esc(i.name) + ' &times; ' + i.quantity + '</span><span>' + money(i.lineTotal, true) + '</span></li>'; }).join('') +
          '</ul>' + totalsHTML(bill) + '<a class="link" href="' + CTX + '/cart">Edit cart</a></div>';
      }
      if (focusSel) { var f = $(focusSel, root); if (f) f.focus(); }
    }

    function apply(promise, focusSel) {
      return promise.then(function (d) {
        if (d.bill) draw(d.bill, focusSel);
        if (!d.ok && d.error && !msg) toast(d.error, { error: true });
      });
    }

    root.addEventListener('click', function (e) {
      var el;
      if ((el = e.target.closest('[data-qty]'))) {
        var row = el.closest('.cart-row');
        var qty = Number($('output', row).textContent) + Number(el.dataset.delta);
        msg = null;
        apply(cartAction({ action: 'set', id: el.dataset.qty, qty: qty }),
              qty > 0 ? '[data-qty="' + el.dataset.qty + '"][data-delta="' + el.dataset.delta + '"]' : null);
      } else if ((el = e.target.closest('[data-remove]'))) {
        msg = null;
        apply(cartAction({ action: 'remove', id: el.dataset.remove }));
      } else if (e.target.closest('[data-remove-coupon]')) {
        msg = { text: 'Coupon removed.', type: '' };
        apply(cartAction({ action: 'coupon', code: '' }));
      } else if (e.target.closest('[data-clear-cart]')) {
        msg = null;
        apply(cartAction({ action: 'clear' }));
      }
    });

    root.addEventListener('submit', function (e) {
      var form = e.target.closest('[data-coupon-form]');
      if (!form) return;
      e.preventDefault();
      var code = $('input', form).value.trim();
      if (!code) { msg = { text: 'Type a coupon code first.', type: 'error' }; refresh(true); return; }
      cartAction({ action: 'coupon', code: code }).then(function (d) {
        msg = d.ok ? { text: d.message, type: 'ok' } : { text: d.error, type: 'error' };
        if (d.bill) draw(d.bill, '#coupon-input');
      });
    });

    function refresh(keepFocus) {
      return getJSON(CTX + '/api/cart').then(function (d) { setBadge(d.bill.count); draw(d.bill, keepFocus ? '#coupon-input' : null); })
        .catch(function () { root.innerHTML = '<div class="panel">Could not load your cart. <button type="button" class="link" onclick="location.reload()">Reload</button></div>'; });
    }
    refresh();
  }

  /* ------------------------------ forms: instant validation ------------------------------ */
  function pwScore(v) {
    return (v.length >= 8 ? 1 : 0) + (/[a-z]/.test(v) && /[A-Z]/.test(v) ? 1 : 0) + (/\d/.test(v) ? 1 : 0) + (/[^A-Za-z0-9]/.test(v) ? 1 : 0);
  }

  function initForms() {
    $$('form[data-validate]').forEach(function (form) {
      var kind = form.dataset.validate;
      var rules = {
        name: function (v) { return v.trim().length >= 2 || 'Enter your name (at least 2 letters).'; },
        email: function (v) { return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v.trim()) || 'Enter a valid email, like name@example.com.'; },
        phone: function (v) { return /^[6-9]\d{9}$/.test(v) || 'Enter a 10-digit mobile number starting with 6, 7, 8 or 9.'; },
        password: function (v) {
          if (kind === 'login') return v.length > 0 || 'Enter your password.';
          if (v.length < 8) return 'Use at least 8 characters.';
          if (!/[A-Za-z]/.test(v) || !/\d/.test(v)) return 'Add at least one letter and one number.';
          return true;
        },
        confirm: function (v) { return v === form.elements.password.value || 'The two passwords do not match.'; }
      };
      var touched = {};

      function check(input) {
        var rule = rules[input.name];
        if (!rule) return true;
        var result = rule(input.value);
        var ok = result === true;
        var msgEl = input.closest('.field').querySelector('.field-msg');
        input.classList.toggle('invalid', !ok);
        input.classList.toggle('valid', ok);
        if (msgEl) {
          if (input.name === 'password' && kind === 'register') {
            var s = pwScore(input.value);
            var labels = ['', 'Weak', 'Okay', 'Good', 'Strong'];
            $('#strength').dataset.score = input.value ? s : 0;
            msgEl.textContent = ok ? 'Password strength: ' + labels[Math.max(s, 1)] : result;
            msgEl.className = 'field-msg ' + (ok ? 'ok' : 'error');
          } else {
            msgEl.textContent = ok ? '' : result;
            msgEl.className = 'field-msg' + (ok ? '' : ' error');
          }
        }
        return ok;
      }

      form.addEventListener('input', function (e) {
        var input = e.target;
        if (input.name === 'phone') input.value = input.value.replace(/\D/g, '').slice(0, 10);
        if (touched[input.name] || input.name === 'password') check(input);
        if (input.name === 'password' && form.elements.confirm && touched.confirm) check(form.elements.confirm);
      });
      form.addEventListener('focusout', function (e) {
        if (!e.target.name) return;
        touched[e.target.name] = true;
        check(e.target);
      });
      form.addEventListener('submit', function (e) {
        var firstBad = null;
        $$('input', form).forEach(function (input) {
          if (!input.name || input.disabled) return;
          touched[input.name] = true;
          if (!check(input) && !firstBad) firstBad = input;
        });
        if (firstBad) { e.preventDefault(); firstBad.focus(); return; }
        var btn = $('button[type="submit"]', form);
        if (btn) { btn.disabled = true; btn.textContent = 'Please wait...'; }
      });
    });

    var checkout = $('#checkout-form');
    if (checkout) {
      checkout.addEventListener('submit', function (e) {
        var addr = $('#address', checkout);
        var msgEl = addr.closest('.field').querySelector('.field-msg');
        if (addr.value.trim().length < 10) {
          e.preventDefault();
          addr.classList.add('invalid');
          msgEl.textContent = 'Enter your full delivery address (at least 10 characters).';
          msgEl.className = 'field-msg error';
          addr.focus();
          return;
        }
        var btn = $('#place-order');
        btn.disabled = true; btn.textContent = 'Placing your order...';   // stops double orders
      });
    }
  }

  /* ------------------------------ carousel ------------------------------ */
  function initCarousel() {
    $$('[data-carousel]').forEach(function (root) {
      var slides = $$('.slide', root);
      if (slides.length < 2) { root.classList.add('single'); return; }
      var dotsBox = $('.dots', root);
      dotsBox.innerHTML = slides.map(function (_, i) { return '<button type="button" aria-label="Show offer ' + (i + 1) + '"></button>'; }).join('');
      var dots = $$('button', dotsBox);
      var index = 0, timer = null;

      function go(n) {
        index = (n + slides.length) % slides.length;
        slides.forEach(function (s, i) {
          s.classList.toggle('is-active', i === index);
          s.setAttribute('aria-hidden', i !== index);
        });
        dots.forEach(function (d, i) { d.setAttribute('aria-current', i === index); });
      }
      function start() {
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
        stop();
        timer = setInterval(function () { go(index + 1); }, 5500);
      }
      function stop() { if (timer) { clearInterval(timer); timer = null; } }

      $('.prev', root).addEventListener('click', function () { go(index - 1); start(); });
      $('.next', root).addEventListener('click', function () { go(index + 1); start(); });
      dots.forEach(function (d, i) { d.addEventListener('click', function () { go(i); start(); }); });
      root.addEventListener('mouseenter', stop);
      root.addEventListener('mouseleave', start);
      root.addEventListener('focusin', stop);
      root.addEventListener('focusout', start);

      // swipe on phones
      var x0 = null;
      root.addEventListener('touchstart', function (e) { x0 = e.touches[0].clientX; stop(); }, { passive: true });
      root.addEventListener('touchend', function (e) {
        if (x0 !== null) {
          var dx = e.changedTouches[0].clientX - x0;
          if (Math.abs(dx) > 40) go(index + (dx < 0 ? 1 : -1));
        }
        x0 = null; start();
      });

      go(0);
      start();
    });
  }

  /* ------------------------------ orders: live status ------------------------------ */
  function initOrders() {
    var cards = $$('[data-order-id]');
    if (!cards.length) return;
    var labels = { PLACED: 'Placed', PREPARING: 'Preparing', DELIVERED: 'Delivered', CANCELLED: 'Cancelled' };
    setInterval(function () {
      if (document.hidden) return;
      getJSON(CTX + '/api/order-status').then(function (d) {
        if (!d.ok) return;
        cards.forEach(function (c) {
          var id = c.dataset.orderId, s = d.statuses[id];
          var tracker = c.matches('.tracker') ? c : $('.tracker', c);
          if (!s || !tracker || tracker.dataset.status === s) return;
          tracker.dataset.status = s;
          var pill = $('[data-status-pill]', c);
          if (pill) { pill.textContent = labels[s]; pill.className = 'pill status-' + s; }
          toast('Order #' + id + ' is now ' + labels[s].toLowerCase());
        });
      }).catch(function () { /* try again at the next tick */ });
    }, 15000);
  }

  /* ------------------------------ admin helpers ------------------------------ */
  function initAdmin() {
    $$('form[data-autosubmit] select').forEach(function (sel) {
      sel.addEventListener('change', function () { sel.form.submit(); });
    });
    $$('form[data-confirm]').forEach(function (form) {
      form.addEventListener('submit', function (e) {
        if (!window.confirm(form.dataset.confirm)) e.preventDefault();
      });
    });
  }

  /* ------------------------------ start-up ------------------------------ */
  function init() {
    initTheme();
    initSuggest();
    initForms();
    initCarousel();
    initOrders();
    initAdmin();

    var page = ($('#main') || {}).dataset ? $('#main').dataset.page : '';
    if (page === 'menu') initMenu();
    if (page === 'cart') mountCart($('#cart-root'), 'cart');
    if (page === 'checkout') mountCart($('#summary-root'), 'checkout');
    $$('[data-food-grid]').forEach(function (g) {
      if (g.dataset.foodGrid !== 'menu') loadGrid(g, g.dataset.query);
    });

    if (body.dataset.flash) toast(body.dataset.flash);
  }
  init();
})();
