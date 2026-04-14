/**
 * StockPulse — dashboard.js
 * All section rendering, Chart.js charts, live price updates, buy/sell.
 * Works with real Spring Boot backend OR mock data fallback.
 */

/* ── Auth guard ─────────────────────────────────────────── */
API.requireAuth();
const U = API.getUser(); // current user object

/* ── State ──────────────────────────────────────────────── */
let activeSection = 'dashboard';
let priceChart    = null;
let selectedSym   = 'RELIANCE';
let orderMode     = 'buy';
let liveTimer     = null;
let searchTimer   = null;

// ── Live stock data (fetched from backend or mock) ────────
let liveStocks = [...MOCK.stocks];

/* ═══════════════════════════════════════════════════════════
   NAVIGATION
═══════════════════════════════════════════════════════════ */
function showSection(id, el) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.getElementById('page-' + id)?.classList.add('active');
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  if (el) el.classList.add('active');

  const TITLES = {
    dashboard:'Dashboard', trade:'Trade', portfolio:'Portfolio',
    watchlist:'Watchlist', stocks:'Stock Market', commodity:'Commodity',
    fno:'Futures & Options', ipo:'IPO', mf:'Mutual Funds',
    orders:'Order History', profile:'Profile & Settings'
  };
  document.getElementById('topbar-title').textContent = TITLES[id] || id;
  activeSection = id;

  // Render on demand
  const renders = {
    dashboard: renderDashboard, trade: renderTrade,
    portfolio: renderPortfolio, watchlist: renderWatchlist,
    stocks: renderStocks, fno: renderFnO,
    orders: renderOrders, profile: renderProfile,
  };
  renders[id]?.();
}

/* ═══════════════════════════════════════════════════════════
   DATA LOADER — tries backend, falls back to mock
═══════════════════════════════════════════════════════════ */
async function loadStocks() {
  try {
    const res = await API.getStocks();
    if (res?.data?.length) { liveStocks = res.data; return; }
  } catch (_) {}
  MOCK.tick();
  liveStocks = [...MOCK.stocks];
}

function getStock(sym) {
  return liveStocks.find(s => s.symbol === sym.toUpperCase()) || MOCK.getStock(sym);
}

/* ═══════════════════════════════════════════════════════════
   TICKER TAPE
═══════════════════════════════════════════════════════════ */
function buildTicker() {
  const track = document.getElementById('ticker-track');
  if (!track) return;
  const items = liveStocks.slice(0, 14).map(s => {
    const chg = s.changePercent ?? pct(s.price, s.previousClose);
    return `<div class="ticker-item">
      <span class="t-sym">${s.symbol}</span>
      <span class="t-price">${fmt(s.price)}</span>
      <span class="t-chg ${chg >= 0 ? 'up' : 'dn'}">${fmtPct(chg)}</span>
    </div>`;
  });
  track.innerHTML = [...items, ...items].join(''); // duplicate for seamless loop
}

/* ═══════════════════════════════════════════════════════════
   DASHBOARD
═══════════════════════════════════════════════════════════ */
function renderDashboard() {
  renderDashStats();
  renderMovers();
  renderIndices();
  renderAIRecs();
}

function renderDashStats() {
  const sum = MOCK.getPortfolioSummary(U);
  setText('dash-portval',  fmt(sum.totalCurrentValue, 0));
  setText('dash-pnl',      (sum.totalPnL >= 0 ? '+' : '') + fmt(Math.abs(sum.totalPnL), 0));
  setText('dash-pnlpct',   fmtPct(sum.totalPnLPercent));
  setText('dash-balance',  fmt(U.balance, 0));
  const el = document.getElementById('dash-pnl');
  if (el) el.style.color = sum.totalPnL >= 0 ? 'var(--green)' : 'var(--red)';
}

