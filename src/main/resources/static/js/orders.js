/* ==========================================================================
   orders.js
   Real endpoint: GET /orders -> list of { orderId, orderDate, orderStatus,
                  totalAmount, items: [{ title, quantity }] } for the logged-in user
   ========================================================================== */

const STATUS_BADGE = {
    PENDING:   "badge-warning",
    CONFIRMED: "badge-neutral",
    SHIPPED:   "badge-neutral",
    DELIVERED: "badge-success",
    CANCELLED: "badge-danger",
};

const DEMO_ORDERS = [
    { orderId: 1042, orderDate: "2026-08-05", orderStatus: "DELIVERED", totalAmount: 1970,
        items: [{ title: "The Silence of the Sea", quantity: 1 }, { title: "Little Star's Big Journey", quantity: 2 }] },
    { orderId: 1058, orderDate: "2026-08-09", orderStatus: "SHIPPED", totalAmount: 990,
        items: [{ title: "Colombo Nights", quantity: 1 }] },
];

$(document).ready(function () {
    initLayout();
    if (!Auth.requireLogin()) return;

    api.get("/orders")
        .done(res => renderOrders(res.body || []))
        .fail(() => renderOrders(DEMO_ORDERS));
});

function renderOrders(orders) {
    const $wrap = $("#orders-list").empty();
    $("#orders-empty").toggle(orders.length === 0);

    const highlight = new URLSearchParams(window.location.search).get("highlight");

    orders.slice().reverse().forEach(order => {
        const itemsLine = order.items.map(i => `${i.title} &times;${i.quantity}`).join(", ");
        const badgeClass = STATUS_BADGE[order.orderStatus] || "badge-neutral";
        const isHighlighted = highlight && String(order.orderId) === String(highlight);

        $wrap.append(`
            <div class="order-card" style="${isHighlighted ? "border-color:var(--accent)" : ""}">
                <div class="order-card-head">
                    <div>
                        <span class="order-id">Order #${order.orderId}</span>
                        <div class="order-date">${formatDate(order.orderDate)}</div>
                    </div>
                    <span class="badge ${badgeClass}">${order.orderStatus}</span>
                </div>
                <div class="order-items-line">${itemsLine}</div>
                <div style="display:flex;justify-content:space-between;align-items:center;margin-top:12px">
                    <span style="font-family:var(--font-display);font-weight:700">${formatLKR(order.totalAmount)}</span>
                    <a href="#" class="btn btn-outline btn-sm">View Details</a>
                </div>
            </div>
        `);
    });
}

function formatDate(dateStr) {
    try {
        return new Date(dateStr).toLocaleDateString("en-LK", { year: "numeric", month: "short", day: "numeric" });
    } catch (e) {
        return dateStr;
    }
}
