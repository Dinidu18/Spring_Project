(() => {
  const toggle = document.querySelector(".nav-toggle");
  const nav = document.querySelector("#site-nav");
  if (toggle && nav) {
    toggle.addEventListener("click", () => {
      const open = nav.classList.toggle("is-open");
      toggle.setAttribute("aria-expanded", open ? "true" : "false");
    });
  }

  document.querySelectorAll("form[data-busy-guard]").forEach((form) => {
    form.addEventListener("submit", () => {
      const btn = form.querySelector('button[type="submit"]');
      if (btn && !btn.disabled) {
        btn.disabled = true;
        btn.dataset.originalText = btn.textContent;
        btn.textContent = "Working…";
      }
    });
  });

  document.querySelectorAll("form[data-star-rating]").forEach((form) => {
    const rating = form.querySelector('[name="rating"]');
    if (!rating) return;
    rating.addEventListener("input", () => {
      const n = Number(rating.value || 0);
      rating.setAttribute("aria-valuetext", `${n} out of 5 stars`);
    });
  });

  const search = document.querySelector(".header-search");
  if (search) {
    search.addEventListener("pointerenter", () => search.classList.add("is-hot"));
    search.addEventListener("pointerleave", () => search.classList.remove("is-hot"));
  }
})();