function renderMovers() {
  const el = document.getElementById('top-movers');
  if (!el) return;
  const sorted = [...liveStocks].sort((a, b) =>
    Math.abs(b.changePercent ?? 0) - Math.abs(a.changePercent ?? 0));
  el.innerHTML = sorted.slice(0, 8).map(s => `
    <div class="watch-item" onclick="openQuickTrade('${s.symbol}')">
      <div class="stock-cell" style="flex:1;">
        <div class="s-icon" style="background:${MOCK.getColor(s.sector)}22;color:${MOCK.getColor(s.sector)};width:30px;height:30px;">${s.iconCode}</div>
        <div><div class="s-name">${s.symbol}</div><div class="s-sub">${s.sector}</div></div>
      </div>
      <div style="text-align:right;">
        <div style="font-family:var(--font-mono);font-size:13px;font-weight:600;">${fmt(s.price)}</div>
        <div class="${s.changePercent >= 0 ? 'up' : 'dn'}" style="font-size:11px;">${fmtPct(s.changePercent ?? 0)}</div>
      </div>
    </div>`).join('');
}

function renderIndices() {
  const el = document.getElementById('indices-list');
  if (!el) return;
  const idxData = [
    { name:'NIFTY 50',     val:24141, chg:0.98 },
    { name:'SENSEX',       val:79408, chg:0.99 },
    { name:'BANK NIFTY',   val:51832, chg:0.63 },
    { name:'NIFTY IT',     val:38214, chg:-0.37 },
    { name:'NIFTY MIDCAP', val:53890, chg:1.23 },
    { name:'NIFTY FMCG',   val:55124, chg:0.18 },
  ];
  el.innerHTML = idxData.map(i => `
    <div class="idx-row">
      <div style="font-weight:600;">${i.name}</div>
      <div style="text-align:right;">
        <div style="font-family:var(--font-mono);font-weight:700;">${fmtNum(i.val)}</div>
        <div class="${i.chg >= 0 ? 'up' : 'dn'}" style="font-size:11px;">${fmtPct(i.chg)}</div>
      </div>
    </div>`).join('');
}

function renderAIRecs() {
  const recs = [
    { sym:'HDFCBANK', action:'BUY',  conf:92, reason:'Strong NII growth, bullish breakout above ₹1,720.' },
    { sym:'LT',       action:'BUY',  conf:88, reason:'Infra boom tailwind, record order book at all-time high.' },
    { sym:'MARUTI',   action:'HOLD', conf:74, reason:'Near resistance zone, wait for volume confirmation.' },
    { sym:'INFY',     action:'HOLD', conf:68, reason:'Global IT demand soft; support at ₹1,840 is key.' },
    { sym:'ZOMATO',   action:'SELL', conf:62, reason:'Near 52W high, valuation expensive at current levels.' },
  ];
  const el = document.getElementById('ai-recs');
  if (!el) return;
  el.innerHTML = recs.map(r => `
    <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:10px;padding:12px 14px;margin-bottom:8px;">
      <div class="flex-sb mb-12" style="margin-bottom:6px;">
        <div style="font-weight:700;font-size:13px;">${r.sym}</div>
        <span class="tag ${r.action==='BUY'?'tg':r.action==='HOLD'?'ta':'tr'}">${r.action}</span>
      </div>
      <div style="font-size:12px;color:var(--text-2);margin-bottom:8px;">${r.reason}</div>
      <div style="display:flex;align-items:center;gap:8px;">
        <div style="flex:1;" class="prog-track"><div class="prog-fill" style="width:${r.conf}%;background:${r.action==='BUY'?'var(--green)':r.action==='HOLD'?'var(--amber)':'var(--red)'}"></div></div>
        <span style="font-size:11px;font-weight:700;color:var(--green);font-family:var(--font-mono);">${r.conf}%</span>
      </div>
    </div>`).join('');
}

/* ═══════════════════════════════════════════════════════════
   TRADE PAGE
═══════════════════════════════════════════════════════════ */
function renderTrade() {
  const sel = document.getElementById('stock-selector');
  if (sel && !sel.options.length) {
    sel.innerHTML = liveStocks.map(s =>
      `<option value="${s.symbol}">${s.symbol} — ${s.companyName}</option>`).join('');
    sel.value = selectedSym;
    sel.onchange = () => { selectedSym = sel.value; loadTradeStock(); };
  }
  loadTradeStock();
}

