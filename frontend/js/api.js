/**
 * StockPulse — api.js
 * ─────────────────────────────────────────────────────────────
 * Central API client. Connects to Spring Boot at localhost:8080.
 * Falls back to mock data if backend is not running.
 *
 * TO USE WITH REAL BACKEND: ensure Spring Boot is running on :8080
 * ─────────────────────────────────────────────────────────────
 */

const API = (() => {

  const BASE = 'http://localhost:8080/api';

  // ── Session helpers ─────────────────────────────────────
  const getToken  = () => localStorage.getItem('sp_token') || '';
  const getUser   = () => { try { return JSON.parse(localStorage.getItem('sp_user') || '{}'); } catch { return {}; } };
  const saveSession = (data) => {
    localStorage.setItem('sp_token', data.token || 'local');
    localStorage.setItem('sp_user',  JSON.stringify(data));
  };
  const clearSession = () => {
    localStorage.removeItem('sp_token');
    localStorage.removeItem('sp_user');
  };
  const requireAuth = () => { if (!getToken()) location.href = 'login.html'; };
  const logout = () => { clearSession(); location.href = 'login.html'; };


  // ── Generic fetch ────────────────────────────────────────
  async function req(method, path, body = null) {
    const headers = { 'Content-Type': 'application/json' };
    if (getToken()) headers['Authorization'] = `Bearer ${getToken()}`;
    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(BASE + path, opts);
    const data = await res.json();
    if (res.status === 401) { clearSession(); location.href = 'login.html'; }
    if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`);
    return data;
  }

  // ── Auth ─────────────────────────────────────────────────
  const login  = (email, password) => req('POST', '/auth/login',  { email, password });
  const signup = (payload)         => req('POST', '/auth/signup',  payload);

  // ── Stocks ───────────────────────────────────────────────
  const getStocks       = ()      => req('GET', '/stocks');
  const getStock        = (sym)   => req('GET', `/stocks/${sym}`);
  const searchStocks    = (q)     => req('GET', `/stocks/search?q=${encodeURIComponent(q)}`);
  const getStockHistory = (sym)   => req('GET', `/stocks/${sym}/history`);
  const getGainers      = ()      => req('GET', '/stocks/gainers');
  const getLosers       = ()      => req('GET', '/stocks/losers');

  // ── Orders ───────────────────────────────────────────────
  const buyStock  = (userId, symbol, quantity) => req('POST', '/orders/buy',  { userId, symbol, quantity });
  const sellStock = (userId, symbol, quantity) => req('POST', '/orders/sell', { userId, symbol, quantity });
  const getOrders = (userId)                   => req('GET',  `/orders/user/${userId}`);

  // ── Portfolio ────────────────────────────────────────────
  const getPortfolio = (userId) => req('GET', `/portfolio/${userId}`);

  // ── Watchlist ────────────────────────────────────────────
  const getWatchlist    = (userId)         => req('GET',    `/watchlist/${userId}`);
  const addWatchlist    = (userId, symbol) => req('POST',   '/watchlist/add', { userId, symbol });
  const removeWatchlist = (userId, symbol) => req('DELETE', `/watchlist/${userId}/${symbol}`);

  // ── Admin ────────────────────────────────────────────────
  const adminUsers = () => req('GET', '/admin/users');
  const adminStats = () => req('GET', '/admin/stats');

  return {
    getToken, getUser, saveSession, clearSession, requireAuth, logout,
    login, signup,
    getStocks, getStock, searchStocks, getStockHistory, getGainers, getLosers,
    buyStock, sellStock, getOrders,
    getPortfolio,
    getWatchlist, addWatchlist, removeWatchlist,
    adminUsers, adminStats,
  };
})();

// ═══════════════════════════════════════════════════════════
//  MOCK DATA ENGINE — used when backend is offline
// ═══════════════════════════════════════════════════════════
const MOCK = (() => {

  const rand = (min, max) => Math.random() * (max - min) + min;

  const stocks = [
    { id:1,  symbol:'RELIANCE',   companyName:'Reliance Industries Ltd',    price:2847.50, previousClose:2799.20, sector:'Oil & Gas',  marketCap:'₹19.2L Cr', exchange:'NSE', iconCode:'RL', changePercent:1.72 },
    { id:2,  symbol:'TCS',        companyName:'Tata Consultancy Services',  price:3982.10, previousClose:4015.40, sector:'IT',         marketCap:'₹14.5L Cr', exchange:'NSE', iconCode:'TC', changePercent:-0.83},
    { id:3,  symbol:'HDFCBANK',   companyName:'HDFC Bank Ltd',              price:1748.35, previousClose:1713.80, sector:'Banking',    marketCap:'₹13.3L Cr', exchange:'NSE', iconCode:'HD', changePercent:2.14 },
    { id:4,  symbol:'INFY',       companyName:'Infosys Ltd',                price:1892.45, previousClose:1916.10, sector:'IT',         marketCap:'₹7.9L Cr',  exchange:'NSE', iconCode:'IN', changePercent:-1.24},
    { id:5,  symbol:'ICICIBANK',  companyName:'ICICI Bank Ltd',             price:1284.70, previousClose:1260.50, sector:'Banking',    marketCap:'₹9.1L Cr',  exchange:'NSE', iconCode:'IC', changePercent:1.89 },
    { id:6,  symbol:'WIPRO',      companyName:'Wipro Ltd',                   price:547.25, previousClose:545.00,  sector:'IT',         marketCap:'₹2.9L Cr',  exchange:'NSE', iconCode:'WI', changePercent:0.42 },
    { id:7,  symbol:'LT',         companyName:'Larsen & Toubro Ltd',        price:3614.80, previousClose:3519.60, sector:'Infra',      marketCap:'₹5.1L Cr',  exchange:'NSE', iconCode:'LT', changePercent:2.67 },
    { id:8,  symbol:'MARUTI',     companyName:'Maruti Suzuki India Ltd',   price:12842.00, previousClose:12884.50,sector:'Auto',       marketCap:'₹3.9L Cr',  exchange:'NSE', iconCode:'MS', changePercent:-0.33},
    { id:9,  symbol:'BAJFINANCE', companyName:'Bajaj Finance Ltd',          price:7248.00, previousClose:7166.30, sector:'NBFC',       marketCap:'₹4.4L Cr',  exchange:'NSE', iconCode:'BF', changePercent:1.12 },
    { id:10, symbol:'SUNPHARMA',  companyName:'Sun Pharmaceutical Ind',     price:1847.00, previousClose:1832.40, sector:'Pharma',     marketCap:'₹4.4L Cr',  exchange:'NSE', iconCode:'SP', changePercent:0.84 },
    { id:11, symbol:'TATAMOTORS', companyName:'Tata Motors Ltd',             price:984.30, previousClose:960.45,  sector:'Auto',       marketCap:'₹3.6L Cr',  exchange:'NSE', iconCode:'TM', changePercent:2.47 },
    { id:12, symbol:'AXISBANK',   companyName:'Axis Bank Ltd',              price:1142.60, previousClose:1129.80, sector:'Banking',    marketCap:'₹3.5L Cr',  exchange:'NSE', iconCode:'AX', changePercent:1.12 },
    { id:13, symbol:'ONGC',       companyName:'Oil & Natural Gas Corp',      price:287.40, previousClose:289.60,  sector:'Oil & Gas',  marketCap:'₹3.6L Cr',  exchange:'NSE', iconCode:'ON', changePercent:-0.76},
    { id:14, symbol:'NTPC',       companyName:'NTPC Ltd',                    price:384.20, previousClose:381.00,  sector:'Power',      marketCap:'₹3.7L Cr',  exchange:'NSE', iconCode:'NT', changePercent:0.84 },
    { id:15, symbol:'COALINDIA',  companyName:'Coal India Ltd',              price:478.60, previousClose:472.80,  sector:'Mining',     marketCap:'₹2.9L Cr',  exchange:'NSE', iconCode:'CI', changePercent:1.22 },
    { id:16, symbol:'HINDUNILVR', companyName:'Hindustan Unilever Ltd',     price:2284.50, previousClose:2291.20, sector:'FMCG',       marketCap:'₹5.4L Cr',  exchange:'NSE', iconCode:'HU', changePercent:-0.29},
    { id:17, symbol:'TITAN',      companyName:'Titan Company Ltd',          price:3842.00, previousClose:3806.50, sector:'Consumer',   marketCap:'₹3.4L Cr',  exchange:'NSE', iconCode:'TI', changePercent:0.93 },
    { id:18, symbol:'ITC',        companyName:'ITC Ltd',                     price:482.15, previousClose:479.40,  sector:'FMCG',       marketCap:'₹6.0L Cr',  exchange:'NSE', iconCode:'IT', changePercent:0.57 },
    { id:19, symbol:'SBILIFE',    companyName:'SBI Life Insurance Co',      price:1687.00, previousClose:1672.50, sector:'Insurance',  marketCap:'₹1.7L Cr',  exchange:'NSE', iconCode:'SL', changePercent:0.87 },
    { id:20, symbol:'ADANIGREEN', companyName:'Adani Green Energy Ltd',     price:1847.30, previousClose:1794.60, sector:'Renewable',  marketCap:'₹2.9L Cr',  exchange:'NSE', iconCode:'AG', changePercent:2.93 },
    { id:21, symbol:'POWERGRID',  companyName:'Power Grid Corp of India',    price:312.40, previousClose:309.80,  sector:'Power',      marketCap:'₹2.9L Cr',  exchange:'NSE', iconCode:'PG', changePercent:0.84 },
    { id:22, symbol:'NESTLEIND',  companyName:'Nestle India Ltd',           price:2487.00, previousClose:2494.30, sector:'FMCG',       marketCap:'₹2.4L Cr',  exchange:'NSE', iconCode:'NE', changePercent:-0.29},
    { id:23, symbol:'ZOMATO',     companyName:'Zomato Ltd',                  price:248.90, previousClose:252.40,  sector:'Tech',       marketCap:'₹2.2L Cr',  exchange:'NSE', iconCode:'ZO', changePercent:-1.39},
    { id:24, symbol:'BAJAJFINSV', companyName:'Bajaj Finserv Ltd',          price:1684.00, previousClose:1668.40, sector:'NBFC',       marketCap:'₹2.7L Cr',  exchange:'NSE', iconCode:'BS', changePercent:0.93 },
    { id:25, symbol:'HCLTECH',    companyName:'HCL Technologies Ltd',       price:1482.60, previousClose:1494.20, sector:'IT',         marketCap:'₹4.0L Cr',  exchange:'NSE', iconCode:'HC', changePercent:-0.78},
  ];

  const SECTOR_COLORS = {
    'IT':'#4f8ef7','Banking':'#9b6dff','Oil & Gas':'#00d09c','Infra':'#22d3ee',
    'Auto':'#f5a623','NBFC':'#ff4d4d','Pharma':'#00d09c','FMCG':'#84cc16',
    'Power':'#f59e0b','Mining':'#6b7280','Consumer':'#ec4899','Tech':'#8b5cf6',
    'Insurance':'#06b6d4','Renewable':'#22c55e',
  };
  const getColor = (sector) => SECTOR_COLORS[sector] || '#4f8ef7';

  let portfolio  = [
    { symbol:'RELIANCE',  quantity:20, averagePrice:2640 },
    { symbol:'TCS',       quantity:8,  averagePrice:3820 },
    { symbol:'HDFCBANK',  quantity:30, averagePrice:1690 },
    { symbol:'INFY',      quantity:15, averagePrice:1920 },
    { symbol:'LT',        quantity:5,  averagePrice:3200 },
  ];
  let watchlist  = ['WIPRO','MARUTI','BAJFINANCE','TATAMOTORS','AXISBANK'];
  let orders     = [];

  // Simulate live price ticks
  function tick() {
    stocks.forEach(s => {
      const delta = s.price * (rand(-0.007, 0.007));
      s.price     = Math.max(1, +(s.price + delta).toFixed(2));
      s.changePercent = +((s.price - s.previousClose)/s.previousClose*100).toFixed(2);
    });
  }

  function getStock(sym) { return stocks.find(s => s.symbol === sym.toUpperCase()); }

  function getHistory(sym) {
    const s = getStock(sym);
    let p = s ? s.previousClose * 0.92 : 1000;
    const pts = [];
    for (let i = 0; i < 60; i++) {
      p = Math.max(1, p + p * rand(-0.012, 0.012));
      pts.push({ price:+p.toFixed(2), recordedAt: new Date(Date.now() - (60-i)*300000).toISOString() });
    }
    pts.push({ price: s ? s.price : p, recordedAt: new Date().toISOString() });
    return pts;
  }

  function getPortfolioSummary(user) {
    let inv = 0, cur = 0;
    const holdings = portfolio.filter(h => h.quantity > 0).map(h => {
      const s  = getStock(h.symbol);
      if (!s) return null;
      const invested = h.averagePrice * h.quantity;
      const current  = s.price * h.quantity;
      inv += invested; cur += current;
      return {
        symbol: h.symbol, companyName: s.companyName, sector: s.sector,
        iconCode: s.iconCode, quantity: h.quantity, averagePrice: h.averagePrice,
        currentPrice: s.price, investedValue: +invested.toFixed(2),
        currentValue: +current.toFixed(2),
        unrealizedPnL: +(current - invested).toFixed(2),
        unrealizedPnLPct: +((current-invested)/invested*100).toFixed(2),
      };
    }).filter(Boolean);
    return {
      availableBalance: user.balance, totalInvested: +inv.toFixed(2),
      totalCurrentValue: +cur.toFixed(2), totalPnL: +(cur-inv).toFixed(2),
      totalPnLPercent: inv ? +((cur-inv)/inv*100).toFixed(2) : 0,
      totalHoldings: holdings.length, holdings,
    };
  }

  function placeOrder(type, symbol, qty, user) {
    const s = getStock(symbol);
    if (!s) return { ok:false, message:'Stock not found: '+symbol };
    const cost = +(s.price * qty).toFixed(2);

    if (type === 'BUY') {
      if (user.balance < cost) return { ok:false, message:`Insufficient balance. Need ₹${cost}, have ₹${user.balance}` };
      user.balance = +(user.balance - cost).toFixed(2);
      const h = portfolio.find(p => p.symbol === symbol);
      if (h) {
        const tot = h.averagePrice * h.quantity + cost;
        h.quantity += qty;
        h.averagePrice = +(tot / h.quantity).toFixed(2);
      } else {
        portfolio.push({ symbol, quantity:qty, averagePrice:s.price });
      }
    } else {
      const h = portfolio.find(p => p.symbol === symbol);
      if (!h || h.quantity < qty) return { ok:false, message:`Insufficient shares. Have ${h?.quantity||0}, need ${qty}` };
      user.balance = +(user.balance + cost).toFixed(2);
      h.quantity -= qty;
    }

    const order = { id:Date.now(), symbol, quantity:qty, price:s.price, type, status:'COMPLETED', timestamp:new Date().toISOString() };
    orders.unshift(order);
    localStorage.setItem('sp_user', JSON.stringify(user));
    return { ok:true, order, remainingBalance:user.balance };
  }

  return { stocks, portfolio, watchlist, orders, tick, getColor, getStock, getHistory, getPortfolioSummary, placeOrder };
})();

// ── Toast utility ────────────────────────────────────────
function toast(msg, type='info') {
  let root = document.getElementById('toast-root');
  if (!root) {
    root = document.createElement('div');
    root.id = 'toast-root';
    document.body.appendChild(root);
  }
  const icons = { success:'✅', error:'❌', info:'ℹ️', warning:'⚠️' };
  const t = document.createElement('div');
  t.className = `toast ${type}`;
  t.innerHTML = `<span class="toast-icon">${icons[type]||'ℹ️'}</span><span class="toast-body">${msg}</span>`;
  root.appendChild(t);
  setTimeout(() => { t.style.cssText='opacity:0;transform:translateX(30px);transition:all .3s'; setTimeout(()=>t.remove(),350); }, 3200);
}

// ── Number formatters ────────────────────────────────────
const fmt    = (n, d=2) => '₹' + Math.abs(+n).toLocaleString('en-IN',{minimumFractionDigits:d,maximumFractionDigits:d});
const fmtNum = (n)      => (+n).toLocaleString('en-IN');
const fmtPct = (n,d=2)  => (n>=0?'+':'')+((+n).toFixed(d))+'%';
const pct    = (cur,prev)=> prev ? ((cur-prev)/prev*100) : 0;
