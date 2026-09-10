/* ==========================================================================
   cart.js
   ========================================================================== */

const SHIPPING_FLAT = 350;

let cartItems = [];

$(document).ready(function () {
    initLayout();
    if (!Auth.requireLogin()) return;
    loadCart();
});

function loadCart() {
    api.get("/carts")
        .done(res => renderCart((res.body && res.body.items) || []))
        .fail(xhr => {
            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }
            showToast("Couldn't load your cart - is the backend running?");
            renderCart([]);
        });
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

    const previousQty = item.quantity;
    const newQty = Math.max(1, previousQty + delta);

    // Optimistic UI update
    item.quantity = newQty;
    $row.find(".qty-value").text(item.quantity);
    $row.find(".cart-item-price").text(formatLKR(item.unitPrice * item.quantity));
    updateSummary();

    api.put("/carts/items/" + cartItemId, { quantity: newQty })
        .fail(xhr => {
            item.quantity = previousQty;
            $row.find(".qty-value").text(previousQty);
            $row.find(".cart-item-price").text(formatLKR(item.unitPrice * previousQty));
            updateSummary();

            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }
            const msg = (xhr.responseJSON && xhr.responseJSON.message) || "Couldn't update quantity";
            showToast(msg);
        });
}

function removeItem($el) {
    const $row = $el.closest(".cart-item");
    const cartItemId = $row.data("cart-item-id");
    const removedItem = cartItems.find(i => i.cartItemId === cartItemId);
    const removedIndex = cartItems.indexOf(removedItem);

    cartItems = cartItems.filter(i => i.cartItemId !== cartItemId);
    $row.remove();
    updateSummary();
    $("#cart-empty").toggle(cartItems.length === 0);

    api.del("/carts/items/" + cartItemId)
        .fail(xhr => {
            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }
            // Removal failed server-side - restore the row instead of pretending it worked.
            if (removedItem) cartItems.splice(removedIndex, 0, removedItem);
            const msg = (xhr.responseJSON && xhr.responseJSON.message) || "Couldn't remove item - please try again";
            showToast(msg);
            renderCart(cartItems);
        });
}

function updateSummary() {
    const subtotal = cartItems.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);
    const shipping = cartItems.length > 0 ? SHIPPING_FLAT : 0;
    $("#sum-subtotal").text(formatLKR(subtotal));
    $("#sum-shipping").text(formatLKR(shipping));
    $("#sum-total").text(formatLKR(subtotal + shipping));


    // GET carts endpoint exists.
    const totalQty = cartItems.reduce((sum, i) => sum + i.quantity, 0);
    localStorage.setItem("potha_cart_count", totalQty);
    localStorage.setItem("potha_cart_total", subtotal);
    if (typeof refreshNavCounts === "function") refreshNavCounts();
}

function escapeHtml(str) {
    return $("<div>").text(str || "").html();
}