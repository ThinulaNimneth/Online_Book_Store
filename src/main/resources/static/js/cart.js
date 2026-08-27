/* ==========================================================================
   cart.js
   Real endpoints:
     GET    /carts                    -> { items: [{cartItemId, book, quantity, unitPrice}], ... }
     PUT    /carts/items/{cartItemId} -> { quantity }
     DELETE /carts/items/{cartItemId}
   ========================================================================== */

const SHIPPING_FLAT = 350;

const DEMO_CART = [
    { cartItemId: 1, bookId: 1, title: "The Silence of the Sea", author: "K. Jayatilaka", unitPrice: 1190, quantity: 1 },
    { cartItemId: 2, bookId: 5, title: "Little Star's Big Journey", author: "A. Perera", unitPrice: 430, quantity: 2 },
];

let cartItems = [];

$(document).ready(function () {
    initLayout();
    loadCart();
});

function loadCart() {
    api.get("/carts")
        .done(res => renderCart((res.body && res.body.items) || []))
        .fail(() => renderCart(DEMO_CART));
}

function renderCart(items) {
    cartItems = items;
    const $wrap = $("#cart-items").empty();
    $("#cart-empty").toggle(items.length === 0);
    $("#checkout-btn").toggleClass("btn-disabled", items.length === 0);

    items.forEach(item => {
        $wrap.append(`
            <div class="cart-item" data-cart-item-id="${item.cartItemId}">
                <div class="cart-item-cover"></div>
                <div>
                    <div class="cart-item-title">${escapeHtml(item.title)}</div>
                    <div class="cart-item-author">by ${escapeHtml(item.author)}</div>
                    <div class="cart-item-meta">
                        <div class="stepper">
                            <button type="button" class="qty-minus">&minus;</button>
                            <span class="qty-value">${item.quantity}</span>
                            <button type="button" class="qty-plus">+</button>
                        </div>
                        <span class="remove-link">Remove</span>
                    </div>
                </div>
                <div class="cart-item-price">${formatLKR(item.unitPrice * item.quantity)}</div>
            </div>
        `);
    });

    bindCartEvents();
    updateSummary();
}

function bindCartEvents() {
    $(".qty-minus").off("click").on("click", function () { changeQty($(this), -1); });
    $(".qty-plus").off("click").on("click", function () { changeQty($(this), 1); });
    $(".remove-link").off("click").on("click", function () { removeItem($(this)); });
}

function changeQty($btn, delta) {
    const $row = $btn.closest(".cart-item");
    const cartItemId = $row.data("cart-item-id");
    const item = cartItems.find(i => i.cartItemId === cartItemId);
    if (!item) return;

    item.quantity = Math.max(1, item.quantity + delta);
    $row.find(".qty-value").text(item.quantity);
    $row.find(".cart-item-price").text(formatLKR(item.unitPrice * item.quantity));
    updateSummary();

    api.put("/carts/items/" + cartItemId, { quantity: item.quantity })
        .fail(() => {}); // demo mode - local state already updated
}

function removeItem($el) {
    const $row = $el.closest(".cart-item");
    const cartItemId = $row.data("cart-item-id");

    cartItems = cartItems.filter(i => i.cartItemId !== cartItemId);
    $row.remove();
    updateSummary();
    $("#cart-empty").toggle(cartItems.length === 0);

    api.del("/carts/items/" + cartItemId).fail(() => {});
}

function updateSummary() {
    const subtotal = cartItems.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);
    const shipping = cartItems.length > 0 ? SHIPPING_FLAT : 0;
    $("#sum-subtotal").text(formatLKR(subtotal));
    $("#sum-shipping").text(formatLKR(shipping));
    $("#sum-total").text(formatLKR(subtotal + shipping));

    // Keep the navbar's cart count/total in sync with this page's real state,
    // rather than the incremental guess other pages use before a real
    // GET /carts endpoint exists.
    const totalQty = cartItems.reduce((sum, i) => sum + i.quantity, 0);
    localStorage.setItem("potha_cart_count", totalQty);
    localStorage.setItem("potha_cart_total", subtotal);
    if (typeof refreshNavCounts === "function") refreshNavCounts();
}

function escapeHtml(str) {
    return $("<div>").text(str || "").html();
}
