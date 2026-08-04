(() => {
  const news = Array.isArray(window.DRO_NEWS) ? [...window.DRO_NEWS] : [];
  const formatDate = (value) => new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit', month: 'long', year: 'numeric'
  }).format(new Date(`${value}T12:00:00`));

  const escapeHtml = (value = '') => value.replace(/[&<>'"]/g, (char) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
  }[char]));

  const createCard = (item, featured = false) => `
    <article class="news-card${featured ? ' news-card-featured' : ''}">
      <a class="news-card-link" href="patch-notes.html?id=${encodeURIComponent(item.id)}" aria-label="Ler ${escapeHtml(item.title)}">
        <div class="news-card-visual" aria-hidden="true">
          <span class="news-version">${escapeHtml(item.version)}</span>
          <span class="news-pulse"></span>
          <strong>DRO</strong>
        </div>
        <div class="news-card-content">
          <div class="news-meta"><span>${escapeHtml(item.type)}</span><time datetime="${item.date}">${formatDate(item.date)}</time></div>
          <h3>${escapeHtml(item.title)}</h3>
          <p>${escapeHtml(item.summary)}</p>
          <span class="news-card-action">Ler publicação <b aria-hidden="true">→</b></span>
        </div>
      </a>
    </article>`;

  const homeContainer = document.querySelector('[data-latest-news]');
  if (homeContainer) {
    if (!news.length) {
      homeContainer.innerHTML = '<p class="news-empty">Nenhuma publicação disponível no momento.</p>';
    } else {
      homeContainer.innerHTML = news
        .sort((a, b) => b.date.localeCompare(a.date))
        .slice(0, 3)
        .map((item, index) => createCard(item, index === 0))
        .join('');
    }
  }

  const listContainer = document.querySelector('[data-news-list]');
  if (listContainer) {
    const filters = document.querySelectorAll('[data-news-filter]');
    const render = (filter = 'TODOS') => {
      const filtered = news
        .sort((a, b) => b.date.localeCompare(a.date))
        .filter((item) => filter === 'TODOS' || item.type === filter);
      listContainer.innerHTML = filtered.length
        ? filtered.map((item, index) => createCard(item, index === 0)).join('')
        : '<p class="news-empty">Nenhuma publicação encontrada neste filtro.</p>'; 
    };
    filters.forEach((button) => button.addEventListener('click', () => {
      filters.forEach((item) => item.classList.remove('is-active'));
      button.classList.add('is-active');
      render(button.dataset.newsFilter);
    }));
    render();
  }

  const articleContainer = document.querySelector('[data-news-article]');
  if (articleContainer) {
    const id = new URLSearchParams(window.location.search).get('id');
    const item = news.find((entry) => entry.id === id) || news[0];
    document.title = `${item.title} — Digimon Revolution Online`;
    articleContainer.innerHTML = `
      <header class="article-header">
        <div class="news-meta"><span>${escapeHtml(item.type)}</span><time datetime="${item.date}">${formatDate(item.date)}</time></div>
        <span class="article-version">${escapeHtml(item.version)}</span>
        <h1>${escapeHtml(item.title)}</h1>
        <p>${escapeHtml(item.summary)}</p>
      </header>
      <div class="article-body">
        ${item.content.map((section) => `
          <section>
            <h2>${escapeHtml(section.heading)}</h2>
            ${(section.paragraphs || []).map((paragraph) => `<p>${escapeHtml(paragraph)}</p>`).join('')}
            ${section.items ? `<ul>${section.items.map((entry) => `<li>${escapeHtml(entry)}</li>`).join('')}</ul>` : ''}
          </section>`).join('')}
      </div>`;
  }
})();