function loadTradeStock() {
  const s = getStock(selectedSym);
  if (!s) return;
  const chg = s.changePercent ?? pct(s.price, s.previousClose);
  setText('trade-sym',     s.symbol);
  setText('trade-company', s.companyName);
  setText('trade-price',   fmt(s.price));
  setText('trade-chg',     `${chg >= 0 ? '▲' : '▼'} ${fmtPct(chg)}`);
  const chgEl = document.getElementById('trade-chg');
  if (chgEl) chgEl.className = chg >= 0 ? 'up' : 'dn';
  renderPriceChart(s);
  renderAIInsight(s);
  calcOrder();
}

function renderPriceChart(s) {
  const ctx = document.getElementById('trade-chart');
  if (!ctx) return;
  if (priceChart) priceChart.destroy();

  const pts    = MOCK.getHistory(s.symbol);
  const prices = pts.map(p => p.price);
  const labels = pts.map((p, i) => {
    const d = new Date(p.recordedAt);
    return d.toLocaleTimeString('en-IN', { hour:'2-digit', minute:'2-digit' });
  });
  const isUp  = s.price >= s.previousClose;
  const color = isUp ? '#00d09c' : '#ff4d4d';

  priceChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        data: prices, borderColor: color, backgroundColor: `${color}18`,
        borderWidth: 2, pointRadius: 0, fill: true, tension: 0.4,
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      animation: { duration: 500 },
      plugins: {
        legend: { display: false },
        tooltip: { mode:'index', intersect:false, callbacks: { label: c => '  ₹' + (+c.raw).toLocaleString() } }
      },
      scales: {
        x: { grid:{ color:'rgba(255,255,255,.04)' }, ticks:{ color:'#4b5e78', maxTicksLimit:8, font:{size:10,family:'JetBrains Mono'} } },
        y: { grid:{ color:'rgba(255,255,255,.04)' }, ticks:{ color:'#4b5e78', font:{size:10,family:'JetBrains Mono'}, callback: v => '₹' + v.toLocaleString() } }
      }
    }
  });
}

function renderAIInsight(s) {
  const insights = {
    RELIANCE:'Strong Q3 expected. Jio growth accelerating. Retail expansion on track. <strong style="color:var(--green)">Target: ₹3,100</strong>. Risk: Medium.',
    TCS:'Global IT soft. Q1 guidance muted. Wait for support at ₹3,800 before fresh entry.',
    HDFCBANK:'Post-merger integration ahead of schedule. NIM improving. <strong style="color:var(--green)">Target: ₹1,950</strong>.',
    LT:'Record order book. Govt infra push strong. <strong style="color:var(--green)">Target: ₹4,000</strong>. Risk: Low.',
    INFY:'Large deals positive but margin pressure. Support at ₹1,840. Wait for breakout.',
  };
  const text = insights[s.symbol] || 'Monitoring price action. No strong signal at current levels. Watch key support and resistance zones.';
  const chg  = s.changePercent ?? 0;
  const sig  = chg > 1.5 ? 'BUY' : chg < -1.5 ? 'SELL' : 'HOLD';
  const sigColor = sig === 'BUY' ? 'var(--green)' : sig === 'SELL' ? 'var(--red)' : 'var(--amber)';
  const el   = document.getElementById('ai-insight-box');
  if (el) el.innerHTML = `<div class="ai-hdr">🤖 AI Signal: <span style="color:${sigColor};">${sig}</span></div><div class="ai-body">${text}</div>`;
}

function setOrderMode(mode) {
  orderMode = mode;
  document.getElementById('o-tab-buy').classList.toggle('active',  mode === 'buy');
  document.getElementById('o-tab-sell').classList.toggle('active', mode === 'sell');
  const btn = document.getElementById('place-btn');
  if (btn) btn.className = `place-btn ${mode}`;
  calcOrder();
}

