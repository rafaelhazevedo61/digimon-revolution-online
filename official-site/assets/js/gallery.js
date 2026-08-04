(() => {
  const items = Array.isArray(window.DRO_GALLERY) ? window.DRO_GALLERY : [];
  const grids = document.querySelectorAll('[data-gallery-grid]');

  const card = (item) => `
    <button class="gallery-card" type="button" data-gallery-open="${item.id}" aria-label="Ampliar ${item.title}">
      <span class="gallery-image-wrap"><img src="${item.image}" alt="${item.title}" loading="lazy" /></span>
      <span class="gallery-card-overlay"><small>${item.category}</small><strong>${item.title}</strong><span>Visualizar imagem ↗</span></span>
    </button>`;

  grids.forEach((grid) => {
    const limit = Number(grid.dataset.limit || 0);
    const category = grid.dataset.category || 'Todos';
    let filtered = category === 'Todos' ? items : items.filter((item) => item.category === category);
    if (limit > 0) filtered = filtered.slice(0, limit);
    grid.innerHTML = filtered.length ? filtered.map(card).join('') : '<p class="gallery-empty">Nenhuma imagem encontrada nesta categoria.</p>';
  });

  const modal = document.querySelector('[data-gallery-modal]');
  const modalImage = modal?.querySelector('[data-gallery-modal-image]');
  const modalTitle = modal?.querySelector('[data-gallery-modal-title]');
  const modalDescription = modal?.querySelector('[data-gallery-modal-description]');
  const modalCategory = modal?.querySelector('[data-gallery-modal-category]');
  let previousFocus = null;

  const close = () => {
    if (!modal) return;
    modal.hidden = true;
    document.body.classList.remove('gallery-modal-open');
    previousFocus?.focus?.();
  };

  const open = (id) => {
    const item = items.find((entry) => entry.id === id);
    if (!item || !modal) return;
    previousFocus = document.activeElement;
    modalImage.src = item.image;
    modalImage.alt = item.title;
    modalTitle.textContent = item.title;
    modalDescription.textContent = item.description;
    modalCategory.textContent = item.category;
    modal.hidden = false;
    document.body.classList.add('gallery-modal-open');
    modal.querySelector('[data-gallery-close]')?.focus();
  };

  document.addEventListener('click', (event) => {
    const trigger = event.target.closest('[data-gallery-open]');
    if (trigger) open(trigger.dataset.galleryOpen);
    if (event.target.closest('[data-gallery-close]') || event.target.matches('[data-gallery-backdrop]')) close();
  });

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && modal && !modal.hidden) close();
  });

  document.querySelectorAll('[data-gallery-filter]').forEach((button) => {
    button.addEventListener('click', () => {
      const filter = button.dataset.galleryFilter;
      document.querySelectorAll('[data-gallery-filter]').forEach((entry) => entry.classList.toggle('is-active', entry === button));
      const fullGrid = document.querySelector('[data-gallery-grid="full"]');
      if (!fullGrid) return;
      const filtered = filter === 'Todos' ? items : items.filter((item) => item.category === filter);
      fullGrid.innerHTML = filtered.length ? filtered.map(card).join('') : '<p class="gallery-empty">Nenhuma imagem encontrada nesta categoria.</p>';
    });
  });
})();
