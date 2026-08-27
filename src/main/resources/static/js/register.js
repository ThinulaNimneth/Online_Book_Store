/* ==========================================================================
   register.js
   Real endpoint: POST /api/v1/auth/register { fullName, email, phone, password }
                  -> CommonResponse.body = { token, userId, fullName, roles }
                  (auto-login after registration, same as most storefronts)
   ========================================================================== */

$(document).ready(function () {
    initLayout();

    $("#facebook-signup-btn, #google-signup-btn").on("click", function () {
        showToast("Social sign-up isn't part of the core coursework spec - TODO if you want it as a bonus.");
    });

    $("#register-form").on("submit", function (e) {
        e.preventDefault();
        $("#register-error").removeClass("show");

        const payload = {
            fullName: $("#fullName").val().trim(),
            email: $("#email").val().trim(),
            phone: $("#phone").val().trim(),
            password: $("#password").val()
        };
        const $btn = $("#register-submit").prop("disabled", true).text("Creating account\u2026");

        api.post("/auth/register", payload)
            .done(function (res) {
                const body = res.body || {};
                if (body.token) {
                    Auth.setSession(body.token, { userId: body.userId, fullName: body.fullName, roles: body.roles || ["USER"] });
                    showToast("Account created - welcome to Potha!");
                    window.location.href = "index.html";
                } else {
                    showToast("Account created - please sign in");
                    window.location.href = "login.html";
                }
            })
            .fail(function (xhr) {
                const msg = (xhr.responseJSON && xhr.responseJSON.message) || "Could not reach the server - is the backend running?";
                $("#register-error").text(msg).addClass("show");
            })
            .always(function () {
                $btn.prop("disabled", false).text("Create Account");
            });
    });
});