function calcOrder() {
  const s   = getStock(selectedSym);
  if (!s) return;
  const qty = parseInt(document.getElementById('ord-qty')?.value) || 1;
  const tot = qty * s.price;
  const stt = +(tot * 0.001).toFixed(2);
  setText('ord-price-val', fmt(s.price));
  setText('ord-qty-val',   qty);
  setText('ord-stt',       fmt(stt));
  setText('ord-total',     fmt(tot + stt));
  const btn = document.getElementById('place-btn');
  if (btn) btn.textContent = `${orderMode.toUpperCase()} ${selectedSym} — ${fmt(tot + stt, 0)}`;
  const warn = document.getElementById('ord-warn');
  if (warn) warn.style.display = tot > U.balance * 0.3 ? 'block' : 'none';
}

async function placeOrder() {
  const qty = parseInt(document.getElementById('ord-qty')?.value) || 1;
  if (qty < 1) { toast('Enter a valid quantity', 'error'); return; }

  try {
    // Try real backend
    const fn  = orderMode === 'buy' ? API.buyStock : API.sellStock;
    const res = await fn(U.id, selectedSym, qty);
    if (res?.data) {
      U.balance = res.data.remainingBalance;
      localStorage.setItem('sp_user', JSON.stringify(U));
      setText('balance-display', fmt(U.balance, 0));
      toast(`✅ ${orderMode.toUpperCase()} ${qty} × ${selectedSym} executed!`, 'success');
      calcOrder();
      return;
    }
  } catch (_) {}

  // Fallback: mock
  const res = MOCK.placeOrder(orderMode.toUpperCase(), selectedSym, qty, U);
  if (!res.ok) { toast(res.message, 'error'); return; }
  setText('balance-display', fmt(U.balance, 0));
  toast(`✅ ${orderMode.toUpperCase()} ${qty} × ${selectedSym} @ ${fmt(res.order.price)}`, 'success');
  calcOrder();
}

/* ═══════════════════════════════════════════════════════════
   STOCKS PAGE
═══════════════════════════════════════════════════════════ */
function renderStocks(filter = 'all') {
  const cards = document.getElementById('idx-cards');
  if (cards) cards.innerHTML = [
    { n:'NIFTY 50',val:24141,c:0.98 },{ n:'SENSEX',val:79408,c:0.99 },
    { n:'BANK NIFTY',val:51832,c:0.63 },{ n:'NIFTY IT',val:38214,c:-0.37 },
  ].map(i => `<div class="stat-card ${i.c>=0?'green':'red'}">
    <div class="stat-lbl">${i.n}</div>
    <div class="stat-val" style="font-size:22px;">${fmtNum(i.val)}</div>
    <div class="stat-chg ${i.c>=0?'up':'dn'}">${i.c>=0?'▲':'▼'} ${fmtPct(Math.abs(i.c))}</div>
  </div>`).join('');

  let list = [...liveStocks];
  if (filter === 'gainers') list = list.filter(s => (s.changePercent??0) >= 0).sort((a,b)=>(b.changePercent??0)-(a.changePercent??0));
  if (filter === 'losers')  list = list.filter(s => (s.changePercent??0) < 0).sort((a,b)=>(a.changePercent??0)-(b.changePercent??0));

  const tbody = document.getElementById('stocks-tbody');
  if (!tbody) return;
  tbody.innerHTML = list.map(s => {
    const chg = s.changePercent ?? 0;
    const col = MOCK.getColor(s.sector);
    return `<tr onclick="openQuickTrade('${s.symbol}')">
      <td><div class="stock-cell">
        <div class="s-icon" style="background:${col}22;color:${col};">${s.iconCode}</div>
        <div><div class="s-name">${s.symbol}</div><div class="s-sub">${s.companyName}</div></div>
      </div></td>
      <td><span class="tag tb" style="font-size:10px;">${s.sector}</span></td>
      <td class="${chg>=0?'up':'dn'}">${fmt(s.price)}</td>
      <td class="${chg>=0?'up':'dn'}">${fmtPct(chg)}</td>
      <td class="neu">${s.marketCap||'—'}</td>
      <td><div style="display:flex;gap:6px;">
        <button class="btn btn-g btn-sm" onclick="event.stopPropagation();openQuickTrade('${s.symbol}','buy')">Buy</button>
        <button class="btn btn-ghost btn-sm" onclick="event.stopPropagation();toggleWatch('${s.symbol}')">★</button>
      </div></td>
    </tr>`;
  }).join('');
}

