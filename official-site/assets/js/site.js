(() => {
  const header = document.querySelector('.site-header');
  const toggle = document.querySelector('.menu-toggle');
  const nav = document.querySelector('.main-nav');

  const setHeaderState = () => header?.classList.toggle('is-scrolled', window.scrollY > 24 || !document.querySelector('.hero'));
  setHeaderState();
  window.addEventListener('scroll', setHeaderState, { passive: true });

  toggle?.addEventListener('click', () => {
    const open = toggle.getAttribute('aria-expanded') === 'true';
    toggle.setAttribute('aria-expanded', String(!open));
    nav?.classList.toggle('is-open', !open);
    document.body.classList.toggle('menu-open', !open);
  });

  nav?.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', () => {
      toggle?.setAttribute('aria-expanded', 'false');
      nav.classList.remove('is-open');
      document.body.classList.remove('menu-open');
    });
  });

  const observer = 'IntersectionObserver' in window ? new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.14 }) : null;

  window.DRO_initReveal = () => {
    document.querySelectorAll('.reveal:not([data-reveal-ready])').forEach((element) => {
      element.dataset.revealReady = 'true';
      if (observer) observer.observe(element);
      else element.classList.add('is-visible');
    });
  };

  window.DRO_initReveal();
  const year = document.getElementById('current-year');
  if (year) year.textContent = String(new Date().getFullYear());

  const ctaModal = document.querySelector('[data-cta-modal]');
  if (ctaModal) {
    const openCtaModal = () => {
      ctaModal.hidden = false;
      document.body.classList.add('cta-modal-open');
    };
    const closeCtaModal = () => {
      ctaModal.hidden = true;
      document.body.classList.remove('cta-modal-open');
    };
    ctaModal.querySelector('[data-cta-modal-backdrop]')?.addEventListener('click', closeCtaModal);
    ctaModal.querySelector('[data-cta-modal-close]')?.addEventListener('click', closeCtaModal);
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && !ctaModal.hidden) closeCtaModal(); });
    document.querySelectorAll('a[href="../game-frontend/index.html"]').forEach((link) => {
      link.addEventListener('click', (e) => { e.preventDefault(); openCtaModal(); });
    });
  }
})();
