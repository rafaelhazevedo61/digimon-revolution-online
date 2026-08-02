const CACHE_NAME = "dro-game-v12";
const STATIC_ASSETS = [
  "/",
  "/index.html",
  "/assets/css/game.css",
  "/assets/js/config.js",
  "/assets/js/api.js",
  "/assets/js/router.js",
  "/assets/js/ui.js",
  "/assets/js/auth.js",
  "/assets/js/starter.js",
  "/assets/js/dashboard.js",
  "/assets/js/missions.js"
];

self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener("activate", event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", event => {
  const url = new URL(event.request.url);

  // API calls and cross-origin: network only (don't cache)
  if (url.origin !== self.location.origin || url.pathname.startsWith("/assets") === false) {
    return;
  }

  // Static assets: network first, cache fallback (ensures fresh code)
  event.respondWith(
    fetch(event.request).then(response => {
      if (response.ok) {
        const clone = response.clone();
        caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
      }
      return response;
    }).catch(() => caches.match(event.request))
  );
});