function filterStocks(el, type) {
  document.querySelectorAll('.pills .pill').forEach(p => p.classList.remove('active'));
  el.classList.add('active');
  renderStocks(type);
}

/* ═══════════════════════════════════════════════════════════
   PORTFOLIO
═══════════════════════════════════════════════════════════ */
function renderPortfolio() {
  const sum = MOCK.getPortfolioSummary(U);

  setText('port-invested',  fmt(sum.totalInvested, 0));
  setText('port-current',   fmt(sum.totalCurrentValue, 0));
  const pnlEl = document.getElementById('port-pnl');
  if (pnlEl) { pnlEl.textContent = (sum.totalPnL>=0?'+':'')+fmt(Math.abs(sum.totalPnL),0); pnlEl.style.color=sum.totalPnL>=0?'var(--green)':'var(--red)'; }
  setText('port-pnlpct',   fmtPct(sum.totalPnLPercent));

  // Sector allocation
  const secMap = {};
  sum.holdings.forEach(h => { secMap[h.sector] = (secMap[h.sector]||0) + h.currentValue; });
  const total = Object.values(secMap).reduce((a,b)=>a+b,0);
  const cols  = ['#00d09c','#4f8ef7','#9b6dff','#f5a623','#ff4d4d','#22d3ee'];
  const secEl = document.getElementById('sector-alloc');
  if (secEl) secEl.innerHTML = Object.entries(secMap).map(([sec,val],i) => {
    const p = total ? (val/total*100).toFixed(1) : 0;
    return `<div style="margin-bottom:10px;">
      <div class="flex-sb" style="font-size:12px;margin-bottom:4px;"><span>${sec}</span><span style="font-family:var(--font-mono);color:${cols[i%cols.length]}">${p}%</span></div>
      <div class="prog-track"><div class="prog-fill" style="width:${p}%;background:${cols[i%cols.length]}"></div></div>
    </div>`;
  }).join('');

  // Holdings table
  const tbody = document.getElementById('holdings-tbody');
  if (!tbody) return;
  tbody.innerHTML = sum.holdings.map(h => {
    const col = MOCK.getColor(h.sector);
    return `<tr>
      <td><div class="stock-cell">
        <div class="s-icon" style="background:${col}22;color:${col};width:28px;height:28px;font-size:11px;">${h.iconCode}</div>
        <div><div class="s-name">${h.symbol}</div><div class="s-sub">${h.sector}</div></div>
      </div></td>
      <td class="neu">${h.quantity}</td>
      <td class="neu">${fmt(h.averagePrice)}</td>
      <td class="neu" style="font-weight:700;">${fmt(h.currentPrice)}</td>
      <td class="${h.unrealizedPnL>=0?'up':'dn'}">${h.unrealizedPnL>=0?'+':''}${fmt(Math.abs(h.unrealizedPnL),0)}</td>
      <td><span class="tag ${h.unrealizedPnLPct>=0?'tg':'tr'}">${h.unrealizedPnLPct>=0?'▲':'▼'} ${Math.abs(h.unrealizedPnLPct).toFixed(1)}%</span></td>
      <td class="neu">${fmt(h.currentValue,0)}</td>
      <td><button class="btn btn-r btn-sm" onclick="openQuickTrade('${h.symbol}','sell')">Sell</button></td>
    </tr>`;
  }).join('');
}

