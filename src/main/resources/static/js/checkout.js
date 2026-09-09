/* ==========================================================================
   checkout.js
   Real endpoints:
     GET  /addresses           -> saved addresses for the address-select dropdown
     POST /addresses           -> save a new address (if "new address" fields used)
     GET  /carts                -> to render the order summary
     POST /orders               -> { addressId, paymentMethod } -> creates Order + OrderItems
     POST /payments              -> { orderId, paymentMethod } -> simulated Payment record
   ========================================================================== */

const SHIPPING_FLAT = 350;
let checkoutCart = [];

$(document).ready(function () {
    initLayout();
    if (!Auth.requireLogin()) return;

    loadAddresses();
    loadCartSummary();

    $("#place-order-btn").on("click", placeOrder);
});

function loadAddresses() {
    api.get("/addresses")
        .done(res => {
            const $select = $("#address-select");
            (res.body || []).forEach(addr => {
                $select.append(`<option value="${addr.addressId}">${escapeHtml(addr.line1)}, ${escapeHtml(addr.city)}</option>`);
            });
        })
        .fail(xhr => {
            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }



        });
}

function loadCartSummary() {
    api.get("/carts")
        .done(res => renderSummary((res.body && res.body.items) || []))
        .fail(xhr => {
            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }
            showToast("Couldn't load your cart - is the backend running?");
            renderSummary([]);
            $("#place-order-btn").prop("disabled", true);
        });
}

function renderSummary(items) {
    checkoutCart = items;
    const $wrap = $("#order-items-summary").empty();
    items.forEach(i => {
        $wrap.append(`<div class="summary-row"><span>${escapeHtml(i.title)} &times; ${i.quantity}</span><span>${formatLKR(i.unitPrice * i.quantity)}</span></div>`);
    });
    const subtotal = items.reduce((s, i) => s + i.unitPrice * i.quantity, 0);
    const shipping = items.length ? SHIPPING_FLAT : 0;
    $("#sum-subtotal").text(formatLKR(subtotal));
    $("#sum-shipping").text(formatLKR(shipping));
    $("#sum-total").text(formatLKR(subtotal + shipping));
}

function placeOrder() {
    if (!checkoutCart.length) {
        showToast("Your cart is empty");
        return;
    }

    const addressId = $("#address-select").val();
    const paymentMethod = $('input[name="paymentMethod"]:checked').val();

    if (!paymentMethod) {
        showToast("Choose a payment method");
        return;
    }

    const payload = addressId
        ? { addressId: addressId, paymentMethod: paymentMethod }
        : {
            paymentMethod: paymentMethod,
            newAddress: {
                line1: $("#line1").val().trim(),
                city: $("#city").val().trim(),
                district: $("#district").val().trim(),
                postalCode: $("#postalCode").val().trim()
            }
        };

    if (!addressId && !payload.newAddress.line1) {
        showToast("Add a shipping address first");
        return;
    }

    const $btn = $("#place-order-btn").prop("disabled", true).text("Placing order\u2026");

    api.post("/orders", payload)
        .done(function (res) {
            const orderId = res.body && res.body.orderId;
            showToast("Order placed! Confirmation sent.");
            localStorage.setItem("potha_cart_count", "0");
            localStorage.setItem("potha_cart_total", "0");
            window.location.href = "orders.html" + (orderId ? "?highlight=" + orderId : "");
        })
        .fail(function (xhr) {



            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }
            const msg = (xhr.responseJSON && xhr.responseJSON.message) || "Couldn't place your order - please try again";
            showToast(msg);
            $btn.prop("disabled", false).text("Place Order");
        });


}

function escapeHtml(str) {
    return $("<div>").text(str || "").html();
}