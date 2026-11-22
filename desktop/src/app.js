(function () {
  const CH = [
    { id: 1004, name: 'Sport TV', logo: 'sport.png' },
    { id: 1010, name: 'Futbol TV ⚽', emoji: '⚽' },
    { id: 1016, name: 'ZoʻrTV', logo: 'zor_tv.png' },
    { id: 1263, name: 'Setanta Sports 1', logo: 'setanta.png' },
    { id: 1264, name: 'Setanta Sports 2', logo: 'setanta.png' }
  ];
  const TG = 'https://t.me/ilhomvich_sh';
  const $ = (id) => document.getElementById(id);
  const ls = window.localStorage;
  const url = (c) => `https://${ls.getItem('server') || 'stream2'}.cinerama.uz/${c.id}/tracks-v1a1/mono.m3u8`;
  const active = () => ls.getItem('active') === '1' && (ls.getItem('name') || '').length > 0;

  function show(id) {
    document.querySelectorAll('.view').forEach((v) => v.classList.remove('on'));
    $(id).classList.add('on');
  }

  // ---- Aktivatsiya ----
  function lockLeft() {
    const l = Number(ls.getItem('lockUntil') || 0) - Date.now();
    return l > 0 ? Math.ceil(l / 1000) : 0;
  }
  function submit() {
    const name = $('name').value.trim();
    const code = $('code').value.trim();
    const err = $('aerr');
    if (lockLeft() > 0) { err.textContent = `Ko‘p xato urinish. ${lockLeft()} soniyadan keyin urining.`; return; }
    if (name.length < 2) { err.textContent = 'Foydalanuvchi nomini kiriting'; return; }
    if (!/^\d{6}$/.test(code)) { err.textContent = 'Kod 6 xonali bo‘lishi kerak'; return; }
    if (window.totp.verify(code)) {
      ls.setItem('active', '1');
      ls.setItem('name', name);
      ls.setItem('uid', Math.random().toString(16).slice(2, 10).toUpperCase());
      ls.setItem('at', String(Date.now()));
      ls.setItem('fails', '0');
      start();
    } else {
      let f = Number(ls.getItem('fails') || 0) + 1;
      if (f >= 5) { f = 0; ls.setItem('lockUntil', String(Date.now() + 60000)); }
      ls.setItem('fails', String(f));
      $('code').value = '';
      err.textContent = 'Kod noto‘g‘ri yoki eskirgan';
    }
  }
  $('go').onclick = submit;
  $('code').addEventListener('keydown', (e) => { if (e.key === 'Enter') submit(); });
  $('tg').onclick = () => window.open(TG);

  // ---- Ro'yxat ----
  function renderList() {
    const list = $('list');
    list.innerHTML = '';
    CH.forEach((c, i) => {
      const row = document.createElement('div');
      row.className = 'row';
      row.tabIndex = 0;
      const logo = document.createElement('div');
      logo.className = 'logo';
      const fallback = () => { logo.textContent = c.emoji || c.name.charAt(0); };
      if (c.logo) {
        const img = document.createElement('img');
        img.src = '../assets/logos/' + c.logo;
        img.onerror = () => { img.remove(); fallback(); };
        logo.appendChild(img);
      } else fallback();
      const info = document.createElement('div');
      info.className = 'info';
      const b = document.createElement('b'); b.textContent = c.name;
      const d = document.createElement('div'); d.textContent = 'ID: ' + c.id;
      info.append(b, d);
      const btn = document.createElement('button'); btn.textContent = 'PLAY';
      row.append(logo, info, btn);
      row.onclick = () => play(i);
      row.onkeydown = (e) => { if (e.key === 'Enter') play(i); };
      list.appendChild(row);
    });
    const cur = ls.getItem('server') || 'stream2';
    document.querySelectorAll('input[name=srv]').forEach((r) => {
      r.checked = r.value === cur;
      r.onchange = () => ls.setItem('server', r.value);
    });
  }
  document.querySelectorAll('[data-page]').forEach((b) => (b.onclick = () => page(b.dataset.page)));

  // ---- Player ----
  let hls = null, idx = 0, retryT = null, hideT = null;
  const video = $('video');
  function stop() {
    clearTimeout(retryT);
    if (hls) { hls.destroy(); hls = null; }
    video.removeAttribute('src'); video.load();
  }
  function play(i) {
    idx = (i + CH.length) % CH.length;
    const c = CH[idx];
    $('ptitle').textContent = c.name;
    $('perr').style.display = 'none';
    show('player');
    stop();
    const src = url(c);
    if (window.Hls && Hls.isSupported()) {
      hls = new Hls({ enableWorker: false, lowLatencyMode: true });
      hls.loadSource(src);
      hls.attachMedia(video);
      hls.on(Hls.Events.ERROR, (_, data) => {
        if (!data.fatal) return;
        if (data.type === Hls.ErrorTypes.MEDIA_ERROR) { hls.recoverMediaError(); return; }
        $('perr').style.display = 'block';
        $('perr').textContent = 'Kanal ochilmadi. Qayta uriniladi… Boshqa serverni tanlab ko‘ring.';
        clearTimeout(retryT);
        retryT = setTimeout(() => play(idx), 4000);
      });
      hls.on(Hls.Events.MANIFEST_PARSED, () => { $('perr').style.display = 'none'; video.play().catch(() => {}); });
    } else {
      video.src = src;
      video.play().catch(() => {});
    }
    poke();
  }
  function poke() {
    $('top').classList.remove('hide');
    clearTimeout(hideT);
    hideT = setTimeout(() => $('top').classList.add('hide'), 3000);
  }
  $('player').addEventListener('mousemove', poke);
  $('back').onclick = () => { stop(); show('main'); };
  $('prev').onclick = () => play(idx - 1);
  $('next').onclick = () => play(idx + 1);
  $('pip').onclick = async () => {
    try {
      if (document.pictureInPictureElement) await document.exitPictureInPicture();
      else await video.requestPictureInPicture();
    } catch (e) { /* qo'llab-quvvatlanmasa */ }
  };
  $('fs').onclick = () => {
    if (document.fullscreenElement) document.exitFullscreen();
    else $('player').requestFullscreen();
  };
  document.addEventListener('keydown', (e) => {
    if (!$('player').classList.contains('on')) return;
    if (e.key === 'ArrowUp' || e.key === 'PageUp') play(idx + 1);
    else if (e.key === 'ArrowDown' || e.key === 'PageDown') play(idx - 1);
    else if (e.key === 'f' || e.key === 'F') $('fs').click();
    else if (e.key === 'Escape') { if (!document.fullscreenElement) $('back').click(); }
  });

  // ---- Sahifalar ----
  function page(p) {
    const body = $('pb'), btns = $('pbtn');
    btns.innerHTML = '';
    const add = (t, fn) => { const b = document.createElement('button'); b.textContent = t; b.onclick = fn; btns.appendChild(b); };
    const at = ls.getItem('at') ? new Date(Number(ls.getItem('at'))).toLocaleString() : '-';
    if (p === 'subs') {
      $('pt').textContent = 'Mening obunalarim';
      body.textContent = `Reja: Private access\nHolat: Faol ✅\nFaollashtirilgan: ${at}\n\nKanallar:\n` +
        CH.map((c) => `• ${c.name}  (ID ${c.id})`).join('\n');
      add('Obunani yangilash / kod olish', () => window.open(TG));
    } else if (p === 'account') {
      $('pt').textContent = 'Akkaunt';
      body.textContent = `Foydalanuvchi: ${ls.getItem('name')}\nID: ${ls.getItem('uid')}\nFaollashtirilgan: ${at}\nServer: ${ls.getItem('server') || 'stream2'}`;
      add('Chiqish (qayta aktivatsiya kerak bo‘ladi)', () => {
        if (confirm('Akkauntdan chiqsangiz, yangi kod kerak bo‘ladi. Davom etasizmi?')) {
          ['active', 'name', 'uid', 'at'].forEach((k) => ls.removeItem(k));
          show('act');
        }
      });
    } else if (p === 'help') {
      $('pt').textContent = 'Yordam';
      body.textContent = '• Ilovadan foydalanish uchun 6 xonali aktivatsiya kodi kerak.\n• Kod Authenticator orqali vaqt asosida yaratiladi (30 soniya).\n• Kodni olish: Telegram t.me/ilhomvich_sh\n• Kanal ishlamasa, boshqa serverni (stream2 / stream8) tanlang.\n• Player: ⬆/⬇ kanal almashtiradi, F — to‘liq ekran, PiP — rasm ustida rasm.';
      add('Telegram: @ilhomvich_sh', () => window.open(TG));
    } else {
      $('pt').textContent = 'Haqida';
      body.textContent = 'IPTV Player v1.0.0\n\nby Shakhzodjon Khudaybediyev\n\nYopiq (private) ilova. Faqat aktivatsiya kodiga ega foydalanuvchilar uchun.';
      add('Telegram: @ilhomvich_sh', () => window.open(TG));
    }
    show('page');
  }
  $('pclose').onclick = () => show('main');

  function start() {
    if (!active()) { show('act'); return; }
    renderList();
    show('main');
  }
  start();
})();