/* ═══════════════════════════════════════════════════════════
   WATCHLIST
═══════════════════════════════════════════════════════════ */
function renderWatchlist() {
  const tbody = document.getElementById('wl-tbody');
  if (!tbody) return;
  tbody.innerHTML = MOCK.watchlist.map(sym => {
    const s = getStock(sym);
    if (!s) return '';
    const chg = s.changePercent ?? 0;
    const col = MOCK.getColor(s.sector);
    return `<tr>
      <td><div class="stock-cell">
        <div class="s-icon" style="background:${col}22;color:${col};width:30px;height:30px;font-size:11px;">${s.iconCode}</div>
        <div><div class="s-name">${s.symbol}</div><div class="s-sub">${s.companyName}</div></div>
      </div></td>
      <td class="${chg>=0?'up':'dn'}" style="font-weight:700;">${fmt(s.price)}</td>
      <td class="${chg>=0?'up':'dn'}">${fmtPct(chg)}</td>
      <td class="neu">${fmt(s.previousClose)}</td>
      <td><span class="tag ${chg>1?'tg':chg<-1?'tr':'ta'}">${chg>1?'BUY':chg<-1?'SELL':'HOLD'}</span></td>
      <td><div style="display:flex;gap:6px;">
        <button class="btn btn-g btn-sm" onclick="openQuickTrade('${sym}','buy')">Buy</button>
        <button class="btn btn-ghost btn-sm" onclick="removeWatch('${sym}')">✕</button>
      </div></td>
    </tr>`;
  }).join('');
  setText('wl-badge', MOCK.watchlist.length);
}

async function toggleWatch(sym) {
  const idx = MOCK.watchlist.indexOf(sym);
  if (idx >= 0) {
    MOCK.watchlist.splice(idx, 1);
    toast(`Removed ${sym} from watchlist`, 'info');
    try { await API.removeWatchlist(U.id, sym); } catch(_){}
  } else {
    MOCK.watchlist.push(sym);
    toast(`★ Added ${sym} to watchlist`, 'success');
    try { await API.addWatchlist(U.id, sym); } catch(_){}
  }
  setText('wl-badge', MOCK.watchlist.length);
  if (activeSection === 'watchlist') renderWatchlist();
}
function removeWatch(sym) { toggleWatch(sym); }

/* ═══════════════════════════════════════════════════════════
   F&O PAGE (static data — extend with real F&O API later)
═══════════════════════════════════════════════════════════ */
function renderFnO() {
  const opts = [
    { strike:24000, cOI:'4.2L',cVol:'2.1L',cIV:18.4,cLTP:142, pLTP:58,  pIV:19.2,pVol:'0.8L',pOI:'3.4L',atm:false },
    { strike:24050, cOI:'2.8L',cVol:'1.4L',cIV:17.9,cLTP:108, pLTP:74,  pIV:18.6,pVol:'1.0L',pOI:'2.6L',atm:false },
    { strike:24100, cOI:'5.1L',cVol:'2.8L',cIV:17.5,cLTP:78,  pLTP:98,  pIV:18.1,pVol:'1.6L',pOI:'4.2L',atm:true  },
    { strike:24150, cOI:'1.9L',cVol:'0.9L',cIV:17.9,cLTP:52,  pLTP:126, pIV:18.8,pVol:'0.7L',pOI:'1.8L',atm:false },
    { strike:24200, cOI:'1.4L',cVol:'0.6L',cIV:18.6,cLTP:32,  pLTP:158, pIV:19.6,pVol:'0.5L',pOI:'1.3L',atm:false },
    { strike:24250, cOI:'0.9L',cVol:'0.4L',cIV:19.4,cLTP:18,  pLTP:194, pIV:20.6,pVol:'0.3L',pOI:'0.8L',atm:false },
  ];
  const tbody = document.getElementById('options-tbody');
  if (tbody) tbody.innerHTML = opts.map(o => `
    <tr style="${o.atm?'background:rgba(0,208,156,.06);':''}">
      <td class="neu">${o.cOI}</td><td class="neu">${o.cVol}</td>
      <td class="neu">${o.cIV}%</td>
      <td class="up" style="font-weight:700;">${o.cLTP}</td>
      <td style="text-align:center;font-family:var(--font-head);font-weight:700;background:rgba(255,255,255,.04);">
        ${o.strike.toLocaleString()}${o.atm?'<br><span style="font-size:9px;color:var(--green);">ATM</span>':''}
      </td>
      <td class="dn" style="font-weight:700;">${o.pLTP}</td>
      <td class="neu">${o.pIV}%</td>
      <td class="neu">${o.pVol}</td><td class="neu">${o.pOI}</td>
    </tr>`).join('');
}

/* ═══════════════════════════════════════════════════════════
   ORDERS
═══════════════════════════════════════════════════════════ */
async function renderOrders() {
  const tbody = document.getElementById('orders-tbody');
  if (!tbody) return;
  let data = MOCK.orders;
  try {
    const res = await API.getOrders(U.id);
    if (res?.data?.length) data = res.data;
  } catch(_){}

  if (!data.length) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:40px;">No orders yet. Go to Trade section to start!</td></tr>';
    return;
  }
  tbody.innerHTML = data.map(o => `
    <tr>
      <td class="neu" style="font-size:11px;">#${String(o.id).slice(-6)}</td>
      <td style="font-weight:700;">${o.symbol}</td>
      <td><span class="tag ${o.type==='BUY'?'tg':'tr'}">${o.type}</span></td>
      <td class="neu">${o.quantity}</td>
      <td class="neu" style="font-weight:600;">${fmt(o.price)}</td>
      <td class="neu">${fmt(+(o.price * o.quantity).toFixed(2), 0)}</td>
      <td style="font-size:11px;color:var(--text-muted);">${new Date(o.timestamp).toLocaleString('en-IN')}</td>
    </tr>`).join('');
}

/* ═══════════════════════════════════════════════════════════
   PROFILE
═══════════════════════════════════════════════════════════ */
function renderProfile() {
  setText('prof-name',    U.name || '—');
  setText('prof-email',   U.email || '—');
  setText('prof-balance', fmt(U.balance, 0));
  const inp = document.getElementById('prof-name-inp');
  if (inp) inp.value = U.name || '';
  const av = document.getElementById('prof-avatar');
  if (av) av.textContent = (U.name||'U').split(' ').map(w=>w[0]).join('').toUpperCase().slice(0,2);
}

function saveProfile() {
  const n = document.getElementById('prof-name-inp')?.value.trim();
  if (n) { U.name = n; localStorage.setItem('sp_user', JSON.stringify(U)); setText('prof-name',n); toast('Profile updated!','success'); }
}

/* ═══════════════════════════════════════════════════════════
   QUICK TRADE MODAL
═══════════════════════════════════════════════════════════ */
function openQuickTrade(sym, mode='buy') {
  selectedSym = sym; orderMode = mode;
  const s = getStock(sym);
  if (!s) return;
  const chg = s.changePercent ?? 0;
  setText('qt-sym',     sym);
  setText('qt-company', s.companyName);
  setText('qt-price',   fmt(s.price));
  setText('qt-chg',     fmtPct(chg));
  document.getElementById('qt-chg').className = chg >= 0 ? 'up' : 'dn';
  document.getElementById('qt-tab-buy').classList.toggle('active',  mode==='buy');
  document.getElementById('qt-tab-sell').classList.toggle('active', mode==='sell');
  document.getElementById('qt-qty').value = 1;
  calcQtOrder();
  openModal('modal-trade');
}

function setQtMode(m) {
  orderMode = m;
  document.getElementById('qt-tab-buy').classList.toggle('active',  m==='buy');
  document.getElementById('qt-tab-sell').classList.toggle('active', m==='sell');
  calcQtOrder();
}

function calcQtOrder() {
  const s   = getStock(selectedSym);
  const qty = parseInt(document.getElementById('qt-qty')?.value) || 1;
  const tot = s ? qty * s.price : 0;
  setText('qt-total', fmt(tot, 0));
  const btn = document.getElementById('qt-place-btn');
  if (btn) { btn.className=`place-btn ${orderMode}`; btn.textContent=`${orderMode.toUpperCase()} ${qty} × ${selectedSym} — ${fmt(tot,0)}`; }
}

function placeQtOrder() {
  const qty = parseInt(document.getElementById('qt-qty')?.value) || 1;
  const res = MOCK.placeOrder(orderMode.toUpperCase(), selectedSym, qty, U);
  if (!res.ok) { toast(res.message, 'error'); return; }
  setText('balance-display', fmt(U.balance, 0));
  toast(`✅ ${orderMode.toUpperCase()} ${qty} × ${selectedSym} @ ${fmt(res.order.price)}`, 'success');
  closeModal('modal-trade');
}

/* ═══════════════════════════════════════════════════════════
   SEARCH
═══════════════════════════════════════════════════════════ */
function handleSearch(q) {
  clearTimeout(searchTimer);
  const dd = document.getElementById('search-dd');
  if (!q.trim()) { if (dd) dd.style.display='none'; return; }
  searchTimer = setTimeout(() => {
    const res = liveStocks.filter(s =>
      s.symbol.includes(q.toUpperCase()) || s.companyName.toLowerCase().includes(q.toLowerCase())
    ).slice(0, 6);
    if (!dd) return;
    dd.innerHTML = res.length ? res.map(s => {
      const chg = s.changePercent ?? 0;
      const col = MOCK.getColor(s.sector);
      return `<div class="watch-item" onclick="openQuickTrade('${s.symbol}');document.getElementById('search-dd').style.display='none';document.getElementById('search-inp').value='';">
        <div class="stock-cell" style="flex:1;">
          <div class="s-icon" style="background:${col}22;color:${col};width:28px;height:28px;font-size:11px;">${s.iconCode}</div>
          <div><div class="s-name">${s.symbol}</div><div class="s-sub">${s.companyName}</div></div>
        </div>
        <span class="tag ${chg>=0?'tg':'tr'}">${fmtPct(chg)}</span>
      </div>`;
    }).join('') : '<div style="padding:14px;color:var(--text-muted);font-size:13px;">No stocks found</div>';
    dd.style.display = 'block';
  }, 220);
}

/* ═══════════════════════════════════════════════════════════
   HELPERS
═══════════════════════════════════════════════════════════ */
function setText(id, val) { const el=document.getElementById(id); if(el) el.textContent=val; }
function openModal(id)    { const m=document.getElementById(id); if(m) m.classList.add('open'); }
function closeModal(id)   { const m=document.getElementById(id); if(m) m.classList.remove('open'); }

/* ═══════════════════════════════════════════════════════════
   LIVE UPDATE LOOP
═══════════════════════════════════════════════════════════ */
async function startLivePrices() {
  await loadStocks();
  buildTicker();

  liveTimer = setInterval(async () => {
    await loadStocks();
    buildTicker();
    if (activeSection === 'dashboard')  { renderMovers(); renderDashStats(); }
    if (activeSection === 'trade')      loadTradeStock();
    if (activeSection === 'portfolio')  renderPortfolio();
    if (activeSection === 'watchlist')  renderWatchlist();
    if (activeSection === 'stocks')     renderStocks();
  }, 5000);
}

/* ═══════════════════════════════════════════════════════════
   INIT
═══════════════════════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
  // Set user info
  setText('user-name-display', U.name || 'User');
  setText('balance-display',   fmt(U.balance, 0));
  const av = document.getElementById('user-avatar');
  if (av) av.textContent = (U.name||'U').split(' ').map(w=>w[0]).join('').toUpperCase().slice(0,2);

  // Close search on outside click
  document.addEventListener('click', e => {
    if (!e.target.closest('.search-wrap')) {
      const dd = document.getElementById('search-dd');
      if (dd) dd.style.display='none';
    }
  });

  // Close modal on backdrop click
  document.querySelectorAll('.modal-overlay').forEach(m => {
    m.addEventListener('click', e => { if (e.target === m) m.classList.remove('open'); });
  });

  showSection('dashboard', document.querySelector('[data-sec="dashboard"]'));
  startLivePrices();
});
